package com.edu.rxjava.observable;

import com.edu.rxjava.observer.Observer;

/**
 * Функциональный интерфейс, реализующий логику эмиссии элементов.
 */
@FunctionalInterface
public interface OnSubscribe<T> {
    /**
     * Задача эмиссии: получает экземпляр Observer, которому отправляет события.
     */
    void call(Observer<T> observer);
}
