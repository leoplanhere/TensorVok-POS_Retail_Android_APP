package com.uhm.uhmcs.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SerializableUtils {
    // 对 List<Serializable> 进行深拷贝
    public static <T extends Serializable> ArrayList<T> deepCopyList(ArrayList<T> originalList) {
        try {
            // 序列化整个 List
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(originalList);
            oos.close();

            // 反序列化生成新 List
            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (ArrayList<T>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("深拷贝失败", e);
        }
    }
    public static <T> T deepCopy(T obj) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(obj);  // 序列化对象到字节流‌:ml-citation{ref="1,8" data="citationList"}
            out.flush();

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream in = new ObjectInputStream(bis);
            return (T) in.readObject();  // 反序列化生成新对象‌:ml-citation{ref="3,8" data="citationList"}
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}

