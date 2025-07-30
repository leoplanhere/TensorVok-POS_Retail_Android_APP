package com.uhm.uhmcs.bean;

import java.math.BigDecimal;
import java.util.ArrayList;

public class CheckoutBean {
    public String member_name;
    public String cardnumber;

    public String member_phone;
    public String goodsjson;
    public int user_id;
    public String machineNumber;
    public String pay_type;
    public String total_amount;
    public String total_fee;
    public String discount_fee;
    public String coupon_fee;
    public String pay_fee;
    public String goods_original_amount;
    public String authCode;
    public String shop_id;

    public String cash_change;

    public String cash_price;

    public int type;

    public int order_status;
    public String order_sn;

    public int allNum;

    public String transaction_id;

    public String pay_time;

    public String xf_type;

    public String getXf_type() {
        return xf_type;
    }

    public void setXf_type(String xf_type) {
        this.xf_type = xf_type;
    }

    public String getPay_time() {
        return pay_time;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    public String getTransaction_id() {
        return transaction_id;
    }

    public void setTransaction_id(String transaction_id) {
        this.transaction_id = transaction_id;
    }

    public int getAllNum() {
        return allNum;
    }

    public void setAllNum(int allNum) {
        this.allNum = allNum;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getOrder_status() {
        return order_status;
    }

    public void setOrder_status(int order_status) {
        this.order_status = order_status;
    }

    public String getOrder_sn() {
        return order_sn;
    }

    public void setOrder_sn(String order_sn) {
        this.order_sn = order_sn;
    }

    public String getCash_price() {
        return cash_price;
    }

    public void setCash_price(String cash_price) {
        this.cash_price = cash_price;
    }

    public String getCash_change() {
        return cash_change;
    }

    public void setCash_change(String cash_change) {
        this.cash_change = cash_change;
    }

    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    public String getCardnumber() {
        return cardnumber;
    }

    public void setCardnumber(String cardnumber) {
        this.cardnumber = cardnumber;
    }

    public String getMember_phone() {
        return member_phone;
    }

    public void setMember_phone(String member_phone) {
        this.member_phone = member_phone;
    }


    public void setGoodsjson(String goodsjson) {
        this.goodsjson = goodsjson;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getMachineNumber() {
        return machineNumber;
    }

    public void setMachineNumber(String machineNumber) {
        this.machineNumber = machineNumber;
    }

    public String getPay_type() {
        return pay_type;
    }

    public String getGoodsjson() {
        return goodsjson;
    }

    public void setPay_type(String pay_type) {
        this.pay_type = pay_type;
    }

    public String getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(String total_amount) {
        this.total_amount = total_amount;
    }

    public String getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(String total_fee) {
        this.total_fee = total_fee;
    }

    public String getDiscount_fee() {
        return discount_fee;
    }

    public void setDiscount_fee(String discount_fee) {
        this.discount_fee = discount_fee;
    }

    public String getCoupon_fee() {
        return coupon_fee;
    }

    public void setCoupon_fee(String coupon_fee) {
        this.coupon_fee = coupon_fee;
    }

    public String getPay_fee() {
        return pay_fee;
    }

    public void setPay_fee(String pay_fee) {
        this.pay_fee = pay_fee;
    }

    public String getGoods_original_amount() {
        return goods_original_amount;
    }

    public void setGoods_original_amount(String goods_original_amount) {
        this.goods_original_amount = goods_original_amount;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getShop_id() {
        return shop_id;
    }

    public void setShop_id(String shop_id) {
        this.shop_id = shop_id;
    }

     public static class GoodsJsonBean{
        public String goods_id;
        public String title;
        public String goods_sn;
        public String sn;
        public String discount;

        public String discounted_price;

        public String goods_price;
        public int goods_num;

        public String goods_weight;
        public int weigh_id;
        public String pay_price;
        public String goods_sku_price_id;

         private String goods_sku_text;



        public String getGoods_id() {
            return goods_id;
        }

        public void setGoods_id(String goods_id) {
            this.goods_id = goods_id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getGoods_sn() {
            return goods_sn;
        }

        public void setGoods_sn(String goods_sn) {
            this.goods_sn = goods_sn;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }


        public int getGoods_num() {
            return goods_num;
        }

        public void setGoods_num(int goods_num) {
            this.goods_num = goods_num;
        }

        public String getGoods_weight() {
            return goods_weight;
        }

        public void setGoods_weight(String goods_weight) {
            this.goods_weight = goods_weight;
        }

        public int getWeigh_id() {
            return weigh_id;
        }

        public void setWeigh_id(int weigh_id) {
            this.weigh_id = weigh_id;
        }

         public String getDiscount() {
             return discount;
         }

         public void setDiscount(String discount) {
             this.discount = discount;
         }

         public String getDiscounted_price() {
             return discounted_price;
         }

         public void setDiscounted_price(String discounted_price) {
             this.discounted_price = discounted_price;
         }

         public String getGoods_price() {
             return goods_price;
         }

         public void setGoods_price(String goods_price) {
             this.goods_price = goods_price;
         }

         public String getPay_price() {
             return pay_price;
         }

         public void setPay_price(String pay_price) {
             this.pay_price = pay_price;
         }

         public String getGoods_sku_price_id() {
            return goods_sku_price_id;
        }

        public void setGoods_sku_price_id(String goods_sku_price_id) {
            this.goods_sku_price_id = goods_sku_price_id;
        }

         public String getGoods_sku_text() {
             return goods_sku_text;
         }

         public void setGoods_sku_text(String goods_sku_text) {
             this.goods_sku_text = goods_sku_text;
         }
     }
}
