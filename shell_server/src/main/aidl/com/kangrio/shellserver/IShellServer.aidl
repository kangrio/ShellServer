package com.kangrio.shellserver;

interface IShellServer {
    void invodeSystemService();
    String exec(String cmd);
    int runOnce(String className, long delayMs);
    int schedule(String className, long initialDelayMs, long intervalMs);
    void cancel(int taskId);
}