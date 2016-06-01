package com.naleid.rx

import rx.Observable
import rx.functions.Func1
import spock.lang.Specification
import spock.lang.Unroll

class RxExtensionsSpec extends Specification {
    def "tap maintains original value"() {
        given:
        Boolean tapCalled = false
        Boolean subscribeCalled = false

        when:
        Observable.just(1)
                .tap { Integer val -> tapCalled = true }
                .single()
                .subscribe { Integer val ->
                    assert 1 == val
                    subscribeCalled = true
                }

        then:
        tapCalled && subscribeCalled
    }

    def "flatTap maintains original value"() {
        given:
        Boolean flatTapCalled = false
        Boolean flatTapResultObserved = false
        Boolean subscribeCalled = false

        when:
        Observable.just(1)
                .flatTap({ Integer val ->
                    flatTapCalled = true
                    return Observable.just(2).doOnNext({ flatTapResultObserved = true })
                } as Func1)
                .single()
                .subscribe { Integer val ->
                    assert 1 == val
                    subscribeCalled = true
                }

        then:
        flatTapCalled && flatTapResultObserved && subscribeCalled
    }

    def "flatTap passes along errors that happen during tap"() {
        given:
        Boolean errorThrownAndCaught = false
        Boolean subscribeCalled = false

        when:
        Observable.just(1)
                .flatTap({ Integer val -> throw new Exception("Boom!") } as Func1)
                .onErrorResumeNext({ Throwable t ->
                    errorThrownAndCaught = true
                    assert t.cause.message == "Boom!"
                    Observable.just(2)
                })
                .single()
                .subscribe { Integer val ->
                    assert 2 == val
                    subscribeCalled = true
                }

        then:
        errorThrownAndCaught && subscribeCalled
    }

    def "flatTap will return original value if an empty observable is subscribed to"() {
        given:
        Boolean flatTapCalled = false
        Boolean subscribeCalled = false

        when:
        Observable.just(1)
                .flatTap({ Integer val ->
                    flatTapCalled = true
                    return Observable.empty()
                } as Func1)
                .single()
                .subscribe { Integer val ->
                    assert 1 == val
                    subscribeCalled = true
                }

        then:
        flatTapCalled && subscribeCalled
    }

    def "flatMap will return original value if null observable is returned from closure"() {
        given:
        Boolean flatTapCalled = false
        Boolean subscribeCalled = false

        when:
        Observable.just(1)
                .flatTap({ Integer val ->
                    flatTapCalled = true
                    return null
                } as Func1)
                .single()
                .subscribe { Integer val ->
                    assert 1 == val
                    subscribeCalled = true
                }

        then:
        flatTapCalled && subscribeCalled

    }

    def "flatMap will return single original value even if observable we flatTap returns multiple"() {
        given:
        Integer flatTappedObservableObservedCount = 0
        Boolean flatTapCalled = false
        Boolean subscribeCalled = false

        when:
        Observable.just(1)
                .flatTap({ Integer val ->
                    flatTapCalled = true
                    return Observable.from((1..10).toList())
                            .tap { flatTappedObservableObservedCount += 1 }
                } as Func1)
                .single()
                .subscribe { Integer val ->
                    assert 1 == val
                    subscribeCalled = true
                }

        then:
        flatTapCalled && subscribeCalled && flatTappedObservableObservedCount == 10
    }

    @Unroll
    def "flatMap will returns original values in stream no matter how many are emitted from the flatTap observable: #flatTapObservable"() {
        given:
        Boolean flatTapCalled = false
        Boolean subscribeCalled = false

        when:
        Observable.from([1, 2])
                .flatTap({ Integer val ->
                    flatTapCalled = true
                    return flatTapObservable
                } as Func1)
                .toList()
                .subscribe { List<Integer> vals ->
                    assert [1, 2] == vals
                    subscribeCalled = true
                }

        then:
        flatTapCalled && subscribeCalled

        where:
        flatTapObservable << [null, Observable.empty(), Observable.just(1), Observable.from((1..10).toList())]
    }
}
