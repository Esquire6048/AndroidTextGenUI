package com.example.textgenui.backend

import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import java.net.URI

object HTTPUtils {
    val myHeaders = Headers.Builder()
        .add("Content-Type", "application/json")
        .add("Accept", "application/json")
        .add("Authorization", "Bearer MOBILECOMPUTING2024") // API key
        .build()

    fun sendGETRequest(uri: URI): String {
        println("Sending HTTP GET request: $uri")
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .headers(myHeaders)
            .url(uri.toURL())
            .get()
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        assert(response.code == 200)
        //println("Status: ${response.statusCode()}, Response Body: >>>${response.body()}<<<")
        return response.body!!.string()
    }

    fun sendPOSTRequest(uri: URI, requestBodyJson: String): String {
        println("Sending HTTP POST request: $uri $requestBodyJson")
        val client = OkHttpClient()
        val request = okhttp3.Request.Builder()
            .headers(myHeaders)
            .url(uri.toURL())
            .post(requestBodyJson.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()
        val call = client.newCall(request)
        val response = call.execute()
        assert(response.code == 200)
        //val response: HttpResponse<String> = client.send(httpReq, HttpResponse.BodyHandlers.ofString())
        //println("Status: ${response.statusCode()}, Response Body: >>>${response.body()}<<<")
        return response.body!!.string()
    }
}