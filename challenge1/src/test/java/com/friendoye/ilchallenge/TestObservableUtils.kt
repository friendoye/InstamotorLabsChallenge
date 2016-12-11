package com.friendoye.ilchallenge

import rx.Observable
import java.io.IOException

fun getConnectionProblemObservable(count: Long): Observable<Long> {
    val obj = object {
        var triggered = false
    }

    return Observable.create<Long> { subscriber ->
        if (!obj.triggered) {
            obj.triggered = true
            subscriber.onError(IOException("Connection lost!"))
        } else {
            for (x in 1..count) {
                subscriber.onNext(x)
            }
            subscriber.onCompleted()
        }
    }
}