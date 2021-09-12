package com.vaccae.roomdemo.bean

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-04-14 14:08
 * 功能模块说明：
 */
@Entity(tableName = "Head")
class Product {

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "Code")
    lateinit var code: String

    @ColumnInfo(name = "Name")
    lateinit var name: String

    @ColumnInfo(name = "Unit")
    lateinit var unit: String

    @ColumnInfo(name = "Price")
    var price: Float = 0f

}