package com.friendoye.ilchallenge

import android.content.Context
import rx.Observable
import java.io.IOException


fun <T> Observable<T>.connectionCheck(context: Context): Observable<T> {
    val connectionObservable = ObservableProducer.getConnectivityObservable(context)
            .filter { connected -> connected }
    return delaySubscription(connectionObservable)
            .retryWhen { it.flatMap({ error -> when(error) {
                // retry, if caught IOException (network isn't connected)
                is IOException -> Observable.just(null)
                // else - forward exception in chain
                else -> Observable.error<Throwable>(error as Throwable)
            }}) }
}