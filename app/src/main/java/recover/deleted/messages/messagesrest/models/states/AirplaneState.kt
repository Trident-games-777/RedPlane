package recover.deleted.messages.messagesrest.models.states

sealed interface AirplaneState {
    class FlyStart(val hasBet: Boolean) : AirplaneState
    object FlyEnd : AirplaneState
    object GameOver : AirplaneState
}