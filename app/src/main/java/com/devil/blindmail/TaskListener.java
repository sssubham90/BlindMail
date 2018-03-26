package com.devil.blindmail;
public interface TaskListener {
    void onTaskStarted();

    void onTaskFinished(boolean result);
}