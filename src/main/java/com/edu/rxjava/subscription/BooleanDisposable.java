package com.edu.rxjava.subscription;

/**
 * Простая реализация Disposable на основе булева флага.
 */
public class BooleanDisposable implements Disposable {
    private volatile boolean disposed = false;

    @Override
    public void dispose() {
        disposed = true;
    }

    @Override
    public boolean isDisposed() {
        return disposed;
    }
}
