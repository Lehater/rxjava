package com.edu.rxjava.observable;

import com.edu.rxjava.observer.Observer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObservableFlatMapTest {

    @Test
    void testFlatMapMergesStreams() {
        // Источник: эмиттит 1 и 2, а потом complete
        Observable<Integer> src = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onComplete();
        });

        List<String> results = new ArrayList<>();

        src.<String>flatMap(i ->
                        Observable.<String>create(inner -> {
                            inner.onNext("X" + i);
                            inner.onNext("Y" + i);
                            inner.onComplete();
                        })
                )
                // Теперь подписываемся на Observable<String> — используем Observer<String>
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Error in flatMap: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        // no-op
                    }
                });

        assertEquals(List.of("X1", "Y1", "X2", "Y2"), results);
    }

    @Test
    void testFlatMapErrorPropagates() {
        Observable<Integer> src = Observable.create(emitter -> emitter.onNext(1));
        RuntimeException ex = new RuntimeException("flatMap failed");
        List<Throwable> errors = new ArrayList<>();

        // опять подсказываем, что flatMap должен вернуть Observable<String>
        src.<String>flatMap(i -> { throw ex; })
                .subscribe(new Observer<String>() {
                    @Override public void onNext(String item) { /* no-op */ }
                    @Override public void onError(Throwable t) {
                        errors.add(t);
                    }
                    @Override public void onComplete() { /* no-op */ }
                });

        assertEquals(1, errors.size());
        assertSame(ex, errors.get(0));
    }
}
