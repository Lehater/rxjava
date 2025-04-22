package com.edu.rxjava.observable;

import com.edu.rxjava.observer.Observer;
import com.edu.rxjava.scheduler.Scheduler;
import com.edu.rxjava.subscription.BooleanDisposable;
import com.edu.rxjava.subscription.Disposable;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Основной класс потока.
 */
public class Observable<T> {
    private final OnSubscribe<T> onSubscribe;

    private Observable(OnSubscribe<T> onSubscribe) {
        this.onSubscribe = onSubscribe;
    }

    public static <T> Observable<T> create(OnSubscribe<T> onSubscribe) {
        return new Observable<>(onSubscribe);
    }

    /**
     * Подписаться и получить Disposable для отмены.
     */
    public Disposable subscribe(Observer<T> actual) {
        BooleanDisposable d = new BooleanDisposable();
        Observer<T> guarded = new Observer<>() {
            @Override public void onNext(T item) {
                if (!d.isDisposed()) actual.onNext(item);
            }
            @Override public void onError(Throwable t) {
                if (!d.isDisposed()) actual.onError(t);
            }
            @Override public void onComplete() {
                if (!d.isDisposed()) actual.onComplete();
            }
        };
        try {
            onSubscribe.call(guarded);
        } catch (Throwable t) {
            guarded.onError(t);
        }
        return d;
    }

    public <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
        return create(emitter ->
                this.subscribe(new Observer<>() {
                    @Override public void onNext(T item) {
                        R mapped;
                        try { mapped = mapper.apply(item); }
                        catch (Throwable t) { emitter.onError(t); return; }
                        emitter.onNext(mapped);
                    }
                    @Override public void onError(Throwable t) { emitter.onError(t); }
                    @Override public void onComplete() { emitter.onComplete(); }
                })
        );
    }

    public Observable<T> filter(Predicate<? super T> predicate) {
        return create(emitter ->
                this.subscribe(new Observer<>() {
                    @Override public void onNext(T item) {
                        boolean ok;
                        try { ok = predicate.test(item); }
                        catch (Throwable t) { emitter.onError(t); return; }
                        if (ok) emitter.onNext(item);
                    }
                    @Override public void onError(Throwable t) { emitter.onError(t); }
                    @Override public void onComplete() { emitter.onComplete(); }
                })
        );
    }

    /**
     * Оператор flatMap: для каждого элемента создаёт вложенный Observable и "сливает" их в единый поток.
     */
    public <R> Observable<R> flatMap(Function<? super T, Observable<R>> mapper) {
        return create(emitter ->
                this.subscribe(new Observer<T>() {
                    @Override
                    public void onNext(T item) {
                        Observable<R> inner;
                        try {
                            inner = mapper.apply(item);
                        } catch (Throwable t) {
                            emitter.onError(t);
                            return;
                        }
                        // здесь inner — точно Observable<R>, так что subscribe(new Observer<R>) подходит
                        inner.subscribe(new Observer<R>() {
                            @Override
                            public void onNext(R r) {
                                emitter.onNext(r);
                            }

                            @Override
                            public void onError(Throwable t) {
                                emitter.onError(t);
                            }

                            @Override
                            public void onComplete() {
                                // мы не закрываем главный поток здесь
                            }
                        });
                    }

                    @Override
                    public void onError(Throwable t) {
                        emitter.onError(t);
                    }

                    @Override
                    public void onComplete() {
                        emitter.onComplete();
                    }
                })
        );
    }

    public Observable<T> subscribeOn(Scheduler scheduler) {
        return create(emitter ->
                scheduler.execute(() -> this.subscribe(emitter))
        );
    }

    public Observable<T> observeOn(Scheduler scheduler) {
        return create(emitter ->
                this.subscribe(new Observer<>() {
                    @Override public void onNext(T item) {
                        scheduler.execute(() -> emitter.onNext(item));
                    }
                    @Override public void onError(Throwable t) {
                        scheduler.execute(() -> emitter.onError(t));
                    }
                    @Override public void onComplete() {
                        scheduler.execute(emitter::onComplete);
                    }
                })
        );
    }
}
