package com.uhm.uhmcs.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

public class GrouponGoodsBean implements Serializable{
    @SerializedName("code")
    private int code;
    @SerializedName("msg")
    private String msg;
    @SerializedName("time")
    private String time;
    @SerializedName("data")
    private ArrayList<GrouponGoodsModel> data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ArrayList<GrouponGoodsModel> getData() {
        return data;
    }

    public void setData(ArrayList<GrouponGoodsModel> data) {
        this.data = data;
    }

    public class DataModel{
        @SerializedName("data")
        private ArrayList<GrouponGoodsModel> data;

        public ArrayList<GrouponGoodsModel> getData() {
            return data;
        }

        public void setData(ArrayList<GrouponGoodsModel> data) {
            this.data = data;
        }
    }

    public static class GrouponGoodsModel implements Serializable  {


        private String id;


        private String goods_id;
        private String goods_sn;

        private String sn;

        private String ggspid;

        private int goods_sku_price_id;

        private String title;

        private String image;

        private String price;

        private String ggprice;

        private String pay_price;

        private String goods_sku_text;

        private String goods_sku_ids;

        private String category_ids;

        private String cost_price;

        private String original_price;

        private String subtitle;

        private BigDecimal heji=new BigDecimal("0.00");

        private String flname;



        private int shuliang=1;
        private boolean is_zengsong=false;

        private boolean isSelected=false;

        private String discount="100";

        private String reward_points;

        private String deduction_golive;

        private String company;

        public String getCompany() {
            return company;
        }

        public void setCompany(String company) {
            this.company = company;
        }

        public String getReward_points() {
            return reward_points;
        }

        public void setReward_points(String reward_points) {
            this.reward_points = reward_points;
        }

        public String getDeduction_golive() {
            return deduction_golive;
        }

        public void setDeduction_golive(String deduction_golive) {
            this.deduction_golive = deduction_golive;
        }

        public String getGgprice() {
            return ggprice;
        }

        public void setGgprice(String ggprice) {
            this.ggprice = ggprice;
        }

        public String getCost_price() {
            return cost_price;
        }

        public void setCost_price(String cost_price) {
            this.cost_price = cost_price;
        }

        public String getOriginal_price() {
            return original_price;
        }

        public void setOriginal_price(String original_price) {
            this.original_price = original_price;
        }

        public String getCategory_ids() {
            return category_ids;
        }

        public void setCategory_ids(String category_ids) {
            this.category_ids = category_ids;
        }

        public String getGoods_sku_ids() {
            return goods_sku_ids;
        }

        public void setGoods_sku_ids(String goods_sku_ids) {
            this.goods_sku_ids = goods_sku_ids;
        }

        public String getGoods_sku_text() {
            return goods_sku_text;
        }

        public void setGoods_sku_text(String goods_sku_text) {
            this.goods_sku_text = goods_sku_text;
        }




        private BigDecimal discounted_price=new BigDecimal("0.00");

        public void setGoods_id(String goods_id) {

            this.goods_id = goods_id;
        }

        public void setGoods_sku_price_id(int goods_sku_price_id) {
            this.goods_sku_price_id = goods_sku_price_id;
        }

        public void setPay_price(String pay_price) {
            this.pay_price = pay_price;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getGgspid() {
            return ggspid;
        }

        public void setGgspid(String ggspid) {
            this.ggspid = ggspid;
        }

        public String getGoods_sku_price_id() {
            return ggspid;
        }



        public String getPay_price() {
            return price;
        }


        public String getDiscount() {
            return discount;
        }

        public void setDiscount(String discount) {
            this.discount = discount;
        }

        public BigDecimal getDiscounted_price() {
            return discounted_price;
        }

        public void setDiscounted_price(BigDecimal discounted_price) {
            this.discounted_price = discounted_price;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public int getShuliang() {
            return shuliang;
        }

        public void setShuliang(int shuliang) {
            this.shuliang = shuliang;
        }

        public BigDecimal getHeji() {
            return heji;
        }

        public void setHeji(BigDecimal heji) {
            this.heji = heji;
        }

        public boolean isIs_zengsong() {
            return is_zengsong;
        }

        public void setIs_zengsong(boolean is_zengsong) {
            this.is_zengsong = is_zengsong;
        }

        public String getSubtitle() {
            return subtitle;
        }

        public void setSubtitle(String subtitle) {
            this.subtitle = subtitle;
        }

        public String getFlname() {
            return flname;
        }

        public void setFlname(String flname) {
            this.flname = flname;
        }

        public String getGoods_id() {
            return goods_id;
        }

        public String getPrice() {
            if (!TextUtils.isEmpty(ggprice)){
                return ggprice;
            }else {
                return price;
            }

        }

        public void setPrice(String price) {
            if (!TextUtils.isEmpty(ggprice)){
                this.ggprice = price;
            }else {
                this.price = price;
            }

        }

        public String getId() {
            if (TextUtils.isEmpty(ggspid)){
                return id;
            }else {
                return ggspid;
            }

        }
        public String getIds() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getGoods_sn() {
            return goods_sn;
        }

        public void setGoods_sn(String goods_sn) {
            this.goods_sn = goods_sn;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }




    }
}
