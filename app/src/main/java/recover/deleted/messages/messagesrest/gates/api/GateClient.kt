package recover.deleted.messages.messagesrest.gates.api

import okhttp3.MultipartBody
import retrofit2.Call
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GateClient @Inject constructor(
    private val gateApi: GateApi
) {

    fun postDocuments(
        documents: MultipartBody.Part
    ): Call<String> =
        gateApi.postDocuments(documents = documents)

}