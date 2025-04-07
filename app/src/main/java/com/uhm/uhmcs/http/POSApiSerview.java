package com.uhm.uhmcs.http;


/**
 * api接口存放
 */
public interface POSApiSerview {

//    String POS_URL = "https://xlcc.uhimao.com/api/";
    String POS_URL = "https://shbs.uhimao.com/api/";

    //登录
    String login = "user/login";
    //登录
    String GrouponGoods = "Supermarket/getGrouponGoods";
    //获取店铺列表
    String shopList = "shop/list";
    //获取分类列表
    String getGrouponCategory = "Supermarket/getGrouponCategory";
    //根据分类查询商品
    String getGrouponGoods = "Supermarket/getGrouponGoods";
    //根据分类查询商品
    String getGrouponGoods2 = "Supermarket/getGrouponGoods2";

    //根据id查询商品
    String getGoods = "Supermarket/getGoodsMultiple";

    //全部商品
    String getGoodsLists = "Supermarket/getGoodsLists";

    //结账
    String addOrder = "Supermarket/addOrder1";
    //打印信息
    String operateDetails = "currency/operateDetails";
    //会员查询
    String getMember = "Custom/accordingPhone";
    //尾单查询
    String getLastOder = "supermarket/printthefinalorder";
    //商品入库
    String addStore = "Supermarket/addStore";
    //历史账单
    String orderList = "Supermarket/orderList";
    //现金退款
    String cash_refund = "supermarket/cash_refund";
    //支付宝退款
    String order_refund = "supermarket/order_refund";
    //微信退款
    String wx_refund = "index/fwswddrefundOrder";
    //交班列表
    String handoverList = "Supermarket/handoverList";
    //交班
    String shiftHandover = "Supermarket/shiftHandover1";
    //查询微信支付状态
    String fwsgetOrderInformation = "Supermarket/fwsgetOrderInformation";
    //撤销微信支付订单
    String fwscancelanOrder = "Supermarket/fwscancelanOrder";
    //查询支付宝支付状态
    String queryOrder = "supermarket/queryOrder";
    //撤销支付宝支付订单
    String revokeOrder = "supermarket/revokeOrder";

    //添加无码商品
    String addNoCode = "supermarket/addNoCode";

}
