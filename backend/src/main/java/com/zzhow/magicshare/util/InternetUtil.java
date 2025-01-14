package com.zzhow.magicshare.util;

import java.io.IOException;
import java.net.*;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author ZZHow
 * @date 2025/1/13
 */
public class InternetUtil {
    /**
     * 获取内网 IPv4 地址
     *
     * @return 内网 IPv4 地址
     */
    public static String getLocalIpAddress() {
        try {
            /*内网 IP 正则表达式
                A: 10.0.0.0 - 10.255.255.255
                B: 172.16.0.0 - 172.31.255.255
                C: 192.168.0.0 - 192.168.255.255
             */
            String privateIpRegex = "^(10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|172\\.(1[6-9]|2[0-9]|3[0-1])\\.\\d{1,3}\\.\\d{1,3}|192\\.168\\.\\d{1,3}\\.\\d{1,3})$";
            Pattern pattern = Pattern.compile(privateIpRegex);

            // 获取所有网络接口
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();

                // 跳过未启用或回环接口
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }

                // 获取当前网络接口的 IP 地址
                Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress instanceof Inet4Address && pattern.matcher(inetAddress.getHostAddress()).matches()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isPortInUse(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return false;
        } catch (IOException e) {
            return true;
        }
    }
}
