package com.vaccae.roomdemo

import android.Manifest
import android.content.pm.PackageManager
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.database.getStringOrNull
import androidx.lifecycle.Observer
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vaccae.roomdemo.bean.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.lang.Exception
import java.lang.StringBuilder
import java.util.*
import kotlin.collections.ArrayList
import java.util.Observer as Observer1

class MainActivity : AppCompatActivity() {

    val btn: Button by lazy { findViewById<Button>(R.id.btn) }
    val btn1: Button by lazy { findViewById<Button>(R.id.btn1) }
    val btn2: Button by lazy { findViewById<Button>(R.id.btn2) }


    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CHANGE_NETWORK_STATE,
            Manifest.permission.INTERNET
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {

            } else {
                Toast.makeText(this, "未开启权限.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        allPermissionsGranted()

        if (CheckProduct() == 0) {
            //生成显示产品数据
            CreateProduct()

            //生成明细数据
            CreateProductItem()
        }

        InitListen()

        btn.setOnClickListener {
            //加载AppDataBase
            val db = DbUtil().getDatabase(this);
            //显示出来
            val list = db.ProductDao().getAll()
            tvshow.text = ""
            list.forEach {
                tvshow.append(
                    it.code + " " + it.name
                            + " " + it.unit + " " + it.price + "\r\n"
                )
            }
        }

        btn1.setOnClickListener {
            try {
                val ipadr = "tcp://" + PhoneAdrUtil.getIpAdr(this) + ":8517"
                tvshow.append(ipadr + "\r\n")
                var isopen = VNanoNNPairUtils.getInstance().IsRecvListen()
                if (!isopen) {
                    VNanoNNPairUtils.getInstance().Bind(ipadr).StartRecvListen()
                } else {
                    VNanoNNPairUtils.getInstance().StopRecvListen()
                    VNanoNNPairUtils.getInstance().UnBind()
                }
                isopen = VNanoNNPairUtils.getInstance().IsRecvListen()
                var str = if (!isopen) {
                    "监听服务未开启"
                } else "开启监听服务"
                tvshow.append(str + "\r\n")
            } catch (e: Exception) {
                tvshow.append(e.message + "\r\n")
            }
        }

        btn2.setOnClickListener { copydatabase() }
    }

    private fun InitListen() {
        LiveEventBus.get<String>("NNPair")
            .observe(this, Observer {

                try {
                    tvshow.append(it + "\r\n")
                    val transparm = it.split("#")
                    when (transparm[0]) {
                        CTransStatus.GetDBName -> getDataBase()
                        CTransStatus.TransDB -> {
                            TransDataBase(transparm[1])
                        }
                        CTransStatus.ExecSql -> {
                            ExecSql(transparm[1])
                        }
                        else -> {
                            tvshow.append("找不到对应的通讯类型\r\n")
                        }
                    }
                } catch (e: Exception) {
                    tvshow.append(e.message + "\r\n")
                }
            })
    }

    private fun ExecSql(sql: String) {
        val exectype = if (sql.trimStart().startsWith("select")) {
            1
        } else {
            2
        }
        //加载AppDataBase
        val db = DbUtil().getDatabase(this);
        val execsqlScope = CoroutineScope(Job())
        execsqlScope.launch(Dispatchers.IO) {
            try {
                when (exectype) {
                    1 -> {
                        val cursor = db.openHelper.writableDatabase.query(sql)
                        cursor?.let {
                            val sb = StringBuilder()
                            //生成对应列名
                            val columnqty = it.columnCount
                            for (i in 0 until columnqty) {
                                sb.append("[${it.columnNames[i]}]").append(",")
                            }

                            sb.deleteCharAt(sb.lastIndexOf(","))
                            sb.append("\r\n")

                            //生成对应数据
                            it.moveToFirst()
                            while (it.moveToNext()) {
                                for (i in 0 until columnqty) {
                                    when (it.getType(i)) {
                                        Cursor.FIELD_TYPE_STRING -> {
                                            sb.append(it.getString(i))
                                        }
                                        Cursor.FIELD_TYPE_INTEGER -> {
                                            sb.append(it.getInt(i))
                                        }
                                        Cursor.FIELD_TYPE_FLOAT -> {
                                            sb.append(it.getFloat(i))
                                        }
                                        Cursor.FIELD_TYPE_BLOB -> {
                                            sb.append(it.getBlob(i))
                                        }
                                        else -> {
                                            sb.append(it.getString(i))
                                        }
                                    }
                                    sb.append(",")
                                }
                                sb.deleteCharAt(sb.lastIndexOf(","))
                                sb.append("\r\n")
                            }

                            //传输数据
                            VNanoNNPairUtils.getInstance().Send(sb.toString().toByteArray())
                            withContext(Dispatchers.Main) {
                                tvshow.append("查询数据完成发送\r\n")
                            }
                        }
                    }
                    else -> {
                        db.runInTransaction {
                            db.openHelper.writableDatabase.execSQL(sql)
                        }
                        //传输数据
                        VNanoNNPairUtils.getInstance().Send("更新完成".toByteArray())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    tvshow.append(e.message + "\r\n")
                }
            }
        }
    }

    private fun TransDataBase(dbname: String) {
        val filename = applicationContext.filesDir.path + "/" + dbname
        VNanoNNPairUtils.getInstance().Send(File(filename))
        tvshow.append("发送数据库文件：$filename \r\n")
    }

    private fun getDataBase() {
        //先将数据库文件拷贝到程序目录的file下
        val dbfilelist = copydatabase()
        var sendstr = dbfilelist.joinToString("#")
        //传输数据库文件
        VNanoNNPairUtils.getInstance().Send(sendstr.toByteArray())
        tvshow.append("数据库文件sendstr\r\n")

    }

    private fun copydatabase(): MutableList<String> {

        val strs = applicationContext.databaseList()
        for (str in strs) {
            //找到文件的路径  /data/data/包名/databases/数据库名称
            val dbFile = applicationContext.getDatabasePath(str)
            val dstFile = applicationContext.filesDir.path + "/" + str
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
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                //关闭数据流
                try {
                    fos?.close()
                    fis?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
        return strs.toMutableList()
    }


    private fun CreateProductItem() {
        //定义明细列表
        val itemlist = ArrayList<ProductItem>()

        //加载AppDataBase
        val db = DbUtil().getDatabase(this);
        //显示所有Product的明细
        val list = db.ProductDao().getAll()

        list.forEach {
            for (i in 1..3) {
                val item = ProductItem()
                item.code = it.code
                item.barcode = it.code + i.toString()
                item.qty = 1
                itemlist.add(item)
            }
        }
        db.ProductItemDao().add(itemlist)

        //显示明细
        val getlist = db.ProductItemDao().getAll()
        tvshow.text = ""
        getlist.forEach {
            tvshow.append(
                it.code + " " + it.barcode
                        + " " + it.qty + "\r\n"
            )
        }
    }

    private fun CheckProduct(): Int {
        //加载AppDataBase
        val db = DbUtil().getDatabase(this);
        return db.ProductDao().getAll().size
    }

    private fun CreateProduct() {
        //加载AppDataBase
        val db = DbUtil().getDatabase(this);
        for (i in 1..5) {
            val item = Product()
            item.code = "0000$i"
            item.name = "产品$i"
            item.unit = "套"
            item.price = 99f
            //写入数据
            db.ProductDao().add(item)
        }

    }
}
