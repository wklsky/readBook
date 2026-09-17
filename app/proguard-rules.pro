# 保留 kotlinx.serialization 生成的 serializer（书源 JSON 编解码依赖反射名不可混淆的前提是保留 @Serializable 类）
-keepclassmembers class com.kr.reader.core.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.kr.reader.core.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
# jsoup 通过反射读取节点类，混淆后规则引擎抽取会静默失败
-keep class org.jsoup.** { *; }
