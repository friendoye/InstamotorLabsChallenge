package com.friendoye.ilchallenge

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import rx.Observable
import rx.observers.TestSubscriber
import java.io.IOException

class ConnectivitySpec: Spek({
    var testSubscriber = TestSubscriber<Int>()

    beforeEach {
        testSubscriber = TestSubscriber<Int>()
    }

    describe("an Observable, which will be transformed by connectivity operator") {
        val observable = Observable.just(1, 2, 3, 4, 5)

        on("no connection") {
            beforeEach {
                observable.connectionCheck(Observable.create { it.onNext(false) }).subscribe(testSubscriber)
            }

            it("should not emit any value") {
                testSubscriber.assertNoValues()
            }

            it("should not terminate") {
                testSubscriber.assertNoTerminalEvent()
            }
        }

        on("has connection") {
            beforeEach {
                observable.connectionCheck(Observable.create { it.onNext(true) }).subscribe(testSubscriber)
            }

            it("should emit all values") {
                testSubscriber.assertValueCount(5)
            }

            it("should complete") {
                testSubscriber.assertCompleted()
            }
        }

        on("connection restores") {
            beforeEach {
                observable.connectionCheck(Observable.create { it.onNext(false); it.onNext(true) }).subscribe(testSubscriber)
            }

            it("should emit all values") {
                testSubscriber.assertValueCount(5)
            }
        }

        on("connection losses") {
            beforeEach {
                var wasThrew = false
                observable.doOnNext {
                    if (!wasThrew) {
                        wasThrew = true
                        throw IOException()
                    }
                }.connectionCheck(Observable.create { it.onNext(true) }).subscribe(testSubscriber)
            }

            it("should emit all values") {
                testSubscriber.assertValueCount(5)
            }

            it("should not emit errors") {
                testSubscriber.assertNoErrors()
            }
        }
    }

    afterEach {
        testSubscriber.unsubscribe()
    }
})