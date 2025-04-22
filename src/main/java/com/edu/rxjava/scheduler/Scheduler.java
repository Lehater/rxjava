package com.edu.rxjava.scheduler;

public interface Scheduler {
    void execute(Runnable task);
    void shutdown();
}
