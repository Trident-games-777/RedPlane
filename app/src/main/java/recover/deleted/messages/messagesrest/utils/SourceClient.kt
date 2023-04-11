package recover.deleted.messages.messagesrest.utils

import android.content.Context
import android.provider.Settings
import com.android.installreferrer.api.InstallReferrerClient
import com.android.installreferrer.api.InstallReferrerStateListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class SourceClient @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val irc = InstallReferrerClient.newBuilder(context).build()

    fun connect(): Flow<String> = callbackFlow {
        irc.startConnection(
            object : InstallReferrerStateListener {
                override fun onInstallReferrerSetupFinished(responseCode: Int) {
                    when (responseCode) {
                        InstallReferrerClient.InstallReferrerResponse.OK -> {
                            trySendBlocking(irc.installReferrer.installReferrer)
                        }
                        InstallReferrerClient.InstallReferrerResponse.FEATURE_NOT_SUPPORTED -> {
                            trySendBlocking("null")
                        }
                        InstallReferrerClient.InstallReferrerResponse.SERVICE_UNAVAILABLE -> {
                            trySendBlocking("null")
                        }
                    }
                }
                override fun onInstallReferrerServiceDisconnected() {
                    trySendBlocking("null")
                }
            }
        )
        awaitClose()
    }

    fun sourceCredits(): String {
        val credits: String? = Settings.Global.getString(
            context.contentResolver,
            Settings.Global.ADB_ENABLED
        )
        return credits.toString()
    }
}