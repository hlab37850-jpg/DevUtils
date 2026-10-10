package com.devtools.utils

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Small GitHub REST client. The caller supplies a token for each operation;
 * credentials are not persisted by this class.
 */
class GitHubControlClient(private val token: String) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(25, TimeUnit.SECONDS)
        .callTimeout(30, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private fun request(url: String): Request.Builder = Request.Builder()
        .url(url)
        .header("Accept", "application/vnd.github+json")
        .header("X-GitHub-Api-Version", "2022-11-28")
        .header("Authorization", "Bearer $token")
        .header("User-Agent", "DevUtils-Android")

    private fun execute(request: Request): String {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = runCatching { JSONObject(body).optString("message") }.getOrDefault("")
                throw IOException("GitHub API ${response.code}: ${message.ifBlank { response.message }}")
            }
            return body
        }
    }

    fun listRoot(repository: String, branch: String): JSONArray {
        val encodedBranch = java.net.URLEncoder.encode(branch, Charsets.UTF_8.name())
        val body = execute(request("https://api.github.com/repos/$repository/contents?ref=$encodedBranch").get().build())
        return JSONArray(body)
    }

    fun listWorkflowRuns(repository: String, branch: String): JSONObject {
        val encodedBranch = java.net.URLEncoder.encode(branch, Charsets.UTF_8.name())
        val body = execute(request("https://api.github.com/repos/$repository/actions/runs?branch=$encodedBranch&per_page=5").get().build())
        return JSONObject(body)
    }

    fun dispatchBuild(repository: String, branch: String) {
        val body = JSONObject().put("ref", branch).toString().toRequestBody(jsonType)
        execute(request("https://api.github.com/repos/$repository/actions/workflows/build.yml/dispatches")
            .post(body).build())
    }

    fun readTextFile(repository: String, path: String, branch: String): JSONObject {
        val encodedPath = path.split("/").joinToString("/") {
            java.net.URLEncoder.encode(it, Charsets.UTF_8.name()).replace("+", "%20")
        }
        val encodedBranch = java.net.URLEncoder.encode(branch, Charsets.UTF_8.name())
        val body = execute(request("https://api.github.com/repos/$repository/contents/$encodedPath?ref=$encodedBranch").get().build())
        return JSONObject(body)
    }

    fun updateTextFile(repository: String, path: String, branch: String, message: String, content: String, sha: String) {
        val payload = JSONObject()
            .put("message", message)
            .put("content", android.util.Base64.encodeToString(content.toByteArray(Charsets.UTF_8), android.util.Base64.NO_WRAP))
            .put("sha", sha)
            .put("branch", branch)
            .toString()
            .toRequestBody(jsonType)
        val encodedPath = path.split("/").joinToString("/") {
            java.net.URLEncoder.encode(it, Charsets.UTF_8.name()).replace("+", "%20")
        }
        execute(request("https://api.github.com/repos/$repository/contents/$encodedPath").put(payload).build())
    }
}
