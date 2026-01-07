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
    public int VENDOR_ID, PRODUCT_ID, LABEKS_VENDOR_ID, LABEKS_PRODUCT_ID;
    private boolean isDazhe, isDianji;

    public static UserUtils getInstance() {
        if (null == instance) instance = new UserUtils();
        return instance;
    }

    public void inti(Context context) {
        SharedPreferences pref = getPrefs(context);
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
    }

    // ================== 新增：标签打印 DIY 配置方法 ==================

    public void setLabelWidth(Context context, int width) {
        saveInt(context, "label_width", width);
    }
    public int getLabelWidth(Context context) {
        return getInt(context, "label_width", 100); // 默认 100mm
    }

    public void setLabelHeight(Context context, int height) {
        saveInt(context, "label_height", height);
    }
    public int getLabelHeight(Context context) {
        return getInt(context, "label_height", 40); // 默认 40mm
    }

    // 打印开关配置
    public void setLabelConfig(Context context, String key, boolean isOpen) {
        saveBoolean(context, "label_cfg_" + key, isOpen);
    }
    public boolean getLabelConfig(Context context, String key, boolean defValue) {
        return getBoolean(context, "label_cfg_" + key, defValue);
    }

    // 字号存储逻辑
    public void setElementTextSize(Context context, String key, int size) {
        getPrefs(context).edit().putInt(key + "_size", size).apply();
    }
    public int getElementTextSize(Context context, String key, int defaultSize) {
        return getPrefs(context).getInt(key + "_size", defaultSize);
    }

    // 元素 X 坐标
    public void setElementX(Context context, String elementName, float x) {
        saveFloat(context, "pos_x_" + elementName, x);
    }
    public float getElementX(Context context, String elementName) {
        return getFloat(context, "pos_x_" + elementName, -1f);
    }

    // 元素 Y 坐标
    public void setElementY(Context context, String elementName, float y) {
        saveFloat(context, "pos_y_" + elementName, y);
    }
    public float getElementY(Context context, String elementName) {
        return getFloat(context, "pos_y_" + elementName, -1f);
    }

    // ================== 原有业务方法 ==================

    public String getLicenseNo() { return TextUtils.isEmpty(licenseNo) ? "" : licenseNo; }

    public void setLicenseNo(Context context, String value) {
        this.licenseNo = value;
        saveString(context, LICENSE_NO_KEY, value);
    }

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

    // ================== 核心基础存取方法 ==================

    /**
     * 统一获取 SharedPreferences
     */
    private SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    private void saveString(Context c, String key, String v) {
        if (c == null) return;
        getPrefs(c).edit().putString(key, v).apply();
    }

    private void saveBoolean(Context c, String key, boolean v) {
        if (c == null) return;
        getPrefs(c).edit().putBoolean(key, v).apply();
    }

    private boolean getBoolean(Context c, String key, boolean defValue) {
        if (c == null) return defValue;
        return getPrefs(c).getBoolean(key, defValue);
    }

    private void saveInt(Context c, String key, int v) {
        if (c == null) return;
        getPrefs(c).edit().putInt(key, v).apply();
    }

    private int getInt(Context c, String key, int defValue) {
        if (c == null) return defValue;
        return getPrefs(c).getInt(key, defValue);
    }

    private void saveFloat(Context c, String key, float v) {
        if (c == null) return;
        getPrefs(c).edit().putFloat(key, v).apply();
    }

    private float getFloat(Context c, String key, float defValue) {
        if (c == null) return defValue;
        return getPrefs(c).getFloat(key, defValue);
    }
}