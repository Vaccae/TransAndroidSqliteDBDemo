package com.vaccae.roomdemo.bean

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Query

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间：2020-04-16 15:28
 * 功能模块说明：
 */
@Entity(tableName = "Body", primaryKeys = ["Code", "BarCode"])
class ProductItem {
    @ColumnInfo(name = "Code")
    lateinit var code: String

    @ColumnInfo(name = "BarCode")
    lateinit var barcode: String

    @ColumnInfo(name = "Qty")
    var qty = 0
}

@Dao
interface ProductItemDao : BaseDao<ProductItem> {
    @Query("select * from Body")
    fun getAll(): List<ProductItem>
}