/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-23 10:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-23 10:00
 * @FilePath: data/sources/src/main/kotlin/com/kr/reader/data/sources/SourceRepositoryImpl.kt
 * @Description: 书源仓储实现：Room 读写 + 规则 JSON 列编解码 + 健康度打点
 */
package com.kr.reader.data.sources

import com.kr.reader.core.common.IoDispatcher
import com.kr.reader.core.database.dao.SourceDao
import com.kr.reader.core.model.SourceHealth
import com.kr.reader.core.model.source.BookSource
import com.kr.reader.domain.repository.SourceRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class SourceRepositoryImpl @Inject constructor(
    private val dao: SourceDao,
) : SourceRepository {

    override fun observeSources(): Flow<List<BookSource>> =
        dao.observeSources()
            .map { rows -> rows.map { it.toModel() } }
            .flowOn(IoDispatcher)

    /** DAO 只提供了「启用且未损坏」的查询，全量列表复用 Flow 的首帧，避免再加一条几乎相同的 SQL */
    override suspend fun all(): List<BookSource> = withContext(IoDispatcher) {
        dao.observeSources().first().map { it.toModel() }
    }

    /**
     * 聚合搜索的源顺序：SQL 已过滤掉 BROKEN 与禁用源，
     * 这里再按健康分降序，让「又快又稳」的源先出结果——用户感知的搜索速度取决于首个返回，而非全部返回。
     */
    override suspend fun enabledSourcesSortedByHealth(): List<BookSource> = withContext(IoDispatcher) {
        dao.getEnabledSourcesSortedByHealth()
            .map { it.toModel() }
            .sortedByDescending { it.healthScore }
    }

    override suspend fun get(id: Long): BookSource? = withContext(IoDispatcher) {
        dao.getSource(id)?.toModel()
    }

    override suspend fun insert(source: BookSource): Long = withContext(IoDispatcher) {
        dao.insert(source.toEntity())
    }

    override suspend fun delete(id: Long): Unit = withContext(IoDispatcher) {
        dao.delete(id)
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean): Unit = withContext(IoDispatcher) {
        dao.setEnabled(id, enabled)
    }

    override suspend fun existsByName(name: String): Boolean = withContext(IoDispatcher) {
        dao.existsByName(name) > 0
    }

    override suspend fun reorder(id: Long, newOrder: Int): Unit = withContext(IoDispatcher) {
        dao.updateSortOrder(id, newOrder)
    }

    /**
     * 成功打点：failCount 归零、健康度回 OK、平均延迟指数平滑。
     * 这里显式「读-改-写」而不调 DAO 的默认方法实现：
     * Room 对「@Dao 接口中带函数体的默认方法」的支持在不同版本表现不一致，
     * 把语义收敛在数据层可以消除这一整类编译期风险。
     */
    override suspend fun recordSuccess(id: Long, latencyMs: Int): Unit = withContext(IoDispatcher) {
        val at = System.currentTimeMillis()
        val entity = dao.loadForUpdate(id) ?: return@withContext
        dao.insert(
            entity.copy(
                failCount = 0,
                health = SourceHealth.OK.name,
                lastSuccessAt = at,
                lastCheckedAt = at,
                // 指数平滑而非全量重算：单次抖动不该把源的排序权重整个掀翻
                avgLatencyMs = (entity.avgLatencyMs * 3 + latencyMs) / 4,
            ),
        )
    }

    /** 失败打点：连续失败 3 次 DEGRADED、10 次 BROKEN，之后聚合搜索自动跳过该源 */
    override suspend fun recordFailure(id: Long): Unit = withContext(IoDispatcher) {
        dao.recordFailureInternal(id, System.currentTimeMillis())
    }
}
