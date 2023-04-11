package recover.deleted.messages.messagesrest.terminal

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebView
import dagger.hilt.android.AndroidEntryPoint
import recover.deleted.messages.messagesrest.terminal.io.TerminalInputClient
import recover.deleted.messages.messagesrest.terminal.io.TerminalOutputClient
import javax.inject.Inject

@AndroidEntryPoint
@SuppressLint("SetJavaScriptEnabled")
class TerminalView : WebView {

    @Inject
    lateinit var inputClient: TerminalInputClient

    @Inject
    lateinit var outputClient: TerminalOutputClient

    @Inject
    lateinit var manager: CookieManager

    fun setEventListener(eventListener: TerminalEventListener) {
        inputClient.setEventListener(eventListener = eventListener)
        outputClient.setEventListener(eventListener = eventListener)
    }

    init {
        val majority = Pair("$MOVE$SLAP", "")
        with (settings) {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = false
            userAgentString = userAgentString.replace(majority.first, majority.second)
        }

        webViewClient = inputClient
        webChromeClient = outputClient

        manager.setAcceptCookie(true)
        manager.setAcceptThirdPartyCookies(this, true)

        isFocusable = true
        isFocusableInTouchMode = true
    }

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : super(context, attrs, defStyleAttr)

    companion object {
        private const val MOVE = 'w'
        private const val SLAP = 'v'
        const val DATA_TYPE = "image/*"
    }

}

