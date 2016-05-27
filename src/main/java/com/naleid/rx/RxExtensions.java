package com.naleid.rx;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;

public abstract class RxExtensions {
    private RxExtensions() { }

    /**
     * Alias for doOnNext.  Allows performing a synchronous side-effect on the stream without changing the stream.
     *
     * Good for things like logging, metric incrementing, etc.
     *
     * Should NOT be used with an action that returns an Observable, use flatTap for that.
     */
    public static <T> Observable<T> tap(Observable<T> observable, final Action1<? super T> onNext) {
        return observable.doOnNext(onNext);
    }

    /**
     *  map : flatMap :: tap : flatTap
     *
     *  Allows an asychronous side-effect to be performed.  Will subscribe to the stream and any exceptions that
     *  are thrown, but downstream will ignore the asynchronous result (if any) and will continue to pass along
     *  the original value that was passed to the
     *
     */
    public static <T, R> Observable<T> flatTap(Observable<T> observable, Func1<? super T, ? extends Observable<R>> func) {
        return observable.flatMap((T t) -> {
            Observable<R> asyncObservable = func.call(t);
            Observable<T> originalResultObservable = asyncObservable.map( (R ignore) -> t );
            return originalResultObservable;
        });
    }
}
