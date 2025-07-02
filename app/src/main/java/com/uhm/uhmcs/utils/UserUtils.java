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

    private ShopDataBean shopDataBean;

    private String grouponGoodsBeanJson;

    private String categoryListBeanJson;

    public  int VENDOR_ID ;  // 替换为你的打印机厂商ID（如芯烨为 1155）
    public  int PRODUCT_ID ; // 替换为你的打印机产品ID

    public  int LABEKS_VENDOR_ID ;  // 替换为你的打印机厂商ID（如芯烨为 1155）
    public  int LABEKS_PRODUCT_ID ; // 替换为你的打印机产品ID

    private String loginPhone;

    private String loginPassword;


    private String orderListJson;

    private boolean isDazhe;

    private boolean isDianji;



    /**
     * 获取单件实例
     *
     * @return
     */
    public static UserUtils getInstance() {
        if (null == instance)
            instance = new UserUtils();
        return instance;
    }

    public void inti(Context context) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);

        if (!TextUtils.isEmpty(prefUserInfo.getString("loginBase",""))){
            loginBase=new Gson().fromJson(prefUserInfo.getString("loginBase",""),LoginBase.class);
        }
        if (!TextUtils.isEmpty(prefUserInfo.getString("shopDataBean",""))){
            shopDataBean=new Gson().fromJson(prefUserInfo.getString("shopDataBean",""),ShopDataBean.class);
        }
        grouponGoodsBeanJson=prefUserInfo.getString("grouponGoodsBeanJson","");
        categoryListBeanJson=prefUserInfo.getString("categoryListBeanJson","");
        VENDOR_ID=prefUserInfo.getInt("VENDOR_ID",1046);
        PRODUCT_ID=prefUserInfo.getInt("PRODUCT_ID",20497);
        LABEKS_VENDOR_ID=prefUserInfo.getInt("LABEKS_VENDOR_ID",8137);
        LABEKS_PRODUCT_ID=prefUserInfo.getInt("LABEKS_PRODUCT_ID",8214);
        loginPassword=prefUserInfo.getString("loginPassword","");
        loginPhone=prefUserInfo.getString("loginPhone","");
        orderListJson=prefUserInfo.getString("orderListJson","");
        isDazhe=prefUserInfo.getBoolean("isDazhe",false);
        isDianji=prefUserInfo.getBoolean("isDianji",true);
    }

    public String getOrderListJson() {
        return orderListJson;
    }

    public void setOrderListJson(Context context,String orderListJson) {

        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.orderListJson = orderListJson;
        editor.putString("orderListJson",orderListJson);
        editor.apply();
        editor=null;
    }


    public boolean isDianji() {
        return isDianji;
    }

    public void setDianji(Context context,boolean dianji) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        isDianji = dianji;
        editor.putBoolean("isDianji",dianji);
        editor.apply();
        editor=null;
    }

    public boolean isDazhe() {
        return isDazhe;
    }

    public void setDazhe(Context context,boolean dazhe) {

        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        isDazhe = dazhe;
        editor.putBoolean("isDazhe",isDazhe);
        editor.apply();
        editor=null;
    }

    public String getLoginPassword() {
        return loginPassword;
    }

    public void setLoginPassword(Context context,String loginPassword) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.loginPassword = loginPassword;
        editor.putString("loginPassword",loginPassword);
        editor.apply();
        editor=null;
    }

    public String getLoginPhone() {
        return loginPhone;
    }

    public void setLoginPhone(Context context,String loginPhone) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.loginPhone = loginPhone;
        editor.putString("loginPhone",loginPhone);
        editor.apply();
        editor=null;
    }

    public int getVENDOR_ID() {
        return VENDOR_ID;
    }

    public void setVENDOR_ID(Context context,int VENDOR_ID) {

        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.VENDOR_ID = VENDOR_ID;
        editor.putInt("VENDOR_ID",VENDOR_ID);
        editor.apply();
        editor=null;
    }

    public int getPRODUCT_ID() {
        return PRODUCT_ID;
    }

    public void setPRODUCT_ID(Context context,int PRODUCT_ID) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.PRODUCT_ID = PRODUCT_ID;
        editor.putInt("PRODUCT_ID",PRODUCT_ID);
        editor.apply();
        editor=null;
    }

    public int getLABEKS_VENDOR_ID() {
        return LABEKS_VENDOR_ID;
    }

    public void setLABEKS_VENDOR_ID(Context context,int LABEKS_VENDOR_ID) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.LABEKS_VENDOR_ID = LABEKS_VENDOR_ID;
        editor.putInt("LABEKS_VENDOR_ID",LABEKS_VENDOR_ID);
        editor.apply();
        editor=null;
    }

    public int getLABEKS_PRODUCT_ID() {
        return LABEKS_PRODUCT_ID;
    }

    public void setLABEKS_PRODUCT_ID(Context context,int LABEKS_PRODUCT_ID) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.LABEKS_PRODUCT_ID = LABEKS_PRODUCT_ID;
        editor.putInt("LABEKS_PRODUCT_ID",LABEKS_PRODUCT_ID);
        editor.apply();
        editor=null;
    }

    public String getGrouponGoodsBeanJson() {
        return grouponGoodsBeanJson;
    }

    public void setGrouponGoodsBeanJson(Context context,String grouponGoodsBeanJson) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.grouponGoodsBeanJson = grouponGoodsBeanJson;
        if (grouponGoodsBeanJson==null){
            editor.putString("grouponGoodsBeanJson","");
        }else {
            editor.putString("grouponGoodsBeanJson",grouponGoodsBeanJson);
        }
        editor.apply();
        editor=null;
    }

    public String getCategoryListBeanJson() {
        return categoryListBeanJson;
    }

    public void setCategoryListBeanJson(Context context,String categoryListBeanJson) {

        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.categoryListBeanJson = categoryListBeanJson;
        if (categoryListBeanJson==null){
            editor.putString("categoryListBeanJson","");
        }else {
            editor.putString("categoryListBeanJson",categoryListBeanJson);
        }
        editor.apply();
        editor=null;
    }

    public ShopDataBean getShopDataBean() {
        return shopDataBean;
    }

    public void setShopDataBean(Context context,ShopDataBean shopDataBean) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.shopDataBean = shopDataBean;
        if (shopDataBean==null){
            editor.putString("shopDataBean","");
        }else {
            editor.putString("shopDataBean",new Gson().toJson(shopDataBean));
        }
        editor.apply();
        editor=null;
    }

    public LoginBase getLoginBase() {
        return loginBase;
    }

    public void setLoginBase(Context context,LoginBase loginBase) {
        SharedPreferences prefUserInfo = context.getSharedPreferences(
                USER_INFO_PREFERENCES_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefUserInfo.edit();
        this.loginBase = loginBase;
        if (loginBase==null){
            editor.putString("loginBase","");
        }else {
            editor.putString("loginBase",new Gson().toJson(loginBase));
        }
        editor.apply();
        editor=null;
    }
}
