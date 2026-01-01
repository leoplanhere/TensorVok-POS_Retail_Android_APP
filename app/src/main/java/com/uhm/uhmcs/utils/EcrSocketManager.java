package com.uhm.uhmcs.utils;

import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.Locale;


public class EcrSocketManager {

    private static final String LOG_TAG = "ECR_COMM";
    private static final AtomicLong ecnCounter = new AtomicLong(1);

    // 1. 获取下一个递增的 ECN (12位补零)
    private static String getNextEcn() {
        return String.format("%012d", ecnCounter.getAndIncrement());
    }

    // --- 命令构造工厂方法 ---

    public static byte[] statusCommand() {
        return EcrProtocol.buildStatusPacket(getNextEcn());
    }

    public static byte[] logonCommand() {
        return EcrProtocol.buildLogonPacket(getNextEcn());
    }

    public static byte[] creditCardSaleCommand(int amountInCents, String referenceNumber) {
        String formattedRef = padString(referenceNumber, 13, '0');
        // 这里需要 EcrProtocol 里实现 buildCreditSalePacket，
        // 如果之前没写，可以统一用 EcrPacketBuilder 构造
        return new EcrProtocol.EcrPacketBuilder(getNextEcn(), "I0")
                .addField("40", String.format("%012d", (long)amountInCents))
                .addField("HD", formattedRef)
                .build();
    }

    public static byte[] netsPurchaseCommand(int amountInCents, String referenceNumber, boolean isQR) {
        String formattedRef = padString(referenceNumber, 13, '0');
        String type = isQR ? "04" : "01";
        return EcrProtocol.buildNetsPurchasePacket(getNextEcn(), (long)amountInCents, formattedRef, type);
    }

    public static byte[] netsVoidCommand(String originalStan, String myRef) {
        String formattedRef = padString(myRef, 13, '0');
        // 取最后6位并左补零
        String formattedStan = originalStan.length() > 6 ?
                originalStan.substring(originalStan.length() - 6) :
                padString(originalStan, 6, '0');

        return new EcrProtocol.EcrPacketBuilder(getNextEcn(), "13")
                .addField("65", formattedStan)
                .addField("HD", formattedRef)
                .build();
    }

    // --- 核心通信逻辑 ---

    /**
     * 执行 ECR 命令
     * 注意：此方法是同步阻塞的，必须在子线程中调用
     */
    public Map<String, String> executeCommand(String ip, int port, byte[] command) {
        Socket socket = null;
        try {
            socket = new Socket();
            // 建立连接，设置超时
            socket.connect(new InetSocketAddress(ip, port), EcrProtocol.ECR_TIMEOUT_MS);
            socket.setSoTimeout(EcrProtocol.ECR_TIMEOUT_MS);

            Log.i(LOG_TAG, "Sending: " + EcrProtocol.bytesToHex(command));

            // 发送数据
            OutputStream outputStream = socket.getOutputStream();
            outputStream.write(command);
            outputStream.flush();

            // 接收响应
            InputStream inputStream = socket.getInputStream();

            // 1. 读取长度前缀 (2字节 BCD)
            byte[] lenPrefixBytes = new byte[2];
            int bytesRead = inputStream.read(lenPrefixBytes);
            if (bytesRead != 2) {
                throw new IOException("Failed to read 2-byte length prefix. Read " + bytesRead + " bytes.");
            }

            String lenHex = EcrProtocol.bcdToHexString(lenPrefixBytes);
            int totalBodyAndLrcLength;
            try {
                totalBodyAndLrcLength = Integer.parseInt(lenHex);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid BCD length format: " + lenHex);
            }

            Log.i(LOG_TAG, "Expected Length (Body + LRC): " + totalBodyAndLrcLength);

            // 2. 读取 Body + LRC
            byte[] remainingBytes = new byte[totalBodyAndLrcLength];
            int totalReadSoFar = 0;
            while (totalReadSoFar < totalBodyAndLrcLength) {
                int currentRead = inputStream.read(remainingBytes, totalReadSoFar, totalBodyAndLrcLength - totalReadSoFar);
                if (currentRead == -1) break;
                totalReadSoFar += currentRead;
            }

            // 3. 组装完整包 (Len + Body + LRC) 用于解析
            byte[] fullRawResponse = new byte[2 + totalReadSoFar];
            System.arraycopy(lenPrefixBytes, 0, fullRawResponse, 0, 2);
            System.arraycopy(remainingBytes, 0, fullRawResponse, 2, totalReadSoFar);

            Log.i(LOG_TAG, "Received: " + EcrProtocol.bytesToHex(fullRawResponse));

            return EcrProtocol.parseEcrResponse(fullRawResponse);

        } catch (Exception e) {
            Log.e(LOG_TAG, "ECR Comm Failed: " + e.getMessage(), e);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("Error", e.getMessage());
            errorMap.put("ResponseCode", "99");
            errorMap.put("状态", "通信异常");
            return errorMap;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 辅助工具：补齐字符串
    private static String padString(String str, int length, char padChar) {
        if (str == null) str = "";
        if (str.length() >= length) return str.substring(0, length);
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }
}