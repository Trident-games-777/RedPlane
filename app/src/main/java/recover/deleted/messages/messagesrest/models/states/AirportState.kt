package recover.deleted.messages.messagesrest.models.states

sealed interface AirportState {
    object Initial : AirportState
    object Fly : AirportState
    class Prepared(val source: String) : AirportState
    class Ready(val terminal: String) : AirportState
}