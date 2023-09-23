package com.example.chat.`interface`

import com.example.chat.constant.Constants.Companion.CONTENT_TYPE
import com.example.chat.constant.Constants.Companion.SERVER_KEY
import com.example.chat.model.PushNotification
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationApi {

    @Headers("Authorization: key=$SERVER_KEY","Content_Type:$CONTENT_TYPE")
    @POST("fcm/send")
    suspend fun postNotification(
        @Body notification :PushNotification
    ):Response<ResponseBody>

}