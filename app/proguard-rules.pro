# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class com.vkusnyvybor.data.model.** { *; }
