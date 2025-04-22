package com.edu.rxjava.scheduler;

import com.edu.rxjava.observable.Observable;
import com.edu.rxjava.observer.Observer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class SchedulerTest {

    @Test
    void testSubscribeOnUsesScheduler() throws InterruptedException {
        SingleThreadScheduler scheduler = new SingleThreadScheduler();

        Observable<Integer> src = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onComplete();
        });

        List<String> threads = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        src.subscribeOn(scheduler)
                .subscribe(new Observer<Integer>() {
                    @Override public void onNext(Integer item) { }
                    @Override public void onError(Throwable t) { }
                    @Override public void onComplete() {
                        threads.add(Thread.currentThread().getName());
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS));
        assertTrue(threads.get(0).startsWith("SingleThreadScheduler-"));
    }

    @Test
    void testObserveOnUsesScheduler() throws InterruptedException {
        SingleThreadScheduler scheduler = new SingleThreadScheduler();
        Observable<Integer> src = Observable.create(e -> {
            e.onNext(1);
            e.onComplete();
        });

        List<String> threads = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);

        src.observeOn(scheduler)
                .subscribe(new Observer<Integer>() {
                    @Override public void onNext(Integer item) {
                        threads.add(Thread.currentThread().getName());
                    }
                    @Override public void onError(Throwable t) { }
                    @Override public void onComplete() {
                        latch.countDown();
                    }
                });

        assertTrue(latch.await(1, TimeUnit.SECONDS), "onComplete timeout");
        assertFalse(threads.isEmpty());
        assertTrue(threads.get(0).startsWith("SingleThreadScheduler-"));
    }
}
