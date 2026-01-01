package com.uhm.uhmcs.utils; // 建议放在 utils 包下

import android.util.Log;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale; // <--- 确保这一行存在

public class EcrProtocol {
    private static final String TAG = "ECR_DEBUG";

    // 1. 通信参数
    public static final String DEFAULT_ECR_IP = "192.168.110.112";
    public static final int DEFAULT_ECR_PORT = 3000;
    public static final int ECR_TIMEOUT_MS = 30000;

    // 2. 协议控制字符
    public static final byte STX = 0x02;
    public static final byte ETX = 0x03;
    public static final byte SEPARATOR = 0x1C;
    private static final int HEADER_LENGTH_CHARS = 18;

    // 3. 数据类型转换工具
    public static byte[] asciiToBytes(String str) {
        return str.getBytes(StandardCharsets.US_ASCII);
    }

    public static byte[] hexToBinary(String hexString) {
        String cleanHex = hexString.replace(" ", "");
        if (cleanHex.length() % 2 != 0) return new byte[0];
        byte[] result = new byte[cleanHex.length() / 2];
        for (int i = 0; i < cleanHex.length(); i += 2) {
            try {
                result[i / 2] = (byte) Integer.parseInt(cleanHex.substring(i, i + 2), 16);
            } catch (NumberFormatException e) {
                result[i / 2] = 0;
            }
        }
        return result;
    }

    public static byte[] intToBcdLength(int length, int byteCount) {
        String formatString = "%0" + (byteCount * 2) + "d";
        return hexToBinary(String.format(formatString, length));
    }

