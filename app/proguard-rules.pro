# kotlinx.serialization — keep generated serializers for @Serializable model classes.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-keepclassmembers class com.tertiaryinfotech.sudokuapp.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.tertiaryinfotech.sudokuapp.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.tertiaryinfotech.sudokuapp.model.**$$serializer { *; }
