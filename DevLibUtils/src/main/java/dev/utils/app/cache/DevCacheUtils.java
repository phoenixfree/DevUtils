package dev.utils.app.cache;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import java.io.ByteArrayOutputStream;

import dev.utils.LogPrintUtils;

/**
 * detail: 缓存检查(时间)工具类
 * Created by 杨福海(michael) www.yangfuhai.com
 * Update to Ttt
 */
final class DevCacheUtils {

    private DevCacheUtils() {
    }

    // 日志 TAG
    private static final String TAG = DevCacheUtils.class.getSimpleName();

    /**
     * 判断缓存的 String 数据是否到期
     * @param str
     * @return true: 到期了, false: 还没有到期
     */
    public static boolean isDue(final String str) {
        if (str == null) return true;
        return isDue(str.getBytes());
    }

    /**
     * 判断缓存的 byte 数据是否到期
     * @param data
     * @return true: 到期了, false: 还没有到期
     */
    public static boolean isDue(final byte[] data) {
        // 获取时间数据信息
        String[] strs = getDateInfoFromDate(data);
        // 判断是否过期
        if (strs != null && strs.length == 2) {
            // 保存的时间
            String saveTimeStr = strs[0];
            // 判断是否0开头,是的话裁剪
            while (saveTimeStr.startsWith("0")) {
                saveTimeStr = saveTimeStr.substring(1);
            }
            // 转换时间
            long saveTime = Long.valueOf(saveTimeStr); // 保存时间
            long deleteAfter = Long.valueOf(strs[1]); // 过期时间
            // 判断当前时间是否大于 保存时间 + 过期时间
            if (System.currentTimeMillis() > saveTime + deleteAfter * 1000) {
                return true;
            }
        }
        return false;
    }

    // -

    /**
     * 保存数据, 创建时间信息
     * @param second
     * @param strInfo
     * @return
     */
    public static String newStringWithDateInfo(final int second, final String strInfo) {
        return createDateInfo(second) + strInfo;
    }

    /**
     * 保存数据, 创建时间信息
     * @param second
     * @param data
     * @return
     */
    public static byte[] newByteArrayWithDateInfo(final int second, final byte[] data) {
        if (data != null) {
            try {
                byte[] dataArys = createDateInfo(second).getBytes();
                byte[] retData = new byte[dataArys.length + data.length];
                System.arraycopy(dataArys, 0, retData, 0, dataArys.length);
                System.arraycopy(data, 0, retData, dataArys.length, data.length);
                return retData;
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "newByteArrayWithDateInfo");
            }
        }
        return null;
    }

    private static final char mSeparator = ' ';

    /**
     * 创建时间信息
     * @param second
     * @return
     */
    private static String createDateInfo(final int second) {
        String currentTime = System.currentTimeMillis() + "";
        while (currentTime.length() < 13) {
            currentTime = "0" + currentTime;
        }
        return currentTime + "-" + second + mSeparator;
    }

    /**
     * 清空时间信息
     * @param strInfo
     * @return
     */
    public static String clearDateInfo(final String strInfo) {
        if (strInfo != null && hasDateInfo(strInfo.getBytes())) {
            return strInfo.substring(strInfo.indexOf(mSeparator) + 1);
        }
        return strInfo;
    }

    /**
     * 清空时间信息
     * @param data
     * @return
     */
    public static byte[] clearDateInfo(final byte[] data) {
        if (hasDateInfo(data)) {
            try {
                return copyOfRange(data, indexOf(data, mSeparator) + 1, data.length);
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "clearDateInfo");
            }
        }
        return data;
    }

    /**
     * 检验时间信息
     * @param data
     * @return
     */
    private static boolean hasDateInfo(final byte[] data) {
        return data != null && data.length > 15 && data[13] == '-' && indexOf(data, mSeparator) > 14;
    }

    /**
     * 获取时间信息 - 保存时间、过期时间
     * @param data
     * @return
     */
    private static String[] getDateInfoFromDate(final byte[] data) {
        if (hasDateInfo(data)) {
            try {
                // 保存时间
                String saveDate = new String(copyOfRange(data, 0, 13));
                // 过期时间
                String deleteAfter = new String(copyOfRange(data, 14, indexOf(data, mSeparator)));
                // 返回数据
                return new String[]{saveDate, deleteAfter};
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "getDateInfoFromDate");
            }
        }
        return null;
    }

    private static int indexOf(final byte[] data, final char c) {
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                if (data[i] == c) {
                    return i;
                }
            }
        }
        return -1;
    }

    private static byte[] copyOfRange(final byte[] original, final int from, final int to) throws Exception {
        int newLength = to - from;
        if (newLength < 0)
            throw new IllegalArgumentException(from + " > " + to);

        byte[] copy = new byte[newLength];
        System.arraycopy(original, from, copy, 0, Math.min(original.length - from, newLength));
        return copy;
    }

    /**
     * Bitmap → byte[]
     * @param bitmap
     * @return
     */
    public static byte[] bitmapToBytes(final Bitmap bitmap) {
        if (bitmap == null) return null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "bitmapToBytes");
        }
        return null;
    }

    /**
     * byte[] → Bitmap
     * @param bytes
     * @return
     */
    public static Bitmap bytesToBitmap(final byte[] bytes) {
        if (bytes != null && bytes.length != 0) {
            try {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (Exception e) {
                LogPrintUtils.eTag(TAG, e, "bytesToBitmap");
            }
        }
        return null;
    }

    /**
     * Drawable → Bitmap
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap(final Drawable drawable) {
        if (drawable == null) return null;
        try {
            // 取 drawable 的长宽
            int w = drawable.getIntrinsicWidth();
            int h = drawable.getIntrinsicHeight();
            // 取 drawable 的颜色格式
            Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888 : Bitmap.Config.RGB_565;
            // 建立对应 bitmap
            Bitmap bitmap = Bitmap.createBitmap(w, h, config);
            // 建立对应 bitmap 的画布
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, w, h);
            // 把 drawable 内容画到画布中
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "drawableToBitmap");
        }
        return null;
    }

    /**
     * Bitmap → Drawable
     * @param bitmap
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Drawable bitmapToDrawable(final Bitmap bitmap) {
        if (bitmap == null) return null;
        try {
            BitmapDrawable drawable = new BitmapDrawable(bitmap);
            drawable.setTargetDensity(bitmap.getDensity());
            return new BitmapDrawable(bitmap);
        } catch (Exception e) {
            LogPrintUtils.eTag(TAG, e, "bitmapToDrawable");
        }
        return null;
    }
}
