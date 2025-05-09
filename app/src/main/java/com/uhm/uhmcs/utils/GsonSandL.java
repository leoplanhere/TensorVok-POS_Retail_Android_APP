package com.uhm.uhmcs.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * 对象转换List与String类型工具
 */
public class GsonSandL {
    private static GsonSandL gsonSandL;

    public static GsonSandL getInstance() {
        if (gsonSandL == null) {
            synchronized (GsonSandL.class) {
                if (gsonSandL == null) {
                    gsonSandL = new GsonSandL();
                }
            }
        }
        return gsonSandL;
    }

    public<T> List<T> GsonStoL(String strJson,Class<T> clazz) {
//        List<T> beans = new ArrayList<>();
//        Gson gson = new Gson();
//        JsonArray array = new JsonParser().parse(strJson).getAsJsonArray();
//        for (JsonElement jsonElement : array) {
//            beans.add(gson.fromJson(jsonElement, value));
//        }
        Type type = TypeToken.getParameterized(ArrayList.class, clazz).getType();
        return new Gson().fromJson(strJson, type);
    }

    public<T> T GsonStoB(String strJson, Class<T> value) {
        T beans;
        Gson gson = new Gson();
        beans = gson.fromJson(strJson,value);
        return beans;
    }

    public String GsonLtoS(Object value) {
        Gson gson = new Gson();
        String strJson = gson.toJson(value);
        return strJson;
    }
}
