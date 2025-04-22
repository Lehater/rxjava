package com.edu.rxjava.observable;

import com.edu.rxjava.observer.Observer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObservableOperatorTest {

    @Test
    void testMapTransformsValues() {
        List<Integer> results = new ArrayList<>();
        Observable<Integer> src = Observable.create(emitter -> {
            emitter.onNext(1);
            emitter.onNext(2);
            emitter.onNext(3);
        });

        src.map(i -> i * 2)
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onNext(Integer item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        // no-op
                    }
                });

        assertEquals(List.of(2, 4, 6), results);
    }

    @Test
    void testFilterSelectsOnlyMatching() {
        List<String> results = new ArrayList<>();
        Observable<String> src = Observable.create(emitter -> {
            emitter.onNext("alpha");
            emitter.onNext("beta");
            emitter.onNext("gamma");
        });

        src.filter(s -> s.length() > 4)
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String item) {
                        results.add(item);
                    }

                    @Override
                    public void onError(Throwable t) {
                        fail("Unexpected error: " + t.getMessage());
                    }

                    @Override
                    public void onComplete() {
                        // no-op
                    }
                });

        assertEquals(List.of("alpha", "gamma"), results);
    }


    @Test
    void testMapErrorPropagates() {
        IllegalStateException ex = new IllegalStateException("mapper failed");
        Observable<Integer> src = Observable.create(e -> e.onNext(1));

        List<Throwable> errors = new ArrayList<>();

        // ← здесь контекст присвоения даёт компилятору знать, что R = Integer
        Observable<Integer> mapped = src.map(i -> { throw ex; });

        mapped.subscribe(new Observer<Integer>() {
            @Override public void onNext(Integer item) { }
            @Override public void onError(Throwable t) { errors.add(t); }
            @Override public void onComplete() { }
        });

        assertEquals(1, errors.size());
        assertSame(ex, errors.get(0));
    }


    @Test
    void testFilterErrorPropagates() {
        RuntimeException ex = new IllegalStateException("mapper failed");
        Observable<Integer> src = Observable.create(emitter -> {
            emitter.onNext(10);
        });

        final List<Throwable> errors = new ArrayList<>();
        src.filter(i -> { throw ex; })
                .subscribe(new Observer<Integer>() {
                    @Override public void onNext(Integer item) {}
                    @Override public void onError(Throwable t) {
                        errors.add(t);
                    }
                    @Override public void onComplete() {}
                });

        assertEquals(1, errors.size());
        assertSame(ex, errors.get(0));
    }
}
