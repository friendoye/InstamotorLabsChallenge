package com.friendoye.ilchallenge

import com.jakewharton.rxrelay.PublishRelay
import rx.Observable
import java.io.IOException

fun <T> Observable<T>.connectionCheck(connectionObservable: Observable<*>): Observable<T> {
    return delaySubscriptionIgnoringOnComplete(connectionObservable)
            .retryWhen { it.flatMap({ error -> when(error) {
                // retry, if caught IOException (network isn't connected)
                is IOException -> Observable.just(null)
                // else - forward exception in chain
                else -> Observable.error<Throwable>(error as Throwable)
            }}) }
}

fun <T> Observable<T>.delaySubscriptionIgnoringOnComplete(observable: Observable<*>): Observable<T> {
        return delaySubscription {
            val relay = PublishRelay.create<Any>()
            val subscription = observable.subscribe(relay)
            relay.doOnUnsubscribe { subscription.unsubscribe() }
        }
}