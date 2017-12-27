/*
 * Copyright (c) 2017 Lizhaotailang
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.github.tonnyl.mango.database

import android.arch.persistence.db.SupportSQLiteDatabase
import android.arch.persistence.room.Database
import android.arch.persistence.room.Room
import android.arch.persistence.room.RoomDatabase
import android.arch.persistence.room.migration.Migration
import android.content.Context
import android.support.annotation.VisibleForTesting
import io.github.tonnyl.mango.data.AccessToken
import io.github.tonnyl.mango.data.User
import io.github.tonnyl.mango.database.dao.AccessTokenDao
import io.github.tonnyl.mango.database.dao.UserDao

/**
 * Created by lizhaotailang on 2017/6/28.
 */

@Database(entities = [(AccessToken::class), (User::class)], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        private val DATABASE_NAME = "mango-db"

        private var INSTANCE: AppDatabase? = null

        private val lock = Any()

        @VisibleForTesting
        @JvmStatic
        private val MIGRATION_1_2 = object : Migration(1, 2) {

            override fun migrate(database: SupportSQLiteDatabase) {
                val creationSQL = """
                    |CREATE TABLE IF NOT EXISTS `user_new` (
                    |`name` TEXT NOT NULL,
                    |`login` TEXT NOT NULL,
                    |`html_url` TEXT NOT NULL,
                    |`avatar_url` TEXT NOT NULL,
                    |`bio` TEXT NOT NULL,
                    |`location` TEXT,
                    |`can_upload_shot` INTEGER NOT NULL,
                    |`type` TEXT NOT NULL,
                    |`pro` INTEGER NOT NULL,
                    |`created_at` INTEGER NOT NULL,
                    |`id` INTEGER NOT NULL,
                    |`web` TEXT,
                    |`twitter` TEXT,
                    |PRIMARY KEY(`id`))
                """.trimMargin()
                // Create a temporary table
                database.execSQL(creationSQL)

                val insertionSQL = """
                    |INSERT INTO `user_new` (
                    |`name`, `username`, `html_url`, `avatar_url`, `bio`,
                    |`location`, `can_upload_shot`, `type`, `pro`, `created_at`,
                    |`id`, `web`, `twitter`, `id`)
                    |SELECT `name`, `username`, `html_url`, `avatar_url`, `bio`,
                    |`location`, `can_upload_shot`, `type`, `pro`, `created_at`,
                    |`id`, `web`, `twitter`, `id`
                    |FROM `user`
                    """.trimMargin()
                // Copy the data from the old table to temporary table
                database.execSQL(insertionSQL)

                // Remove the old table
                database.execSQL("DROP TABLE `user`")

                // Change the table name to the correct one
                database.execSQL("ALTER TABLE `user_new` RENAME TO `user`")
            }

        }

        fun getInstance(context: Context): AppDatabase {
            synchronized(lock) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext,
                            AppDatabase::class.java, DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build()
                }
                return INSTANCE as AppDatabase
            }
        }

    }

    abstract fun accessTokenDao(): AccessTokenDao

    abstract fun userDao(): UserDao
}