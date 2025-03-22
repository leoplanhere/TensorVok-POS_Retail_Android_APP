package com.uhm.uhmcs.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

import java.io.ByteArrayOutputStream;

// ImagePrinter.java
public class ImagePrinter {
    private static final int PRINTER_WIDTH = 384; // 根据实际打印机调整

    public static byte[] convertBitmapToEscPos(Bitmap bitmap) {
//        Bitmap scaledBitmap = scaleBitmap(bitmap);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        // ESC POS初始化
//            output.write(new byte[] { 0x1B, 0x40 });

        // 设置行间距
//            output.write(new byte[] { 0x1B, 0x33, 0x12 });

        // 光栅模式打印
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

//            for (int y = 0; y < height; y += 24) {
//                output.write(new byte[] { 0x1B, 0x2A, 0x21, (byte) (width & 0xFF), (byte) ((width >> 8) & 0xFF) });
//
//                for (int x = 0; x < width; x++) {
//                    for (int k = 0; k < 3; k++) {
//                        byte slice = 0;
//                        for (int b = 0; b < 8; b++) {
//                            int yPos = y + k * 8 + b;
//                            if (yPos >= height) continue;
//
//                            int pixel = bitmap.getPixel(x, yPos);
//                            if (Color.red(pixel) < 128) {
//                                slice |= (byte) (1 << (7 - b));
//                            }
//                        }
//                        output.write(slice);
//                    }
//                }
//                output.write(0x0A); // 换行
//            }
        // 1. 添加指令头
        byte[] cmdHeader = {
                0x1D, 0x76, 0x30, // GS v 0
                0x00,             // 模式：正常（1x1）
                (byte)((width/8) % 256), (byte)((width/8)/256), // xL/xH
                (byte)(height % 256), (byte)(height/256)        // yL/yH
        };
        output.write(cmdHeader, 0, cmdHeader.length);

        // 2. 生成像素数据
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x += 8) {
                byte dataByte = 0;
                for (int bit = 0; bit < 8; bit++) {
                    int px = x + bit;
                    if (px < width) {
                        int pixel = bitmap.getPixel(px, y);
                        // 判断灰度值是否超过阈值（示例使用50%阈值）
                        if ((Color.red(pixel) + Color.green(pixel) + Color.blue(pixel)) / 3 < 128) {
                            dataByte |= (1 << (7 - bit)); // 高位在前
                        }
                    }
                }
                output.write(dataByte);
            }
        }

        // 走纸和切纸
//            output.write(new byte[] { 0x1D, 0x56, 0x41, 0x10 });
        return output.toByteArray();
    }

    private static Bitmap scaleBitmap(Bitmap src) {
        int width = src.getWidth();
        int height = src.getHeight();

        // 保持宽高比缩放
        float ratio = (float) PRINTER_WIDTH / width;
        int newHeight = (int) (height * ratio);

        return Bitmap.createScaledBitmap(
                src,
                PRINTER_WIDTH,
                newHeight,
                true
        );
    }

    public static Bitmap toMonochrome(Bitmap src) {
        Bitmap result = Bitmap.createBitmap(src.getWidth(), src.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter filter = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(filter);
        canvas.drawBitmap(src, 0, 0, paint);
        return result;
    }
}

