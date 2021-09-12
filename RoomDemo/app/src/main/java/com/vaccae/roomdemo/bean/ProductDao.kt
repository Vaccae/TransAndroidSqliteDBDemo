package com.vaccae.roomdemo.bean

import androidx.room.*

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-04-14 14:12
 * 功能模块说明：
 */
@Dao
interface ProductDao {
    @Transaction
    @Insert
    fun add(vararg arr:Product)
    @Transaction
    @Delete
    fun del(vararg arr:Product)
    @Transaction
    @Update
    fun upd(vararg arr:Product)

    @Query("select * from Head")
    fun getAll():List<Product>

    @Query("select * from Head where code=:input")
    fun getfromcode(input:String):Product?
}