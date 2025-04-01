package com.uhm.uhmcs.utils;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class ParcelUtils {
    // 对 List<Parcelable> 进行深拷贝
    public static <T extends Parcelable> List<T> deepCopyList(List<T> originalList) {
        List<T> copyList = new ArrayList<>();
        for (T item : originalList) {
            Parcel parcel = Parcel.obtain();
            try {
                parcel.writeParcelable(item, 0); // 写入对象到 Parcel‌:ml-citation{ref="1,2" data="citationList"}
                parcel.setDataPosition(0);        // 重置读取位置
                T copy = parcel.readParcelable(item.getClass().getClassLoader());
                copyList.add(copy);
            } finally {
                parcel.recycle(); // 释放 Parcel 资源‌:ml-citation{ref="2" data="citationList"}
            }
        }
        return copyList;
    }
}

