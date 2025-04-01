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
}

