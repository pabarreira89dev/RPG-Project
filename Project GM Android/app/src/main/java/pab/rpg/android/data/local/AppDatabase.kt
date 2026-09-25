package pab.rpg.android.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ChatMessageEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun chatMessageDao(): ChatMessageDao

    companion object {
        // Local cache only (backend is the source of truth), so destroying it on schema changes is fine.
        private const val DATABASE_NAME = "project_gm.db"

        lateinit var instance: AppDatabase
            private set

        fun init(context: Context) {
            if (::instance.isInitialized) return
            instance = Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration(true)
                .build()
        }
    }
}
