package com.vaccae.roomdemo.bean

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-04-14 14:29
 * 功能模块说明：
 */
@Database(entities = [Product::class,ProductItem::class], version = 2, exportSchema = false)
abstract class AppDataBase : RoomDatabase() {
    abstract fun ProductDao(): ProductDao

    abstract fun ProductItemDao():ProductItemDao
}

class DbUtil {

    //数据库升级
    var migration1_2 = object : Migration(1, 2) {
        override fun migrate(database: SupportSQLiteDatabase) {
            val sql="CREATE TABLE if not exists Body(Code TEXT NOT NULL ," +
                    "BarCode TEXT NOT NULL,Qty INTEGER NOT NULL,PRIMARY KEY(Code,BarCode))"
            database.execSQL(sql)
        }
    }

    //创建单例
    private var INSTANCE: AppDataBase? = null

    fun getDatabase(context: Context): AppDataBase {
        if (INSTANCE == null) {
            synchronized(lock = AppDataBase::class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDataBase::class.java, "testdb"
                    )
                        .allowMainThreadQueries()//允许在主线程查询数据
                        .addMigrations(migration1_2)//数据库升级时执行
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
        }
        return INSTANCE!!
    }
}