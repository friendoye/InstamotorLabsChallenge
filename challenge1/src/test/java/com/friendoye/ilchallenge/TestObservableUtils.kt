package com.friendoye.ilchallenge

import rx.Observable
import java.io.IOException

fun getConnectionProblemObservable(count: Long): Observable<Long> {
    val obj = object {
        val triggered = false
            get() {
                val currentValue = field
                field = !field
                return currentValue
            }
    }

    return Observable.create<Long> { subscriber ->
        if (!obj.triggered) {
            subscriber.onError(IOException("Connection lost!"))
        } else {
            for (x in 1..count) {
                subscriber.onNext(x)
            }
            subscriber.onCompleted()
        }
    }
}