# Keep SLF4J classes
-dontwarn org.slf4j.**
-keep class org.slf4j.** { *; }
-keepclassmembers class org.slf4j.** { *; }

# SLF4J uses reflection
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
