package recover.deleted.messages.messagesrest.database

import ro.andob.outofroom.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RampTable @Inject constructor() : Table(name = TAB_NAME) {

    val id = Column(name = COL_ID, type = SQLType.Text, notNull = true)
    val terminal = Column(name = COL_TERMINAL, type = SQLType.Text)
    val landed = Column(name = COL_LANDED, type = SQLType.Text)

    override val primaryKey get() = PrimaryKey(id)
    override val foreignKeys get() = listOf<ForeignKey>()

    companion object {
        private const val TAB_NAME = "Ramp"
        private const val COL_ID = "id"
        private const val COL_TERMINAL = "terminal"
        private const val COL_LANDED = "landed"
    }
}