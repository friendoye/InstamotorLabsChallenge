package com.friendoye.ilchallenge

import android.content.Context
import android.content.IntentFilter
import android.net.ConnectivityManager.CONNECTIVITY_ACTION
import android.net.ConnectivityManager.EXTRA_NO_CONNECTIVITY
import com.cantrowitz.rxbroadcast.RxBroadcast
import rx.Observable
import java.io.IOException
import java.util.concurrent.TimeUnit

object ObservableProducer {

    private val infiniteObservable = Observable.interval(1, TimeUnit.SECONDS)

    fun getConnectivityObservable(context: Context): Observable<Boolean> {
        return RxBroadcast.fromBroadcast(context,
                IntentFilter(CONNECTIVITY_ACTION))
                .map { intent -> !intent.getBooleanExtra(EXTRA_NO_CONNECTIVITY, false) }
                .mergeWith(Observable.just(context.isInternetConnected))
    }

    fun getInfiniteObservable(context: Context): Observable<Long> {
        val connectivityObs = getConnectivityObservable(context)

        val connectivityThrowableObs: Observable<Long> = connectivityObs
                .map { connected ->
                    if (!connected) {
                        throw IOException("Connectivity lost!")
                    } else {
                        null
                    }
                }

        return Observable.merge(infiniteObservable, connectivityThrowableObs)
                .filter { it != null }
    }
}