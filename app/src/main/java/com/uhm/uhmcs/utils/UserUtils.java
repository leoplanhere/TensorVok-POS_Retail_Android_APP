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

    private LoginBase loginBase;
    private String currencyType; // 新增：币种变量
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

    /**
     * 获取单例实例
     */
    public static UserUtils getInstance() {
        if (null == instance)
            instance = new UserUtils();
        return instance;
    }

    /**
     * 初始化：从本地存储加载所有数据
     */
    public void inti(Context context) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);

        // 加载对象类数据
        String loginBaseJson = prefUserInfo.getString("loginBase", "");
        if (!TextUtils.isEmpty(loginBaseJson)) {
            loginBase = new Gson().fromJson(loginBaseJson, LoginBase.class);
        }

        String shopDataJson = prefUserInfo.getString("shopDataBean", "");
        if (!TextUtils.isEmpty(shopDataJson)) {
            shopDataBean = new Gson().fromJson(shopDataJson, ShopDataBean.class);
        }

        // 加载基础配置
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

        // 加载币种配置（默认为人民币 CNY）
        currencyType = prefUserInfo.getString("currencyType", "CNY");
    }

    // ================= 币种 (Currency) 处理 =================

    public String getCurrencyType() {
        return TextUtils.isEmpty(currencyType) ? "CNY" : currencyType;
    }

    public void setCurrencyType(Context context, String type) {
        this.currencyType = type;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putString("currencyType", type);
        editor.apply();
    }

    // ================= 原有属性的 Getters & Setters =================

    public String getOrderListJson() {
        return orderListJson;
    }

    public void setOrderListJson(Context context, String orderListJson) {
        this.orderListJson = orderListJson;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putString("orderListJson", orderListJson);
        editor.apply();
    }

    public boolean isDianji() {
        return isDianji;
    }

    public void setDianji(Context context, boolean dianji) {
        this.isDianji = dianji;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putBoolean("isDianji", dianji);
        editor.apply();
    }

    public boolean isDazhe() {
        return isDazhe;
    }

    public void setDazhe(Context context, boolean dazhe) {
        this.isDazhe = dazhe;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putBoolean("isDazhe", dazhe);
        editor.apply();
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(Context context, String loginPassword) {
        this.loginPassword = loginPassword;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putString("loginPassword", loginPassword);
        editor.apply();
    }

    public String getLoginPhone() {
        return loginPhone;
    }

    public void setLoginPhone(Context context, String loginPhone) {
        this.loginPhone = loginPhone;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putString("loginPhone", loginPhone);
        editor.apply();
    }

    public int getVENDOR_ID() {
        return VENDOR_ID;
    }

    public void setVENDOR_ID(Context context, int VENDOR_ID) {
        this.VENDOR_ID = VENDOR_ID;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putInt("VENDOR_ID", VENDOR_ID);
        editor.apply();
    }

    public int getPRODUCT_ID() {
        return PRODUCT_ID;
    }

    public void setPRODUCT_ID(Context context, int PRODUCT_ID) {
        this.PRODUCT_ID = PRODUCT_ID;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putInt("PRODUCT_ID", PRODUCT_ID);
        editor.apply();
    }

    public int getLABEKS_VENDOR_ID() {
        return LABEKS_VENDOR_ID;
    }

    public void setLABEKS_VENDOR_ID(Context context, int LABEKS_VENDOR_ID) {
        this.LABEKS_VENDOR_ID = LABEKS_VENDOR_ID;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putInt("LABEKS_VENDOR_ID", LABEKS_VENDOR_ID);
        editor.apply();
    }

    public int getLABEKS_PRODUCT_ID() {
        return LABEKS_PRODUCT_ID;
    }

    public void setLABEKS_PRODUCT_ID(Context context, int LABEKS_PRODUCT_ID) {
        this.LABEKS_PRODUCT_ID = LABEKS_PRODUCT_ID;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putInt("LABEKS_PRODUCT_ID", LABEKS_PRODUCT_ID);
        editor.apply();
    }

    public String getGrouponGoodsBeanJson() {
        return grouponGoodsBeanJson;
    }

    public void setGrouponGoodsBeanJson(Context context, String grouponGoodsBeanJson) {
        this.grouponGoodsBeanJson = grouponGoodsBeanJson;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putString("grouponGoodsBeanJson", grouponGoodsBeanJson == null ? "" : grouponGoodsBeanJson);
        editor.apply();
    }

    public String getCategoryListBeanJson() {
        return categoryListBeanJson;
    }

    public void setCategoryListBeanJson(Context context, String categoryListBeanJson) {
        this.categoryListBeanJson = categoryListBeanJson;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putString("categoryListBeanJson", categoryListBeanJson == null ? "" : categoryListBeanJson);
        editor.apply();
    }

    public ShopDataBean getShopDataBean() {
        return shopDataBean;
    }

    public void setShopDataBean(Context context, ShopDataBean shopDataBean) {
        this.shopDataBean = shopDataBean;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putString("shopDataBean", shopDataBean == null ? "" : new Gson().toJson(shopDataBean));
        editor.apply();
    }

    public LoginBase getLoginBase() {
        return loginBase;
    }

    public void setLoginBase(Context context, LoginBase loginBase) {
        this.loginBase = loginBase;
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        editor.putString("loginBase", loginBase == null ? "" : new Gson().toJson(loginBase));
        editor.apply();
    }
}