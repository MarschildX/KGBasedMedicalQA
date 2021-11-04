package com.example.healworld.utils;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;


public class AppUtil {

    public static void showToast(Context context, @StringRes int text, boolean isLong) {
        showToast(context, context.getString(text), isLong);
    }

    public static void showToast(Context context, String text, boolean isLong) {
        Toast.makeText(context, text, isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    /**
     * get device mac address
     */
    public static String getMacAddress(Context paramContext) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                String str = getMacMoreThanM(paramContext);
                if (!TextUtils.isEmpty(str))
                    return str;
            }
            // android 6.0 and lower access mac address from wifi
            WifiManager wifiManager = (WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null)
                return wifiInfo.getMacAddress();
        } catch (Throwable throwable) {}
        return null;
    }

    /**
     * get mac address of the devices that more than android Q
     */
    private static String getMacMoreThanM(Context paramContext) {
        try {
            //获取本机器所有的网络接口
            Enumeration enumeration = NetworkInterface.getNetworkInterfaces();
            while (enumeration.hasMoreElements()) {
                NetworkInterface networkInterface = (NetworkInterface)enumeration.nextElement();
                //获取硬件地址，一般是MAC
                byte[] arrayOfByte = networkInterface.getHardwareAddress();
                if (arrayOfByte == null || arrayOfByte.length == 0) {
                    continue;
                }

                StringBuilder stringBuilder = new StringBuilder();
                for (byte b : arrayOfByte) {
                    //格式化为：两位十六进制加冒号的格式，若是不足两位，补0
                    stringBuilder.append(String.format("%02X:", new Object[] { Byte.valueOf(b) }));
                }
                if (stringBuilder.length() > 0) {
                    //删除后面多余的冒号
                    stringBuilder.deleteCharAt(stringBuilder.length() - 1);
                }
                String str = stringBuilder.toString();
                // wlan0:无线网卡 eth0：以太网卡
                if (networkInterface.getName().equals("wlan0")) {
                    return str;
                }
            }
        } catch (SocketException socketException) {
            return null;
        }
        return null;
    }

}
