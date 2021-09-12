package com.vaccae.roomdemo

import android.R.attr
import com.jeremyliao.liveeventbus.LiveEventBus
import com.vaccae.vnanomsg.NNPAIR
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.lang.Exception
import android.R.attr.path

import android.R.string.no


/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 09:31
 * 功能模块说明：
 */
object VNanoNNPairUtils {

    private var mNNPAIR: NNPAIR? = null

    private var isOpenListen = false;

    fun IsRecvListen(): Boolean {
        return isOpenListen
    }

    fun getInstance(): VNanoNNPairUtils {
        mNNPAIR ?: run {
            synchronized(VNanoNNPairUtils::class.java) {
                mNNPAIR = NNPAIR()
            }
        }
        return VNanoNNPairUtils
    }

    fun Bind(ipadr: String): VNanoNNPairUtils {
        mNNPAIR?.let {
            //var ipstr = "tcp://192.168.10.155:8157"
            it.bind(ipadr)
        }
        return VNanoNNPairUtils
    }

    fun UnBind() {
        mNNPAIR?.let {
            it.shutdownbind()
        }
    }

    private fun byteMerger(bt1: ByteArray, bt2: ByteArray): ByteArray {
        val bt3 = ByteArray(bt1.size + bt2.size)
        System.arraycopy(bt1, 0, bt3, 0, bt1.size)
        System.arraycopy(bt2, 0, bt3, bt1.size, bt2.size)
        return bt3
    }

    fun Send(file: File) {
        mNNPAIR?.let {
            var filebytearray = ByteArray(0)
            var len = 0;
            var byteArray = ByteArray(1024)
            val inputStream: FileInputStream = FileInputStream(file)

            //判断是否读到文件末尾
            while (inputStream.read(byteArray).also { len = it } != -1) {
                //将文件循环写入fielbytearray
                filebytearray = byteMerger(filebytearray, byteArray)
            }

            it.send(filebytearray)
        }
    }

    fun Send(byte: ByteArray) {
        mNNPAIR?.let { it.send(byte) }
    }


    fun StartRecvListen() {
        mNNPAIR?.let {
            isOpenListen = true;
            val recvScope = CoroutineScope(Job())
            recvScope.launch {
                try {
                    withContext(Dispatchers.IO) {
                        while (isOpenListen) {
                            delay(50)
                            val recvstr = it.recv()
                            recvstr?.let {
                                LiveEventBus.get("NNPair", String::class.java)
                                    .postOrderly(it)
                            }
                        }
                    }
                } catch (e: Exception) {
                    throw e
                }
            }
        }
    }


    fun StopRecvListen() {
        isOpenListen = false;
    }


}