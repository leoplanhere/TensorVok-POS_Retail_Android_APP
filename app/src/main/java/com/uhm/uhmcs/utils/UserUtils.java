package com.uhm.uhmcs.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.uhm.uhmcs.bean.LoginBase;
import com.uhm.uhmcs.bean.ShopDataBean;

public class UserUtils {
    private static UserUtils instance;
    public static final String USER_INFO_PREFERENCES_NAME = "user_info";
    private static final String LABEL_DIY_PREFERENCES = "label_diy_config";

    private LoginBase loginBase;
    private String currencyType;
    private ShopDataBean shopDataBean;
    private String grouponGoodsBeanJson;
    private String categoryListBeanJson;

    public int VENDOR_ID;
    public int PRODUCT_ID;
    public int LABEKS_VENDOR_ID;
    public int LABEKS_PRODUCT_ID;

    private String loginPhone;
    private String loginPassword;
    private String orderListJson;
    private boolean isDazhe;
    private boolean isDianji;

    public static UserUtils getInstance() {
        if (null == instance)
            instance = new UserUtils();
        return instance;
    }

    public void inti(Context context) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);

        String loginBaseJson = prefUserInfo.getString("loginBase", "");
        if (!TextUtils.isEmpty(loginBaseJson)) {
            loginBase = new Gson().fromJson(loginBaseJson, LoginBase.class);
        }

        String shopDataJson = prefUserInfo.getString("shopDataBean", "");
        if (!TextUtils.isEmpty(shopDataJson)) {
            shopDataBean = new Gson().fromJson(shopDataJson, ShopDataBean.class);
        }

        grouponGoodsBeanJson = prefUserInfo.getString("grouponGoodsBeanJson", "");
        categoryListBeanJson = prefUserInfo.getString("categoryListBeanJson", "");
        VENDOR_ID = prefUserInfo.getInt("VENDOR_ID", 1046);
        PRODUCT_ID = prefUserInfo.getInt("PRODUCT_ID", 20497);
        LABEKS_VENDOR_ID = prefUserInfo.getInt("LABEKS_VENDOR_ID", 8137);
        LABEKS_PRODUCT_ID = prefUserInfo.getInt("LABEKS_PRODUCT_ID", 8214);
        loginPassword = prefUserInfo.getString("loginPassword", "");
        loginPhone = prefUserInfo.getString("loginPhone", "");
        orderListJson = prefUserInfo.getString("orderListJson", "");
        isDazhe = prefUserInfo.getBoolean("isDazhe", false);
        isDianji = prefUserInfo.getBoolean("isDianji", true);
        currencyType = prefUserInfo.getString("currencyType", "CNY");
    }

    // ================= 标签打印 & DIY 元素持久化配置 (新增) =================

    // 标签宽高 (mm)
    public int getLabelWidth(Context context) {
        return context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).getInt("label_width", 100);
    }
    public void setLabelWidth(Context context, int width) {
        context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).edit().putInt("label_width", width).apply();
    }
    public int getLabelHeight(Context context) {
        return context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).getInt("label_height", 40);
    }
    public void setLabelHeight(Context context, int height) {
        context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).edit().putInt("label_height", height).apply();
    }

    // 显隐配置
    public boolean getLabelConfig(Context context, String key, boolean defaultVal) {
        return context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).getBoolean("conf_" + key, defaultVal);
    }
    public void setLabelConfig(Context context, String key, boolean value) {
        context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean("conf_" + key, value).apply();
    }

    // 坐标位置 (X, Y)
    public float getElementX(Context context, String key) {
        return context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).getFloat("pos_x_" + key, -1f);
    }
    public void setElementX(Context context, String key, float x) {
        context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).edit().putFloat("pos_x_" + key, x).apply();
    }
    public float getElementY(Context context, String key) {
        return context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).getFloat("pos_y_" + key, -1f);
    }
    public void setElementY(Context context, String key, float y) {
        context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).edit().putFloat("pos_y_" + key, y).apply();
    }

    // 字号大小
    public int getElementTextSize(Context context, String key, int defaultSize) {
        return context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).getInt("size_" + key, defaultSize);
    }
    public void setElementTextSize(Context context, String key, int size) {
        context.getSharedPreferences(LABEL_DIY_PREFERENCES, Context.MODE_PRIVATE).edit().putInt("size_" + key, size).apply();
    }

    // ================= 原有业务逻辑 (Getters & Setters) =================

    public String getCurrencyType() {
        return TextUtils.isEmpty(currencyType) ? "CNY" : currencyType;
    }

    public void setCurrencyType(Context context, String type) {
        this.currencyType = type;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString("currencyType", type).apply();
    }

    public String getOrderListJson() { return orderListJson; }
    public void setOrderListJson(Context context, String orderListJson) {
        this.orderListJson = orderListJson;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString("orderListJson", orderListJson).apply();
    }

    public boolean isDianji() { return isDianji; }
    public void setDianji(Context context, boolean dianji) {
        this.isDianji = dianji;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putBoolean("isDianji", dianji).apply();
    }

    public boolean isDazhe() { return isDazhe; }
    public void setDazhe(Context context, boolean dazhe) {
        this.isDazhe = dazhe;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putBoolean("isDazhe", dazhe).apply();
    }

    public String getLoginPassword() { return loginPassword; }
    public void setLoginPassword(Context context, String loginPassword) {
        this.loginPassword = loginPassword;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString("loginPassword", loginPassword).apply();
    }

    public String getLoginPhone() { return loginPhone; }
    public void setLoginPhone(Context context, String loginPhone) {
        this.loginPhone = loginPhone;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString("loginPhone", loginPhone).apply();
    }

    public int getVENDOR_ID() { return VENDOR_ID; }
    public void setVENDOR_ID(Context context, int VENDOR_ID) {
        this.VENDOR_ID = VENDOR_ID;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putInt("VENDOR_ID", VENDOR_ID).apply();
    }

    public int getPRODUCT_ID() { return PRODUCT_ID; }
    public void setPRODUCT_ID(Context context, int PRODUCT_ID) {
        this.PRODUCT_ID = PRODUCT_ID;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putInt("PRODUCT_ID", PRODUCT_ID).apply();
    }

    public int getLABEKS_VENDOR_ID() { return LABEKS_VENDOR_ID; }
    public void setLABEKS_VENDOR_ID(Context context, int LABEKS_VENDOR_ID) {
        this.LABEKS_VENDOR_ID = LABEKS_VENDOR_ID;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putInt("LABEKS_VENDOR_ID", LABEKS_VENDOR_ID).apply();
    }

    public int getLABEKS_PRODUCT_ID() { return LABEKS_PRODUCT_ID; }
    public void setLABEKS_PRODUCT_ID(Context context, int LABEKS_PRODUCT_ID) {
        this.LABEKS_PRODUCT_ID = LABEKS_PRODUCT_ID;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putInt("LABEKS_PRODUCT_ID", LABEKS_PRODUCT_ID).apply();
    }

    public String getGrouponGoodsBeanJson() { return grouponGoodsBeanJson; }
    public void setGrouponGoodsBeanJson(Context context, String grouponGoodsBeanJson) {
        this.grouponGoodsBeanJson = grouponGoodsBeanJson;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString("grouponGoodsBeanJson", grouponGoodsBeanJson == null ? "" : grouponGoodsBeanJson).apply();
    }

    public String getCategoryListBeanJson() { return categoryListBeanJson; }
    public void setCategoryListBeanJson(Context context, String categoryListBeanJson) {
        this.categoryListBeanJson = categoryListBeanJson;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString("categoryListBeanJson", categoryListBeanJson == null ? "" : categoryListBeanJson).apply();
    }

    public ShopDataBean getShopDataBean() { return shopDataBean; }
    public void setShopDataBean(Context context, ShopDataBean shopDataBean) {
        this.shopDataBean = shopDataBean;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString("shopDataBean", shopDataBean == null ? "" : new Gson().toJson(shopDataBean)).apply();
    }

    public LoginBase getLoginBase() { return loginBase; }
    public void setLoginBase(Context context, LoginBase loginBase) {
        this.loginBase = loginBase;
        context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE).edit().putString("loginBase", loginBase == null ? "" : new Gson().toJson(loginBase)).apply();
    }
}