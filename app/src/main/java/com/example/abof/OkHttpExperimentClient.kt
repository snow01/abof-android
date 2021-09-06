package com.example.abof

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import java.io.IOException
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy


val JSON = "application/json; charset=utf-8".toMediaType()

class OkHttpExperimentClient constructor() : ExperimentRunnerClient {
    override fun runExperiment(
        repository: AbofExperimentRepository
    ) {
        val client: OkHttpClient = OkHttpClient.Builder()
            .eventListener(PrintingEventListener(repository))
            .dispatcher(Dispatcher())
            .build()

        val body: RequestBody = repository.getNewRequestBody().toRequestBody(JSON)

        val request = Request.Builder()
            .url(BuildConfig.ABOF_API_URL)
            .post(body)
            .build()

        repository.setStartTime(System.currentTimeMillis())

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

//                    for ((name, value) in response.headers) {
//                        println("$name: $value")
//                    }

                    repository.experimentResponse.postValue(repository.gson.fromJson(response.body!!.charStream(), ExperimentResponse::class.java))
                    repository.setTotalTimeTaken(System.currentTimeMillis())
                }
            }
        })
    }
}

class PrintingEventListener constructor(private val repository: AbofExperimentRepository) : EventListener() {
    private fun printEvent(kind: String) {
        repository.addGauge(kind, System.currentTimeMillis())
    }

    override fun callEnd(call: Call) {
        printEvent("callEnd")
    }

    override fun callStart(call: Call) {
        printEvent("callStart")
    }

    override fun connectEnd(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy, protocol: Protocol?) {
        printEvent("connectEnd")
    }

    override fun connectStart(call: Call, inetSocketAddress: InetSocketAddress, proxy: Proxy) {
        printEvent("connectStart")
    }

    override fun connectionAcquired(call: Call, connection: Connection) {
        printEvent("connectionAcquired")
    }

    override fun connectionReleased(call: Call, connection: Connection) {
        printEvent("connectionReleased")
    }

    override fun dnsEnd(call: Call, domainName: String, inetAddressList: List<InetAddress>) {
        printEvent("dnsEnd")
    }

    override fun dnsStart(call: Call, domainName: String) {
        printEvent("dnsStart")
    }

    override fun requestBodyEnd(call: Call, byteCount: Long) {
        printEvent("requestBodyEnd")
    }

    override fun requestBodyStart(call: Call) {
        printEvent("requestBodyStart")
    }

    override fun requestHeadersEnd(call: Call, request: Request) {
        printEvent("requestHeadersEnd")
    }

    override fun requestHeadersStart(call: Call) {
        printEvent("requestHeadersStart")
    }

    override fun responseBodyEnd(call: Call, byteCount: Long) {
        printEvent("responseBodyEnd")
    }

    override fun responseBodyStart(call: Call) {
        printEvent("responseBodyStart")
    }

    override fun responseHeadersEnd(call: Call, response: Response) {
        printEvent("responseHeadersEnd")
    }

    override fun responseHeadersStart(call: Call) {
        printEvent("responseHeadersStart")
    }
}