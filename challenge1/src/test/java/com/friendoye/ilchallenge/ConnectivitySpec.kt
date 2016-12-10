package com.friendoye.ilchallenge

import org.jetbrains.spek.api.Spek
import rx.Observable
import rx.observers.TestSubscriber

class ConnectivitySpec: Spek({
    var testSubscriber: TestSubscriber<Long> = TestSubscriber<Long>()

    describe("an Observable, which will be transformed by connectivity operator") {
        var observable: Observable<Long> = Observable.empty()

        beforeEach {
            observable = Observable.just(1L, 2L, 3L, 4L, 5L)
            testSubscriber = TestSubscriber<Long>()
        }

        it("should be unsubscribed when device has no Internet connection") {
            observable.connectionCheck(mockConnectionStatus(false))
                    .subscribe(testSubscriber)

            // we always get back subscription, so test whether we received any item
            testSubscriber.assertValueCount(0)
        }

        it("should subscribe when device has Internet connection") {
            observable.connectionCheck(mockConnectionStatus(true))
                    .subscribe(testSubscriber)

            testSubscriber.awaitTerminalEvent()
            testSubscriber.assertValueCount(5)
        }


        afterEach {
            testSubscriber.unsubscribe()
        }
    }

    describe("an Observable, which has connection problem - throws IOException every 2nd time") {
        var observable: Observable<Long> = getConnectionProblemObservable(5)

        it("should resubscribe when device has lost Internet connection") {
            observable.connectionCheck(mockConnectionStatus(true))
                    .subscribe(testSubscriber)

            testSubscriber.awaitTerminalEvent()
            testSubscriber.assertValueCount(5)
            testSubscriber.unsubscribe()
        }
    }
})