    public static String bcdToHexString(byte[] bcd) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bcd) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    // 4. LRC 校验 (异或校验)
    public static byte calculateLrc(byte[] data) {
        byte lrc = 0x00;
        for (byte b : data) {
            lrc ^= b;
        }
        return lrc;
    }

    // 5. 内部报文构造类 (Builder 模式)
    public static class EcrPacketBuilder {
        private final String ecn;
        private final String functionCode;
        private final List<byte[]> messageFields = new ArrayList<>();
        private final String versionCode = "01";

        public EcrPacketBuilder(String ecn, String functionCode) {
            this.ecn = ecn;
            this.functionCode = functionCode;
        }

        public EcrPacketBuilder addField(String fieldCode, String data) {
            byte[] dataBytes = asciiToBytes(data);
            byte[] lenBcd = intToBcdLength(dataBytes.length, 2);
            byte[] fieldCodeBytes = asciiToBytes(fieldCode);

            // 拼接 Field: Code(2) + Len(2 BCD) + Data + SEPARATOR
            byte[] fieldPackage = new byte[fieldCodeBytes.length + lenBcd.length + dataBytes.length + 1];
            System.arraycopy(fieldCodeBytes, 0, fieldPackage, 0, fieldCodeBytes.length);
            System.arraycopy(lenBcd, 0, fieldPackage, fieldCodeBytes.length, lenBcd.length);
            System.arraycopy(dataBytes, 0, fieldPackage, fieldCodeBytes.length + lenBcd.length, dataBytes.length);
            fieldPackage[fieldPackage.length - 1] = SEPARATOR;

            messageFields.add(fieldPackage);
            return this;
        }

        public byte[] build() {
            // 构造 Header
            String headerStr = ecn + functionCode + versionCode + "0";
            byte[] headerBytesWithoutSep = asciiToBytes(headerStr);
            byte[] headerBytes = new byte[headerBytesWithoutSep.length + 1];
            System.arraycopy(headerBytesWithoutSep, 0, headerBytes, 0, headerBytesWithoutSep.length);
            headerBytes[headerBytes.length - 1] = SEPARATOR;

            // 计算 Body 总长度
            int bodyLen = headerBytes.length;
            for (byte[] field : messageFields) bodyLen += field.length;

            byte[] bodyBytes = new byte[bodyLen];
            int cursor = 0;
            System.arraycopy(headerBytes, 0, bodyBytes, cursor, headerBytes.length);
            cursor += headerBytes.length;
            for (byte[] field : messageFields) {
                System.arraycopy(field, 0, bodyBytes, cursor, field.length);
                cursor += field.length;
            }

            // 计算全包: Len(2 BCD) + Body + LRC(1)
            byte[] lenBytes = intToBcdLength(bodyBytes.length + 1, 2);
            byte lrc = calculateLrc(bodyBytes);

            byte[] fullPacket = new byte[lenBytes.length + bodyBytes.length + 1];
            System.arraycopy(lenBytes, 0, fullPacket, 0, lenBytes.length);
            System.arraycopy(bodyBytes, 0, fullPacket, lenBytes.length, bodyBytes.length);
            fullPacket[fullPacket.length - 1] = lrc;

            return fullPacket;
        }
    }

    // 6. 静态报文构造方法
    public static byte[] buildStatusPacket(String ecn) {
        return new EcrPacketBuilder(ecn, "55").build();
    }

    public static byte[] buildLogonPacket(String ecn) {
        return new EcrPacketBuilder(ecn, "80").build();
    }

    public static byte[] buildNetsPurchasePacket(String ecn, long amount, String ref, String type) {
        return new EcrPacketBuilder(ecn, "30")
                .addField("T2", type)
                .addField("40", String.format(Locale.US, "%012d", amount))
                .addField("42", String.format(Locale.US, "%012d", 0L))
                .addField("HD", ref)
                .build();
    }

    // 7. 响应解析工具
    public static Map<String, String> parseEcrResponse(byte[] response) {
        Map<String, String> parsedData = new HashMap<>();
        Log.d(TAG, "解析响应 Hex: " + bytesToHex(response));

        if (response.length < 3) {
            parsedData.put("Error", "Incomplete response");
            return parsedData;
        }

        int bodyStartIndex = 2; // 跳过前两个字节的长度标识

        // 解析 Header (18字节)
        if (response.length >= bodyStartIndex + HEADER_LENGTH_CHARS) {
            byte[] headerBytes = new byte[HEADER_LENGTH_CHARS];
            System.arraycopy(response, bodyStartIndex, headerBytes, 0, HEADER_LENGTH_CHARS);
            String headerStr = new String(headerBytes, StandardCharsets.US_ASCII);

            if (headerStr.length() >= 16) {
                parsedData.put("ECN", headerStr.substring(0, 12));
                parsedData.put("FunctionCode", headerStr.substring(12, 14));
                parsedData.put("ResponseCode", headerStr.substring(14, 16));
            }
        }

        // 解析 Field Data
        int fieldDataStartIndex = bodyStartIndex + HEADER_LENGTH_CHARS;
        int cursor = fieldDataStartIndex;

        // 循环解析直到包尾（最后1字节是LRC，需排除）
        while (cursor < response.length - 1) {
            if (response.length - cursor < 4) break;

            // 获取 Field ID (2字节 ASCII)
            byte[] codeBytes = new byte[2];
            System.arraycopy(response, cursor, codeBytes, 0, 2);
            String codeStr = bytesToHex(codeBytes).replace(" ", "");
            cursor += 2;

            // 获取 Field 长度 (2字节 BCD)
            byte[] lenBytes = new byte[2];
            System.arraycopy(response, cursor, lenBytes, 0, 2);
            String lenHexStr = bcdToHexString(lenBytes);
            int dataLen = 0;
            try {
                dataLen = Integer.parseInt(lenHexStr);
            } catch (Exception ignored) {}
            cursor += 2;

            if (cursor + dataLen > response.length) break;

            // 获取 Field Value (ASCII)
            byte[] valueBytes = new byte[dataLen];
            System.arraycopy(response, cursor, valueBytes, 0, dataLen);
            String valueStr = new String(valueBytes, StandardCharsets.US_ASCII).trim();
            parsedData.put(codeStr, valueStr);
            cursor += dataLen;

            // 跳过分隔符
            if (cursor < response.length && response[cursor] == SEPARATOR) {
                cursor++;
            }
        }

        String resCode = parsedData.get("ResponseCode");
        parsedData.put("状态", "00".equals(resCode) ? "成功 (00)" : "失败 (" + resCode + ")");

        return parsedData;
    }
}