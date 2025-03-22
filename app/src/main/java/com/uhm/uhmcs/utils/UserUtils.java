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
        editor.commit();
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
        editor.commit();
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
        editor.commit();
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
        editor.commit();
        editor=null;
    }
}
