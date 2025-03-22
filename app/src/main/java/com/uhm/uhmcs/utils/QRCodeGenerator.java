package com.uhm.uhmcs.utils;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.EnumMap;
import java.util.Map;

public class QRCodeGenerator {
    /**
     * 生成二维码Bitmap
     * @param content   二维码内容
     * @param widthPix  输出图片宽度（像素）
     * @param heightPix 输出图片高度（像素）
     * @param margin    二维码边距（0-4）
     */
    public static Bitmap createQRCode(String content, int widthPix, int heightPix, int margin) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE,
                    widthPix, heightPix, MapUtils.getHints(margin));

            return bitMatrix2Bitmap(matrix);
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Bitmap bitMatrix2Bitmap(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bitmap;
    }

    // 配置参数工具
    private static class MapUtils {
        static Map<EncodeHintType, Object> getHints(int margin) {
            Map<EncodeHintType, Object> hints = new EnumMap<>(EncodeHintType.class);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, margin);    // 默认边距=4
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 高容错率
            return hints;
        }
    }
}

