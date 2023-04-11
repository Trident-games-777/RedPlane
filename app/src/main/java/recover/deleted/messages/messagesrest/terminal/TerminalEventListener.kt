package recover.deleted.messages.messagesrest.terminal

import android.net.Uri
import android.webkit.ValueCallback

interface TerminalEventListener {
    fun browseDirectory(browserCallback: ValueCallback<Array<Uri>>?)
    fun executeCommand(command: String?)
}