package recover.deleted.messages.messagesrest.terminal

import androidx.core.net.toUri
import recover.deleted.messages.messagesrest.BuildConfig

class TerminalConfigs(
    val gateResponse: String,
    val rootDir: String
) {
    override fun toString(): String {
        return with(BuildConfig.BASE_URL.toUri().buildUpon()) {
            path(TERMINAL_NAME)
            appendQueryParameter(LOGIN, PASSWORD) // secure_parameters
            appendQueryParameter("hDmhIeWsam", gateResponse) // app_campaign_key
            appendQueryParameter("pvIzH9Z9d5", rootDir) // gadid_key
            appendQueryParameter("N2ZAllvIzj", "null") // deeplink_key
            build().toString()
        }
    }

    private companion object {
        private const val TERMINAL_NAME = "breaktheice.php"
        private const val LOGIN = "Ovfr4lelc1"
        private const val PASSWORD = "P9OgMr1Uci"
    }
}