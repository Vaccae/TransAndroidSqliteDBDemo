package com.vaccae.roomdemo

import android.annotation.SuppressLint
import android.content.Context
import android.os.Environment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException


/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 18:22
 * 功能模块说明：
 */

class DbBackupUtil {

    private var mContext: Context? = null

    val path =
        Environment.getExternalStorageDirectory().absolutePath + File.separator + "RoomBackup" + File.separator

    private fun getInstance(context: Context) {
        mContext ?: run {
            synchronized(DbBackupUtil::class.java) {
                mContext = context
            }
        }
    }

    private fun createPath() {
        //安装包路径
        val updateDir = File(path)
        //创建文件夹
        if (!updateDir.exists()) {
            updateDir.mkdirs()
        }
    }

    suspend fun backup(context: Context): Flow<String> = flow {
        getInstance(context)
        createPath()
        mContext?.let {
            val strs = it.databaseList()
            emit("共${strs.size}个数据库文件，开始备份")

            for (str in strs) {
                emit("正在备份${str}数据库。。。")

                //找到文件的路径  /data/data/包名/databases/数据库名称
                val dbFile = it.getDatabasePath(str)
                //val dstFile = it.getExternalFilesDir("db").toString() + "/" + str

                val dstFile = path + str;

                var fis: FileInputStream? = null
                var fos: FileOutputStream? = null
                try {
                    //文件复制到sd卡中
                    fis = FileInputStream(dbFile)
                    fos = FileOutputStream(dstFile)
                    var len = 0
                    val buffer = ByteArray(2048)
                    while (-1 != fis.read(buffer).also({ len = it })) {
                        fos.write(buffer, 0, len)
                    }
                    fos.flush()
                    emit("${str}数据库备份完成。。。")
                } catch (e: Exception) {
                    throw e
                } finally {
                    //关闭数据流
                    try {
                        fos?.close()
                        fis?.close()
                    } catch (e: IOException) {
                        throw e
                    }
                }
            }
            emit("所有数据库备份完成")
        } ?: kotlin.run { throw Exception("未定义Context") }
    }

    suspend fun restore(context: Context): Flow<String> {
        return flow {
            getInstance(context)
            createPath()

            mContext?.let {
                //var dbfiles = it.getExternalFilesDir("db")
                var dbfiles = File(path)

                dbfiles.let { dbs ->
                    var files = dbs.listFiles()
                    if (files.isNotEmpty()) {
                        emit("共${files.size}个数据库文件，开始还原")

                        for (str in files) {
                            var dbFile = it.getDatabasePath(str.name)
                            dbFile.delete()

                            var fis: FileInputStream? = null
                            var fos: FileOutputStream? = null
                            try {
                                //文件复制到sd卡中
                                fis = FileInputStream(str)
                                fos = FileOutputStream(dbFile)
                                var len = 0
                                val buffer = ByteArray(2048)
                                while (-1 != fis.read(buffer).also({ len = it })) {
                                    fos.write(buffer, 0, len)
                                }
                                fos.flush()
                                emit("${str}数据库还原完成。。。")
                            } catch (e: Exception) {
                                throw e
                            } finally {
                                //关闭数据流
                                try {
                                    fos?.close()
                                    fis?.close()
                                } catch (e: IOException) {
                                    throw e
                                }
                            }
                        }
                        emit("所有数据库还原完成")
                    }
                }
            } ?: kotlin.run { throw Exception("未定义Context") }
        }
    }
}

