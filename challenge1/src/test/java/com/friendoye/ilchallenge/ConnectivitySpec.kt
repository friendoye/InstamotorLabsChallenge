package com.friendoye.ilchallenge

import org.jetbrains.spek.api.Spek
import rx.Observable
import rx.observers.TestSubscriber
import java.util.concurrent.TimeUnit

class ConnectivitySpec: Spek({
    var testSubscriber: TestSubscriber<Long> = TestSubscriber<Long>()
    val tickerObservable = Observable.interval(1, TimeUnit.SECONDS)
    val emptyObservable = Observable.empty<Boolean>()

    beforeEach {
        testSubscriber = TestSubscriber<Long>()
    }

    describe("an Observable, which will be transformed by connectivity operator") {
        val observable: Observable<Long> = Observable.just(1L, 2L, 3L, 4L, 5L)

        it("should be unsubscribed when device has no Internet connection") {
            observable.connectionCheck(emptyObservable)
                    .subscribe(testSubscriber)

            // we always get back subscription, so test whether we received any item
            testSubscriber.awaitTerminalEventAndUnsubscribeOnTimeout(3, TimeUnit.SECONDS)
            testSubscriber.assertValueCount(0)
        }

        it("should subscribe when device has Internet connection") {
            observable.connectionCheck(tickerObservable)
                    .subscribe(testSubscriber)

            testSubscriber.awaitTerminalEventAndUnsubscribeOnTimeout(3, TimeUnit.SECONDS)
            testSubscriber.assertValueCount(5)
        }

    }

    describe("an Observable, which has connection problem - " +
             "throws IOException during 1st subscription") {
        val observable: Observable<Long> = getConnectionProblemObservable(5)

        it("should resubscribe when device has lost Internet connection - IOException thrown") {
            observable.connectionCheck(tickerObservable)
                    .subscribe(testSubscriber)

            testSubscriber.awaitTerminalEventAndUnsubscribeOnTimeout(3, TimeUnit.SECONDS)
            testSubscriber.assertValueCount(5)
        }
    }

    afterEach {
        testSubscriber.unsubscribe()
    }
})