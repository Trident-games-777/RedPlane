package recover.deleted.messages.messagesrest.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RampDatabaseOpenHelper @Inject constructor(
    private val schema : RampDatabaseSchema,
    @ApplicationContext context: Context
) : SQLiteOpenHelper(
    context,
    DB_NAME,
    null,
    DB_VERSION,
    { throw Error("Detected a corrupt database!") }
) {
    override fun onCreate(db : SQLiteDatabase) {
        for (table in schema.tables)
            db.execSQL(table.toCreateTableSQL())

        for (index in schema.indices)
            db.execSQL(index.toCreateIndexSQL())
    }

    override fun onConfigure(db : SQLiteDatabase) {
        super.onConfigure(db)
        db.enableWriteAheadLogging()
    }

    override fun onUpgrade(db : SQLiteDatabase?, oldVersion : Int, newVersion : Int) {

    }

    companion object {
        private const val DB_NAME = "ramp.db"
        private const val DB_VERSION = 1
    }
}