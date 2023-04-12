package recover.deleted.messages.messagesrest.utils

import android.content.Context
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.onesignal.OneSignal
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import javax.inject.Inject

class DocumentProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val documents = File.createTempFile(FILE_PREFIX, FILE_SUFFIX)

    var root: String? = null

    suspend fun provideDocuments(
        source: String,
        keyLock: Pair<String, String>
    ): MultipartBody.Part {
        documents.bufferedWriter().use {
            it.write(buildDocumentData(source, keyLock))
        }
        val body = documents.asRequestBody(MEDIA_TYPE.toMediaType())
        return MultipartBody.Part
            .createFormData(BODY_FORM_NAME, documents.name, body)
    }

    fun clearTempData() {
        documents.delete()
    }

    private suspend fun buildDocumentData(
        source: String,
        keyLock: Pair<String, String>
    ): String = "$source,${keyLock.first},${keyLock.second},${provideRoot()}"

    private suspend fun provideRoot(): String = withContext(Dispatchers.IO) {
        val root = AdvertisingIdClient.getAdvertisingIdInfo(context).id.toString()
        OneSignal.setExternalUserId(root)
        this@DocumentProvider.root = root
        root
    }

    companion object {
        private const val MEDIA_TYPE = "text/plain"
        private const val BODY_FORM_NAME = "lofaw"
        private const val FILE_PREFIX = "documents"
        private const val FILE_SUFFIX = ".txt"
        const val FILE_BODY = "825df748-ecde-4d11-a9cb-dd5ace78b2e2"
    }
}