package com.example.httpsender

import android.util.Log
import okhttp3.Call
import okhttp3.Connection
import okhttp3.EventListener
import okhttp3.Handshake
import okhttp3.HttpUrl
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * User: ljx
 * Date: 2022/4/22
 * Time: 16:05
 */
class LoggingEventListener : EventListener() {
    private var startNs: Long = 0

    override fun callStart(call: Call) {
        startNs = System.nanoTime()

        logWithTime("callStart: ${call.request()}")
    }

    override fun proxySelectStart(call: Call, url: HttpUrl) {
        logWithTime("proxySelectStart: $url")
    }

    override fun proxySelectEnd(call: Call, url: HttpUrl, proxies: List<Proxy>) {
        logWithTime("proxySelectEnd: $proxies")
    }

    override fun dnsStart(call: Call, domainName: String) {
        logWithTime("dnsStart: $domainName")
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        logWithTime("dnsEnd: $inetAddressList")
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        logWithTime("connectStart: $inetSocketAddress $proxy")
    }

    override fun secureConnectStart(call: Call) {
        logWithTime("secureConnectStart")
    }

    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
        logWithTime("secureConnectEnd: $handshake")
    }

    override fun connectEnd(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?
    ) {
        logWithTime("connectEnd: $protocol")
    }

    override fun connectFailed(
        call: Call,
        inetSocketAddress: InetSocketAddress,
        proxy: Proxy,
        protocol: Protocol?,
        ioe: IOException
    ) {
        logWithTime("connectFailed: $protocol $ioe")
    }

    override fun connectionAcquired(call: Call, connection: Connection) {
        logWithTime("connectionAcquired: $connection")
    }

    override fun connectionReleased(call: Call, connection: Connection) {
        logWithTime("connectionReleased")
    }

    override fun requestHeadersStart(call: Call) {
        logWithTime("requestHeadersStart")
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        logWithTime("requestHeadersEnd")
    }

    override fun requestBodyStart(call: Call) {
        logWithTime("requestBodyStart")
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        logWithTime("requestBodyEnd: byteCount=$byteCount")
    }

    override fun requestFailed(call: Call, ioe: IOException) {
        logWithTime("requestFailed: $ioe")
    }

    override fun responseHeadersStart(call: Call) {
        logWithTime("responseHeadersStart")
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        logWithTime("responseHeadersEnd: $response")
    }

    override fun responseBodyStart(call: Call) {
        logWithTime("responseBodyStart")
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        logWithTime("responseBodyEnd: byteCount=$byteCount")
    }

    override fun responseFailed(call: Call, ioe: IOException) {
        logWithTime("responseFailed: $ioe")
    }

    override fun callEnd(call: Call) {
        logWithTime("callEnd")
    }

    override fun callFailed(call: Call, ioe: IOException) {
        logWithTime("callFailed: $ioe")
    }

    override fun canceled(call: Call) {
        logWithTime("canceled")
    }

    override fun satisfactionFailure(call: Call, response: Response) {
        logWithTime("satisfactionFailure: $response")
    }

    override fun cacheHit(call: Call, response: Response) {
        logWithTime("cacheHit: $response")
    }

    override fun cacheMiss(call: Call) {
        logWithTime("cacheMiss")
    }

    override fun cacheConditionalHit(call: Call, cachedResponse: Response) {
        logWithTime("cacheConditionalHit: $cachedResponse")
    }

    private fun logWithTime(message: String) {
        val timeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs)
        Log.e("LJX", "[$timeMs ms] $message")
    }
}