package com.friendoye.ilchallenge

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock

fun mockConnectionStatus(connectionResults: List<Boolean>): Context {
    val networkInfoMock = mock<NetworkInfo> {
        on { isConnected } doReturn connectionResults
    }
    val connectivityManagerMock = mock<ConnectivityManager> {
        on { activeNetworkInfo } doReturn networkInfoMock
    }
    return mock<Context> {
        on { getSystemService(Context.CONNECTIVITY_SERVICE)}
                .doReturn(connectivityManagerMock)
    }
}

fun mockConnectionStatus(connectionResult: Boolean)
        = mockConnectionStatus(listOf(connectionResult))