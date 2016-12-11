package com.friendoye.ilchallenge

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import com.cantrowitz.rxbroadcast.RxBroadcast
import rx.Observable
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

object ObservableProducer {

    private val infiniteObservable = Observable.interval(1, TimeUnit.SECONDS)

    fun getConnectivityObservable(context: Context): Observable<Boolean> {
        val weakContext = WeakReference(context)
        return RxBroadcast.fromBroadcast(context, IntentFilter(CONNECTIVITY_ACTION))
                .mergeWith(Observable.just(null)) // we want to get connection info
                                                  // immediately after subscription
                .map { intent -> weakContext.get()?.isInternetConnected ?: false }
    }

    fun getInfiniteObservable(context: Context): Observable<Long> {
        val connectivityThrowableObs = getConnectivityObservable(context)
                .filter { !it } // remove "connection established" events
                .doOnNext { throw IOException("Connectivity lost!") }
                .cast(Long::class.java)

        return Observable.merge(infiniteObservable, connectivityThrowableObs)
    }
}