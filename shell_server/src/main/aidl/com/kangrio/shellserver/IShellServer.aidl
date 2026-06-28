package com.kangrio.shellserver;

import android.os.Bundle;

interface IShellServer {
    void invodeSystemService();
    void keepAliveClient(IBinder clientToken);
    Bundle exec(String cmd);
    int runOnce(in byte[] data, long delayMs);
    int schedule(in byte[] data, long initialDelayMs, long intervalMs);
    void cancel(int taskId);
}