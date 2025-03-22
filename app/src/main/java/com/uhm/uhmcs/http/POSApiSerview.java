package com.uhm.uhmcs.http;


/**
 * api接口存放
 */
public interface POSApiSerview {

    String POS_URL = "https://xlcc.uhimao.com/api/";

    //登录
    String login = "user/login";
    //登录
    String GrouponGoods = "Supermarket/getGrouponGoods";
    //获取店铺列表
    String shopList = "shop/list";
   //获取分类列表
    String getGrouponCategory="Supermarket/getGrouponCategory";
   //根据分类查询商品
    String getGrouponGoods="Supermarket/getGrouponGoods";
    //根据分类查询商品
    String getGrouponGoods2="Supermarket/getGrouponGoods2";

    //根据id查询商品
    String getGoods="Supermarket/getGoodsMultiple";

    //全部商品
    String getGoodsLists="Supermarket/getGoodsLists";

    //结账
    String addOrder="Supermarket/addOrder";
    //打印信息
    String operateDetails="currency/operateDetails";
    //会员查询
    String getMember="Custom/accordingPhone";
    //尾单查询
    String getLastOder="Supermarket/printthefinalorder";
}
