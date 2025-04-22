// File: src/test/java/com/edu/rxjava/observer/ObserverTest.java
package com.edu.rxjava.observer;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ObserverTest {

    static class TestObserver<T> implements Observer<T> {
        final List<T> nexts = new ArrayList<>();
        Throwable error;
        boolean completed = false;

        @Override public void onNext(T item) { nexts.add(item); }
        @Override public void onError(Throwable t)  { error = t; }
        @Override public void onComplete()           { completed = true; }
    }

    @Test
    void observerInvokesAllMethods() {
        TestObserver<String> obs = new TestObserver<>();
        obs.onNext("one");
        obs.onError(new IllegalStateException("err"));
        obs.onComplete();

        assertEquals(List.of("one"), obs.nexts);
        assertTrue(obs.error instanceof IllegalStateException);
        assertEquals("err", obs.error.getMessage());
        assertTrue(obs.completed);
    }
}
