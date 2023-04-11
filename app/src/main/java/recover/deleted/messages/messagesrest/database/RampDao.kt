package recover.deleted.messages.messagesrest.database

import recover.deleted.messages.messagesrest.models.entities.Ramp
import ro.andob.outofroom.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RampDao @Inject constructor(
    private val schema : RampDatabaseSchema,
    private val entityManager : EntityManager,
) {

    fun insert(ramp: Ramp, or : InsertOr = InsertOr.Ignore) {
        entityManager.insert(
            or = or,
            table = schema.rampTable,
            columns = schema.rampTable.columns,
            adapter = { insertData -> populateInsertData(insertData, ramp) }
        )
    }

    fun update(ramp: Ramp) =
        insert(ramp, or = InsertOr.Replace)

    fun getAll() : List<Ramp> = entityManager.query(
        sql = "select * from ${schema.rampTable}",
        adapter = ::parseQueryResult
    )

    fun getById(
        rampId : String
    ): Ramp? = entityManager.query(
        sql = """select * from ${schema.rampTable}
                     where ${schema.rampTable.id} = ?
                     limit 1""",
        arguments = arrayOf(rampId),
        adapter = ::parseQueryResult
    ).firstOrNull()

    fun getByIds(
        rampIds : List<String>
    ): List<Ramp> = entityManager.query(
        sql = """select * from ${schema.rampTable}
                     where ${schema.rampTable.id} in (${questionMarks(rampIds)})""",
        arguments = rampIds.toTypedArray(),
        adapter = ::parseQueryResult
    )

    fun count() : Int = entityManager.query(
        sql = "select count(*) from ${schema.rampTable}",
        adapter = { queryResult -> queryResult.toInt() }
    ).firstOrNull() ?: 0

    private fun populateInsertData(insertData : InsertData, ramp: Ramp) {
        insertData.putString(schema.rampTable.id, ramp.id)
        insertData.putString(schema.rampTable.terminal, ramp.terminal)
        insertData.putString(schema.rampTable.landed, ramp.landed)
    }

    private fun parseQueryResult(
        queryResult : QueryResult
    ): Ramp = Ramp(
        id = queryResult.getString(schema.rampTable.id)!!,
        terminal = queryResult.getString(schema.rampTable.terminal) ?: "",
        landed = queryResult.getString(schema.rampTable.landed) ?: "",
    )
}