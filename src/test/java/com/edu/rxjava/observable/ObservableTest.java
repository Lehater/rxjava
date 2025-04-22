// File: src/test/java/com/edu/rxjava/observable/ObservableTest.java
package com.edu.rxjava.observable;

import com.edu.rxjava.observer.Observer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObservableTest {

    @Test
    void testCreateAndComplete() {
        List<Integer> received = new ArrayList<>();
        Observable<Integer> src = Observable.create(emitter -> {
            emitter.onNext(5);
            emitter.onComplete();
        });

        src.subscribe(new Observer<>() {
            @Override public void onNext(Integer item)    { received.add(item); }
            @Override public void onError(Throwable t)     { fail("Unexpected error"); }
            @Override public void onComplete()             { received.add(-1); }
        });

        assertEquals(List.of(5, -1), received);
    }

    @Test
    void testCreateAndError() {
        RuntimeException ex = new RuntimeException("boom");
        List<Throwable> errors = new ArrayList<>();
        Observable<Integer> src = Observable.create(emitter -> {
            throw ex;
        });

        src.subscribe(new Observer<>() {
            @Override public void onNext(Integer item)    { }
            @Override public void onError(Throwable t)     { errors.add(t); }
            @Override public void onComplete()             { fail("Should not complete"); }
        });

        assertEquals(1, errors.size());
        assertSame(ex, errors.get(0));
    }
}
