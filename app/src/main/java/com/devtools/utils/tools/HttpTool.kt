package com.devtools.utils.tools

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class HttpTool {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .callTimeout(45, TimeUnit.SECONDS)
        .build()

    fun executeRequest(url: String, method: String, body: String?, callback: (String) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            val result = withContext(Dispatchers.IO) {
                try {
                    require(url.startsWith("https://", true) || url.startsWith("http://", true)) {
                        "يجب أن يبدأ الرابط بـ http:// أو https://"
                    }
                    val normalizedMethod = method.uppercase()
                    require(normalizedMethod == "GET" || normalizedMethod == "POST") {
                        "طريقة الطلب المدعومة هي GET أو POST"
                    }
                    val requestBuilder = Request.Builder().url(url)
                    if (normalizedMethod == "POST") {
                        requestBuilder.post(body.orEmpty().toRequestBody("application/json; charset=utf-8".toMediaType()))
                    } else {
                        requestBuilder.get()
                    }
                    client.newCall(requestBuilder.build()).execute().use { response ->
                        val responseText = response.body?.string().orEmpty()
                        "HTTP ${response.code} ${response.message}\n\n$responseText"
                    }
                } catch (e: SocketTimeoutException) {
                    "خطأ الشبكة: انتهت مهلة الاتصال بالخادم."
                } catch (e: Exception) {
                    "خطأ الشبكة: ${e.message ?: "تعذر الاتصال بالخادم."}"
                }
            }
            callback(result)
        }
    }
}
