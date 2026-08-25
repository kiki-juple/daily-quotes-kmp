# Tink (pulled in by androidx.security:security-crypto) references Error Prone's annotations, which
# are compile-time only and absent at runtime. Suppressing the warning is the right fix here —
# adding the annotations as a runtime dependency would ship classes nothing ever loads.
-dontwarn com.google.errorprone.annotations.CanIgnoreReturnValue
-dontwarn com.google.errorprone.annotations.CheckReturnValue
-dontwarn com.google.errorprone.annotations.Immutable
-dontwarn com.google.errorprone.annotations.RestrictedApi

# kotlinx.serialization looks up the generated `Companion.serializer()` of every @Serializable type
# reflectively, so the companions and their serializer members have to survive shrinking. The nav
# keys in :composeApp are resolved this way when the back stack is restored.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}
-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class **$* {
    kotlinx.serialization.KSerializer serializer(...);
}
