package com.edu.rxjava.subscription;

import com.edu.rxjava.observable.Observable;
import com.edu.rxjava.observer.Observer;
import com.edu.rxjava.scheduler.SingleThreadScheduler;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class DisposableTest {

    @Test
    void testDisposeStopsEmission() throws InterruptedException {
        // потокобезопасный список для собранных значений
        List<Integer> results = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch disposedLatch = new CountDownLatch(1);
        AtomicReference<Disposable> ref = new AtomicReference<>();

        // источник: эмиттит числа 1..5 с небольшими задержками
        Observable<Integer> src = Observable.create(emitter -> {
            for (int i = 1; i <= 5; i++) {
                emitter.onNext(i);
                try { Thread.sleep(10); } catch (InterruptedException ignored) {}
            }
            emitter.onComplete();
        });

        // запускаем эмиссию в отдельном потоке
        SingleThreadScheduler scheduler = new SingleThreadScheduler();
        Disposable d = src
                .subscribeOn(scheduler)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                        if (item == 3) {
                            // при третьем элементе – отменяем подписку
                            ref.get().dispose();
                            disposedLatch.countDown();
                        }
                    }
                    @Override public void onError(Throwable t) { /* не ожидаем */ }
                    @Override public void onComplete()    { /* не ожидаем */ }
                });
        ref.set(d);

        // ждём, что dispose() будет вызван
        assertTrue(disposedLatch.await(1, TimeUnit.SECONDS), "dispose() не вызвался");

        // даём немного времени, чтобы остальные таски могли бы сработать, если бы не dispose
        Thread.sleep(50);

        // убеждаемся, что после 3 эмиссии больше ничего не пришло
        assertTrue(results.containsAll(List.of(1, 2, 3)), "Первые три элемента должны быть");
        assertFalse(results.contains(4), "Четвёртый элемент не должен попасть");
        assertFalse(results.contains(5), "Пятый элемент не должен попасть");
    }
}
