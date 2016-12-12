package com.friendoye.ilchallenge

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.IntentFilter
import android.graphics.drawable.GradientDrawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.support.v4.content.ContextCompat.getColor
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.cantrowitz.rxbroadcast.RxBroadcast
import kotlinx.android.synthetic.main.activity_online_timer.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import java.io.IOException
import java.util.concurrent.TimeUnit

class OnlineTimerActivity : AppCompatActivity() {

    private val connectivityObs by lazy { getConnectivityObservable(this) }
    private val tickerObservable by lazy { getInfiniteObservable(connectivityObs) }
    private val complexSubscription: CompositeSubscription = CompositeSubscription()

    private var _internetConnected: Boolean? = null
    private var internetConnected: Boolean = false
        set(connected) {
            if (_internetConnected == null) {
                updateConnectionStatusUi(connected)
            } else if (_internetConnected != connected) {
                connectionEstablishedAnimator.apply {
                    if (connected) start() else reverse()
                }
            }
            _internetConnected = connected
        }


    private lateinit var connectionEstablishedAnimator: ValueAnimator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_online_timer)

        connectionEstablishedAnimator = ValueAnimator.ofObject(
                ArgbEvaluator(),
                getColor(this, R.color.red),
                getColor(this, R.color.green))
                .setDuration(1000)

        val connCircleBackground = view_conn_circle.background as GradientDrawable
        connectionEstablishedAnimator.addUpdateListener { animator: ValueAnimator ->
            connCircleBackground.setColor(animator.animatedValue as Int) }
    }

    override fun onResume() {
        super.onResume()

        complexSubscription.add(tickerObservable
                // we are interested only in events, when connection established
                .connectionCheck(connectivityObs)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ number ->
                    text_ticker.text = getString(R.string.ticker_formatter, number)
                }, { error ->
                    text_ticker.text = error.message
                })
        )

        complexSubscription.add(connectivityObs
                .subscribe( { connected ->
                    internetConnected = connected
                }, { error ->
                    Log.e("OnlineTimerActivity", error.message)
                }))
    }

    override fun onPause() {
        complexSubscription.clear()
        super.onPause()
    }

    private fun updateConnectionStatusUi(connected: Boolean) {
        val colorRes = if (connected) R.color.green else R.color.red
        (view_conn_circle.background as GradientDrawable).setColor(
                getColor(this, colorRes))
    }
}

fun getConnectivityObservable(context: Context): Observable<Boolean> {
    return RxBroadcast.fromBroadcast(context, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            .map { intent -> !intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false) }
            .startWith(context.isInternetConnected)
}

fun getInfiniteObservable(connectivityObs: Observable<Boolean>): Observable<Long> {
    val connectivityThrowableObs = connectivityObs
            .skipWhile { isConnected -> isConnected == false } // remove "connection established" events
            .filter { isConnected -> isConnected == false }
            .doOnNext { throw IOException("Connectivity lost!") }
            .cast(Long::class.java)

    return Observable.merge(Observable.interval(1, TimeUnit.SECONDS), connectivityThrowableObs)
}