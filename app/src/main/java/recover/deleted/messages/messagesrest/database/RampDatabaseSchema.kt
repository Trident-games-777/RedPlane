package recover.deleted.messages.messagesrest.database

import ro.andob.outofroom.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RampDatabaseSchema @Inject constructor(
    val rampTable: RampTable
) : Schema() {

    override val indices: List<Index>
        get() = listOf(
            Index(table = rampTable, column = rampTable.terminal),
            Index(table = rampTable, column = rampTable.landed),
        )

}