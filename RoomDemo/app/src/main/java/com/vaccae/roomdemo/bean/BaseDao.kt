package com.vaccae.roomdemo.bean

import androidx.room.*

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-04-16 15:29
 * 功能模块说明：
 */
@Dao
interface BaseDao<T> {
    @Transaction
    @Insert
    fun add(vararg arr:T)
    @Transaction
    @Insert
    fun add(arr:ArrayList<T>)

    @Transaction
    @Update
    fun upd(vararg arr:T)
    @Transaction
    @Update
    fun upd(arr:ArrayList<T>)

    @Transaction
    @Delete
    fun del(vararg arr:T)
    @Transaction
    @Delete
    fun del(arr:ArrayList<T>)
}