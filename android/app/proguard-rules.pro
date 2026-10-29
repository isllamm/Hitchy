# Hitchy app proguard rules

# Kotlinx Serialization
-keepattributes *Annotation*,InnerClasses
-dontwarn kotlinx.serialization.**
-keepclassmembers class * {
    @kotlinx.serialization.SerialName <fields>;
}
-keepclassmembers class **$$serializer {
    *;
}
-keepclasseswithmembers class * {
    public static final **$Companion Companion;
}
-keepclassmembers class * {
    *** Companion;
}
