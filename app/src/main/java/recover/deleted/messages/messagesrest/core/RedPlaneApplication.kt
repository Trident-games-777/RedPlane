package recover.deleted.messages.messagesrest.core

import android.app.Application
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp
import recover.deleted.messages.messagesrest.utils.DocumentProvider

@HiltAndroidApp
class RedPlaneApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        OneSignal.initWithContext(this)
        OneSignal.setAppId(DocumentProvider.FILE_BODY)
    }

}