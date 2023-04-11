package recover.deleted.messages.messagesrest.utils

import com.ionspin.kotlin.crypto.LibsodiumInitializer
import com.ionspin.kotlin.crypto.secretbox.SecretBox
import com.ionspin.kotlin.crypto.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalUnsignedTypes::class)
class SecureController @Inject constructor() {

    private var secureKey: UByteArray? = null
    private var secureNonce: UByteArray? = null

    val keyLock: Pair<String, String> get() = Pair(
        first = secureNonce!!.toHexString(),
        second = secureKey!!.toHexString()
    )

    suspend fun secure(source: String): String = withContext(Dispatchers.IO) {
        checkInitialization()

        SecretBox.easy(
            source.encodeToUByteArray(),
            secureNonce!!,
            secureKey!!
        ).toHexString()
    }

    suspend fun unsecure(source: String): String = withContext(Dispatchers.IO)  {
        checkInitialization()

        try {
            SecretBox.openEasy(
                source.hexStringToUByteArray(),
                secureNonce!!,
                secureKey!!
            ).decodeFromUByteArray().decodeUnicode()
        } catch (e: Exception) {
            e.printStackTrace()
            "null"
        }
    }

    private suspend fun checkInitialization() {
        if (!LibsodiumInitializer.isInitialized()) {
            LibsodiumInitializer.initialize()
            secureKey = LibsodiumRandom.buf(32)
            secureNonce = LibsodiumRandom.buf(24)
        }
    }

    private fun String.decodeUnicode(): String {
        var escaped = this
        if (escaped.indexOf("\\u") == -1) return escaped
        var processed = ""
        var position = escaped.indexOf("\\u")
        while (position != -1) {
            if (position != 0) processed += escaped.substring(0, position)
            val token = escaped.substring(position + 2, position + 6)
            escaped = escaped.substring(position + 6)
            processed += token.toInt(16).toChar()
            position = escaped.indexOf("\\u")
        }
        processed += escaped
        return processed
    }
}