package com.edu.rxjava.subscription;

/**
 * Контракт для отмены подписки.
 */
public interface Disposable {
    /**
     * Отменить подписку — дальнейшие onNext/onError/onComplete не будут вызваны.
     */
    void dispose();

    /**
     * @return true, если подписка отменена.
     */
    boolean isDisposed();
}
