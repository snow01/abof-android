package com.example.abof

import android.content.Context
import android.util.Log
import org.chromium.net.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.Reader
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.concurrent.Executor
import java.util.concurrent.Executors


class CronetClient constructor(context: Context) : ExperimentRunnerClient {
    private val cronetEngine: CronetEngine =
        CronetEngine.Builder(context).enableHttp2(true).enableBrotli(true).build()
    private val executor: Executor = Executors.newSingleThreadExecutor()

    override fun runExperiment(
        repository: AbofExperimentRepository
    ) {
        val requestBuilder = cronetEngine.newUrlRequestBuilder(
            BuildConfig.ABOF_API_URL,
            RequestCallback(repository),
            executor
        )

        requestBuilder.setHttpMethod("POST");
        requestBuilder.addHeader("Content-Type", "application/json");
        requestBuilder.addHeader("Accept", "application/json");

        val bytes: ByteArray = convertStringToBytes(repository.getNewRequestBody())
        val uploadDataProvider = UploadDataProviders.create(bytes)
        requestBuilder.setUploadDataProvider(uploadDataProvider, executor)

        val request: UrlRequest = requestBuilder.build()
        Log.i("CronetClient", "REQUEST-STARTED = %d".format(System.currentTimeMillis()))

        request.start()
    }
}

class RequestCallback constructor(
    private val repository: AbofExperimentRepository
) : UrlRequest.Callback() {

    private lateinit var byteArrayOutputStream: ByteArrayOutputStream
//    private lateinit var bb: ByteBuffer

    override fun onRedirectReceived(
        request: UrlRequest?,
        info: UrlResponseInfo?,
        newLocationUrl: String?
    ) {
        request?.followRedirect()
    }

    override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
        Log.i("CronetClient", "RESPONSE-STARTED = %d".format(System.currentTimeMillis()))

        // TODO: get other headers

        val contentLength = getContentLength(info)
        Log.i("CronetClient", "CONTENT-LENGTH = %d".format(contentLength))

        byteArrayOutputStream = ByteArrayOutputStream(contentLength)

        // read first
        request?.read(ByteBuffer.allocateDirect(1024))
    }

    override fun onReadCompleted(
        request: UrlRequest?,
        info: UrlResponseInfo?,
        byteBuffer: ByteBuffer?
    ) {
        Log.i("CronetClient", "READ-COMPLETE = %d".format(System.currentTimeMillis()))

        byteBuffer?.flip();

        Log.i("CronetClient", "PRE-READ-BYTES = %s".format(byteArrayOutputStream.toString("utf-8")))
        Log.i("CronetClient", "INPUT-BYTES = %s".format(StandardCharsets.UTF_8.decode(byteBuffer).toString()))

        byteArrayOutputStream.write(byteBuffer?.array())

        Log.i("CronetClient", "READ-BYTES = %s".format(byteArrayOutputStream.toString("utf-8")))

        // read, so clear
        byteBuffer?.clear();

        // read more
        request?.read(byteBuffer);
    }

    override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
        Log.i("CronetClient", "SUCCEEDED = %d".format(System.currentTimeMillis()))
        val response = byteArrayOutputStream.toString("utf-8")
        Log.i("CronetClient", "RESPONSE = %s".format(response))
        repository.experimentResponse.value = repository.gson.fromJson(response, ExperimentResponse::class.java)
    }

    override fun onFailed(request: UrlRequest?, info: UrlResponseInfo?, error: CronetException?) {
        Log.i("CronetClient", "FAILED = %d".format(System.currentTimeMillis()))
    }

}

private fun getContentLength(urlResponseInfo: UrlResponseInfo?): Int {
    println("All headers = %s".format(urlResponseInfo?.allHeaders))

    val content = urlResponseInfo?.allHeaders?.get("Content-Length")
    return content?.get(0)?.toInt() ?: 1024
}

private fun convertStringToBytes(payload: String): ByteArray {
    val bytes: ByteArray
    val byteBuffer = ByteBuffer.wrap(payload.toByteArray())
    if (byteBuffer.hasArray()) {
        bytes = byteBuffer.array()
    } else {
        bytes = ByteArray(byteBuffer.remaining())
        byteBuffer[bytes]
    }
    return bytes
}