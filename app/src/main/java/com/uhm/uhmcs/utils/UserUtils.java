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
    private static final String LICENSE_NO_KEY = "license_no";

    private LoginBase loginBase;
    private ShopDataBean shopDataBean;
    private String grouponGoodsBeanJson, categoryListBeanJson, loginPhone, loginPassword, orderListJson, language, serialPortName, licenseNo;

    // --- 新增：NETS POS 相关字段 ---
    private String ecrIp;
    private int ecrPort;

    public int VENDOR_ID, PRODUCT_ID, LABEKS_VENDOR_ID, LABEKS_PRODUCT_ID;
    private boolean isDazhe, isDianji;

    public static UserUtils getInstance() {
        if (null == instance) instance = new UserUtils();
        return instance;
    }

    public void inti(Context context) {
        SharedPreferences pref = context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        if (!TextUtils.isEmpty(pref.getString("loginBase", ""))) loginBase = new Gson().fromJson(pref.getString("loginBase", ""), LoginBase.class);
        if (!TextUtils.isEmpty(pref.getString("shopDataBean", ""))) shopDataBean = new Gson().fromJson(pref.getString("shopDataBean", ""), ShopDataBean.class);
        grouponGoodsBeanJson = pref.getString("grouponGoodsBeanJson", "");
        categoryListBeanJson = pref.getString("categoryListBeanJson", "");
        VENDOR_ID = pref.getInt("VENDOR_ID", 1046);
        PRODUCT_ID = pref.getInt("PRODUCT_ID", 20497);
        LABEKS_VENDOR_ID = pref.getInt("LABEKS_VENDOR_ID", 8137);
        LABEKS_PRODUCT_ID = pref.getInt("LABEKS_PRODUCT_ID", 8214);
        loginPassword = pref.getString("loginPassword", "");
        loginPhone = pref.getString("loginPhone", "");
        orderListJson = pref.getString("orderListJson", "");
        isDazhe = pref.getBoolean("isDazhe", false);
        language = pref.getString("language", "");
        isDianji = pref.getBoolean("isDianji", true);
        serialPortName = pref.getString("serialPortName", "/dev/ttyS4");
        licenseNo = pref.getString(LICENSE_NO_KEY, "");

        // --- 初始化时读取 POS 参数 ---
        ecrIp = pref.getString("ecrIp", "192.168.110.112"); // 默认 IP
        ecrPort = pref.getInt("ecrPort", 3000);              // 默认 端口
    }

    // --- 新增：NETS POS 设置方法 ---
    public String getEcrIp() {
        return TextUtils.isEmpty(ecrIp) ? "192.168.110.112" : ecrIp;
    }

    public void setEcrIp(Context context, String ip) {
        this.ecrIp = ip;
        saveString(context, "ecrIp", ip);
    }

    public int getEcrPort() {
        return ecrPort == 0 ? 3000 : ecrPort;
    }

    public void setEcrPort(Context context, int port) {
        this.ecrPort = port;
        saveInt(context, "ecrPort", port);
    }

    // --- 新增/修复的 AI 激活码方法 ---
    public String getLicenseNo() { return TextUtils.isEmpty(licenseNo) ? "" : licenseNo; }

    public void setLicenseNo(Context context, String value) {
        this.licenseNo = value;
        saveString(context, LICENSE_NO_KEY, value);
    }

    // --- 原有方法逻辑保持不变 ---
    public String getLanguage() { return language; }
    public void setLanguage(Context context, String language) { this.language = language; saveString(context, "language", language); }
    public String getOrderListJson() { return orderListJson; }
    public void setOrderListJson(Context context, String orderListJson) { this.orderListJson = orderListJson; saveString(context, "orderListJson", orderListJson); }
    public boolean isDianji() { return isDianji; }
    public void setDianji(Context context, boolean dianji) { this.isDianji = dianji; saveBoolean(context, "isDianji", dianji); }
    public boolean isDazhe() { return isDazhe; }
    public void setDazhe(Context context, boolean dazhe) { this.isDazhe = dazhe; saveBoolean(context, "isDazhe", dazhe); }
    public String getLoginPassword() { return loginPassword; }
    public void setLoginPassword(Context context, String loginPassword) { this.loginPassword = loginPassword; saveString(context, "loginPassword", loginPassword); }
    public String getLoginPhone() { return loginPhone; }
    public void setLoginPhone(Context context, String loginPhone) { this.loginPhone = loginPhone; saveString(context, "loginPhone", loginPhone); }
    public int getVENDOR_ID() { return VENDOR_ID; }
    public void setVENDOR_ID(Context context, int VENDOR_ID) { this.VENDOR_ID = VENDOR_ID; saveInt(context, "VENDOR_ID", VENDOR_ID); }
    public int getPRODUCT_ID() { return PRODUCT_ID; }
    public void setPRODUCT_ID(Context context, int PRODUCT_ID) { this.PRODUCT_ID = PRODUCT_ID; saveInt(context, "PRODUCT_ID", PRODUCT_ID); }
    public int getLABEKS_VENDOR_ID() { return LABEKS_VENDOR_ID; }
    public void setLABEKS_VENDOR_ID(Context context, int id) { this.LABEKS_VENDOR_ID = id; saveInt(context, "LABEKS_VENDOR_ID", id); }
    public int getLABEKS_PRODUCT_ID() { return LABEKS_PRODUCT_ID; }
    public void setLABEKS_PRODUCT_ID(Context context, int id) { this.LABEKS_PRODUCT_ID = id; saveInt(context, "LABEKS_PRODUCT_ID", id); }
    public String getGrouponGoodsBeanJson() { return grouponGoodsBeanJson; }
    public void setGrouponGoodsBeanJson(Context context, String json) { this.grouponGoodsBeanJson = json; saveString(context, "grouponGoodsBeanJson", json == null ? "" : json); }
    public String getCategoryListBeanJson() { return categoryListBeanJson; }
    public void setCategoryListBeanJson(Context context, String json) { this.categoryListBeanJson = json; saveString(context, "categoryListBeanJson", json == null ? "" : json); }
    public ShopDataBean getShopDataBean() { return shopDataBean; }
    public void setShopDataBean(Context context, ShopDataBean bean) { this.shopDataBean = bean; saveString(context, "shopDataBean", bean == null ? "" : new Gson().toJson(bean)); }
    public LoginBase getLoginBase() { return loginBase; }
    public void setLoginBase(Context context, LoginBase base) { this.loginBase = base; saveString(context, "loginBase", base == null ? "" : new Gson().toJson(base)); }
    public String getSerialPortName() { return TextUtils.isEmpty(serialPortName) ? "/dev/ttyS4" : serialPortName; }
    public void setSerialPortName(Context context, String name) { this.serialPortName = name; saveString(context, "serialPortName", name); }

    // --- 内部通用保存方法 ---
    private void saveString(Context c, String key, String v) { c.getSharedPreferences(USER_INFO_PREFERENCES_NAME, 0).edit().putString(key, v).apply(); }
    private void saveBoolean(Context c, String key, boolean v) { c.getSharedPreferences(USER_INFO_PREFERENCES_NAME, 0).edit().putBoolean(key, v).apply(); }
    private void saveInt(Context c, String key, int v) { c.getSharedPreferences(USER_INFO_PREFERENCES_NAME, 0).edit().putInt(key, v).apply(); }
}