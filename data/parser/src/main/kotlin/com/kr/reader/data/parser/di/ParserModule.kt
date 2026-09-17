/**
 * @Author: wj 3363891051@qq.com
 * @Date: 2026-09-17 09:00
 * @LastEditors: wj 3363891051@qq.com
 * @LastEditTime: 2026-09-17 09:00
 * @FilePath: data/parser/src/main/kotlin/com/kr/reader/data/parser/di/ParserModule.kt
 * @Description: 解析层 DI 绑定：面向接口编程，domain 层用例只依赖抽象
 */
package com.kr.reader.data.parser.di

import com.kr.reader.data.parser.BookInfoExtractorImpl
import com.kr.reader.data.parser.LocalBookReaderImpl
import com.kr.reader.data.parser.pdf.PdfParser
import com.kr.reader.domain.repository.BookInfoExtractor
import com.kr.reader.domain.repository.LocalBookReader
import com.kr.reader.domain.repository.PdfPageRenderer
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ParserModule {

    @Binds
    @Singleton
    abstract fun bindLocalBookReader(impl: LocalBookReaderImpl): LocalBookReader

    @Binds
    @Singleton
    abstract fun bindBookInfoExtractor(impl: BookInfoExtractorImpl): BookInfoExtractor

    @Binds
    @Singleton
    abstract fun bindPdfPageRenderer(impl: PdfParser): PdfPageRenderer
}
