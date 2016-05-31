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
     *
     * @param observable the observable to listen to for values, but that we don't want to change the stream of
     * @param onNext the action to perform a synchronous side effect
     * @param <T> the datatype coming from the observable that we're passed
     * @return an observable that emits the same synchronous items that it's given
     */
    public static <T> Observable<T> tap(Observable<T> observable, final Action1<? super T> onNext) {
        return observable.doOnNext(onNext);
    }

    /**
     *  map : flatMap :: tap : flatTap
     *
     *  Allows an asychronous side-effect to be performed.  Will subscribe to the stream and any exceptions that
     *  are thrown, but downstream will ignore the asynchronous result (if any) and will continue to pass along
     *  the original value that was passed into the flatTap
     *
     *  It will also swallow null/Observable.empty() sequences and continue to operate correctly.
     *
     *  The function it calls must either return an Observable or null, another datatype will not work, use tap for that
     *
     * @param observable the observable to listen to for values, but that we don't want to change the stream of
     * @param onNext the asynchronous function that should return an Observable of type R
     * @param <T> the datatype comfing from the original Observable that we are passed
     * @param <R> the return type of the Observable in the asynchronous function that we will ignore
     * @return an Observable that emits the original item that we were given, unless there is an error
     */
    public static <T, R> Observable<T> flatTap(Observable<T> observable, Func1<? super T, ? extends Observable<R>> onNext) {
        return observable.flatMap((T t) -> {
            Observable<R> flatObservable = onNext.call(t);

            if (flatObservable == null) {
                return Observable.just(t);
            }

            return flatObservable
                    .defaultIfEmpty(null)
                    .map( (R ignore) -> t );
        });
    }
}
