package recover.deleted.messages.messagesrest.terminal.io

import android.graphics.Bitmap
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import recover.deleted.messages.messagesrest.terminal.TerminalEventListener
import javax.inject.Inject

class TerminalInputClient @Inject constructor(
    private val manager: CookieManager
) : WebViewClient() {

    private var eventListener: TerminalEventListener? = null

    fun setEventListener(eventListener: TerminalEventListener) {
        this.eventListener = eventListener
    }

    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        Log.d("PageLoadStarted", "$url")
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)
        eventListener?.executeCommand(url)
        manager.flush()
    }

}