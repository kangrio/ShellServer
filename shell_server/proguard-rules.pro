-keepclassmembers class com.kangrio.shellserver.server.ShellServer {
    public static *;
}
-keepclassmembers class * implements  com.kangrio.shellserver.shared.ShellServerRunnable { *; }
-keep class com.kangrio.shellserver.client.WakeUpActivity { *; }
-keep class com.kangrio.shellserver.server.ContextHelper { *; }
-keep class android.app.** { *; }