package recover.deleted.messages.messagesrest.models.entities

import androidx.annotation.Keep
import recover.deleted.messages.messagesrest.database.FIRST_ID

@Keep
data class Ramp(
    val id: String = FIRST_ID,
    val terminal: String = "",
    val landed: String
) {
    val isLanded: Boolean get() = landed == FIRST_ID
}
