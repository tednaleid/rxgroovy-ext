package com.naleid.rx

import rx.Observable
import rx.functions.Func1
import spock.lang.Specification

class RxExtensionsSpec extends Specification {
    def "tap maintains original value"() {
        given:
        Boolean tapCalled = false
        Boolean subscribeCalled = false

        when:
        Observable.just(1)
                .tap { Integer val -> tapCalled = true }
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
                .subscribe { Integer val ->
                    assert 1 == val
                    subscribeCalled = true
                }

        then:
        flatTapCalled && subscribeCalled

    }
}
