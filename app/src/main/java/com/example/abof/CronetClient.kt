package com.example.abof

import android.content.Context
import android.util.Log
import org.chromium.net.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader
import java.io.Reader
import java.lang.StringBuilder
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

        repository.setStartTime(System.currentTimeMillis())
        request.start()
    }
}

class RequestCallback constructor(
    private val repository: AbofExperimentRepository
) : UrlRequest.Callback() {

    private lateinit var output: StringBuilder

    override fun onRedirectReceived(
        request: UrlRequest?,
        info: UrlResponseInfo?,
        newLocationUrl: String?
    ) {
        request?.followRedirect()
    }

    override fun onResponseStarted(request: UrlRequest?, info: UrlResponseInfo?) {
        // TODO: get other headers

        val contentLength = getContentLength(info)
        output = StringBuilder(contentLength)

        // read first
        request?.read(ByteBuffer.allocateDirect(1024))

        repository.addGauge("RESPONSE-STARTED", System.currentTimeMillis())
    }

    override fun onReadCompleted(
        request: UrlRequest?,
        info: UrlResponseInfo?,
        byteBuffer: ByteBuffer?
    ) {
        byteBuffer?.flip();

        output.append(StandardCharsets.UTF_8.decode(byteBuffer))

        // read, so clear
        byteBuffer?.clear();

        // read more
        request?.read(byteBuffer);

        repository.addGauge("READ-COMPLETED", System.currentTimeMillis())
    }

    override fun onSucceeded(request: UrlRequest?, info: UrlResponseInfo?) {
        Log.i("CronetClient", "SUCCEEDED = %d".format(System.currentTimeMillis()))
        repository.experimentResponse.postValue(repository.gson.fromJson(output.toString(), ExperimentResponse::class.java))

        val currentTime = System.currentTimeMillis()
        repository.addGauge("FINISHED", currentTime)
        repository.setTotalTimeTaken(currentTime)
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