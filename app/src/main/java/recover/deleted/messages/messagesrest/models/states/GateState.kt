package recover.deleted.messages.messagesrest.models.states

import recover.deleted.messages.messagesrest.terminal.TerminalConfigs

sealed interface GateState {
    class Success(val terminal: TerminalConfigs) : GateState
    class Error(val message: String) : GateState
    object Initial : GateState
}