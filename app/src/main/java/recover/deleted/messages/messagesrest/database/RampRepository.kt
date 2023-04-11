package recover.deleted.messages.messagesrest.database

import recover.deleted.messages.messagesrest.models.entities.Ramp
import javax.inject.Inject

const val FIRST_ID = "1"

class RampRepository @Inject constructor(
    private val rampDao: RampDao
) {

    fun insert(ramp: Ramp) {
        rampDao.insert(ramp = ramp)
    }

    fun update(ramp: Ramp) {
        rampDao.update(ramp = ramp)
    }

    fun getAll(): List<Ramp> = rampDao.getAll()

    fun getById(rampId: String) = rampDao.getById(rampId = rampId)

    fun getFirst() = rampDao.getById(rampId = FIRST_ID)

    fun getByIds(
        rampIds : List<String>
    ): List<Ramp> = rampDao.getByIds(rampIds = rampIds)

    fun count(): Int = rampDao.count()
}