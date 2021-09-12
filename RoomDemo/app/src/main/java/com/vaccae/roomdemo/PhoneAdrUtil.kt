package com.vaccae.roomdemo

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import java.net.Inet4Address
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 13:14
 * 功能模块说明：
 */
class PhoneAdrUtil {

    companion object {

        fun getIpAdr(context: Context): String? {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            if (Build.VERSION.SDK_INT < 23) {
                val networiinfo = cm.activeNetworkInfo
                networiinfo?.let {
                    if (it.type == ConnectivityManager.TYPE_WIFI) {
                        return getWIfiIpAdr(context)
                    } else if (it.type == ConnectivityManager.TYPE_MOBILE) {
                        return getMobileIpAdr()
                    }
                }
            } else {
                val network = cm.activeNetwork
                network?.let { it ->
                    val networkCapabilities = cm.getNetworkCapabilities(it)
                    networkCapabilities?.let { item ->
                        if (item.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                            return getWIfiIpAdr(context)
                        }else if (item.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                            return getMobileIpAdr()
                        }
                    }
                }
            }

            return null
        }

        private fun getMobileIpAdr():String {
            var ipstr = ""
            val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf: NetworkInterface = en.nextElement()
                val enumIpAddr: Enumeration<InetAddress> = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress: InetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        ipstr = inetAddress.hostAddress.toString()
                        return ipstr
                    }
                }
            }
            return ipstr
        }

        private fun getWIfiIpAdr(context: Context):String{
            val wifiManager =
                context.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val wifiinfo = wifiManager.connectionInfo
            return ChangeIP2String(wifiinfo.ipAddress)
        }

        private fun ChangeIP2String(ip: Int): String {
            return "" + (ip and 0xFF) + "." +
                    ((ip shr 8) and 0xFF) + "." +
                    ((ip shr 16) and 0xFF) + "." +
                    (ip shr 24 and 0xFF);
        }
    }



}