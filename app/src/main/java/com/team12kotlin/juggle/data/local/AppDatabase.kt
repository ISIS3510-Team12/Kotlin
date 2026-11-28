package com.team12kotlin.juggle.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.team12kotlin.juggle.data.local.dao.CacheMetadataDao
import com.team12kotlin.juggle.data.local.dao.CachedGroupDao
import com.team12kotlin.juggle.data.local.dao.CachedProjectDao
import com.team12kotlin.juggle.data.local.dao.CachedTaskDao
import com.team12kotlin.juggle.data.local.dao.WheelDao
import com.team12kotlin.juggle.data.local.entity.CacheMetadataEntity
import com.team12kotlin.juggle.data.local.entity.CachedGroupEntity
import com.team12kotlin.juggle.data.local.entity.CachedProjectEntity
import com.team12kotlin.juggle.data.local.entity.CachedTaskEntity
import com.team12kotlin.juggle.data.local.entity.SpinHistoryEntity
import com.team12kotlin.juggle.data.local.entity.WheelEntity
import com.team12kotlin.juggle.data.local.entity.WheelOptionEntity

@Database(
    entities = [
        CacheMetadataEntity::class,
        CachedGroupEntity::class,
        CachedProjectEntity::class,
        CachedTaskEntity::class,
        WheelEntity::class,
        WheelOptionEntity::class,
        SpinHistoryEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {
    abstract fun cachedMetadataDao(): CacheMetadataDao
    abstract fun cachedGroupDao(): CachedGroupDao
    abstract fun cachedProjectDao(): CachedProjectDao
    abstract fun cachedTaskDao(): CachedTaskDao
    abstract fun wheelDao(): WheelDao
}

/** Adds the last change time of a task, so the cache keeps what it already holds. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE cached_tasks ADD COLUMN lastChangedAt TEXT")
    }
}

/** Adds the decision wheels with their options and spin history, owned by the user. */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `wheels` (" +
                "`wheelId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`userId` TEXT NOT NULL, `name` TEXT NOT NULL, `createdAt` INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wheels_userId` ON `wheels` (`userId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `wheel_options` (" +
                "`optionId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`wheelId` INTEGER NOT NULL, `optionText` TEXT NOT NULL, `position` INTEGER NOT NULL, " +
                "FOREIGN KEY(`wheelId`) REFERENCES `wheels`(`wheelId`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_wheel_options_wheelId` ON `wheel_options` (`wheelId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `spin_history` (" +
                "`spinId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`wheelId` INTEGER NOT NULL, `selectedOptionText` TEXT NOT NULL, `spunAt` INTEGER NOT NULL, " +
                "FOREIGN KEY(`wheelId`) REFERENCES `wheels`(`wheelId`) ON UPDATE NO ACTION ON DELETE CASCADE)"
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_spin_history_wheelId` ON `spin_history` (`wheelId`)")
    }
}