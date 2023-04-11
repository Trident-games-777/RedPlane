package recover.deleted.messages.messagesrest.terminal.io

import android.net.Uri
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import recover.deleted.messages.messagesrest.terminal.TerminalEventListener
import javax.inject.Inject

class TerminalOutputClient @Inject constructor() : WebChromeClient() {

    private var eventListener: TerminalEventListener? = null

    fun setEventListener(eventListener: TerminalEventListener) {
        this.eventListener = eventListener
    }

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallback: ValueCallback<Array<Uri>>?,
        fileChooserParams: FileChooserParams?
    ): Boolean {
        return eventListener?.let { tel ->
            tel.browseDirectory(filePathCallback)
            true
        } ?: false
    }

}