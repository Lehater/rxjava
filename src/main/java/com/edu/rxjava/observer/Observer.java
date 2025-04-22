package com.edu.rxjava.observer;

/**
 * Интерфейс наблюдателя для реактивного потока.
 */
public interface Observer<T> {

    /**
     * Вызывается при поступлении нового элемента.
     */
    void onNext(T item);

    /**
     * Вызывается при возникновении ошибки.
     */
    void onError(Throwable t);

    /**
     * Вызывается при нормальном завершении потока.
     */
    void onComplete();
}
