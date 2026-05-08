package com.kangrio.shellserver;

import android.os.Bundle;

interface IShellServer {
    void invodeSystemService();
    String exec(String cmd);
    int runOnce(String className, in Bundle bundle, long delayMs);
    int schedule(String className, in Bundle bundle, long initialDelayMs, long intervalMs);
    void cancel(int taskId);
}