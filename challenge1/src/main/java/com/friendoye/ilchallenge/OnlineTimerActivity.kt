package com.friendoye.ilchallenge

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import kotlinx.android.synthetic.main.activity_online_timer.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription

class OnlineTimerActivity : AppCompatActivity() {

    private val tickerObservable: Observable<Long>
            by lazy { ObservableProducer.getInfiniteObservable(this) }
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
                ArgbEvaluator(), getColor(R.color.red), getColor(R.color.green))
                .setDuration(1000)

        val connCircleBackground = view_conn_circle.background as GradientDrawable
        connectionEstablishedAnimator.addUpdateListener { animator: ValueAnimator ->
            connCircleBackground.setColor(animator.animatedValue as Int) }
    }

    override fun onResume() {
        super.onResume()

        complexSubscription.add(tickerObservable
                .connectionCheck(this)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ number ->
                    text_ticker.text = getString(R.string.ticker_formatter, number)
                }, { error ->
                    text_ticker.text = error.message
                })
        )

        complexSubscription.add(ObservableProducer.getConnectivityObservable(this)
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
        (view_conn_circle.background as GradientDrawable)
                .setColor(getColor(colorRes))
    }
}
