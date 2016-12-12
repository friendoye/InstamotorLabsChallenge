package com.friendoye.ilchallenge

import rx.Observable
import java.io.IOException

fun <T> Observable<T>.connectionCheck(connectivityObservable: Observable<Boolean>): Observable<T> {
    return delaySubscription { connectivityObservable.first { isConnected -> isConnected == true } }
            .retryWhen { it.flatMap({ error -> when(error) {
                // retry, if caught IOException (network isn't connected)
                is IOException -> Observable.just(null)
                // else - forward exception in chain
                else -> Observable.error<Throwable>(error as Throwable)
            }}) }
}