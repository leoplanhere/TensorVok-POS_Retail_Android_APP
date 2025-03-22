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
    public int total_amount;
    public int total_fee;
    public int discount_fee;
    public int coupon_fee;
    public int pay_fee;
    public int goods_original_amount;
    public String authCode;
    public String shop_id;

    public String cash_change;

    public String cash_price;



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

    public int getTotal_amount() {
        return total_amount;
    }

    public void setTotal_amount(int total_amount) {
        this.total_amount = total_amount;
    }

    public int getTotal_fee() {
        return total_fee;
    }

    public void setTotal_fee(int total_fee) {
        this.total_fee = total_fee;
    }

    public int getDiscount_fee() {
        return discount_fee;
    }

    public void setDiscount_fee(int discount_fee) {
        this.discount_fee = discount_fee;
    }

    public int getCoupon_fee() {
        return coupon_fee;
    }

    public void setCoupon_fee(int coupon_fee) {
        this.coupon_fee = coupon_fee;
    }

    public int getPay_fee() {
        return pay_fee;
    }

    public void setPay_fee(int pay_fee) {
        this.pay_fee = pay_fee;
    }

    public int getGoods_original_amount() {
        return goods_original_amount;
    }

    public void setGoods_original_amount(int goods_original_amount) {
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
        public int goods_id;
        public String title;
        public String goods_sn;
        public String sn;
        public String discount;

        public String discounted_price;

        public String goods_price;
        public int goods_num;

        public int goods_weight;
        public int weigh_id;
        public String pay_price;
        public String goods_sku_price_id;

         private String goods_sku_text;



        public int getGoods_id() {
            return goods_id;
        }

        public void setGoods_id(int goods_id) {
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

        public int getGoods_weight() {
            return goods_weight;
        }

        public void setGoods_weight(int goods_weight) {
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
