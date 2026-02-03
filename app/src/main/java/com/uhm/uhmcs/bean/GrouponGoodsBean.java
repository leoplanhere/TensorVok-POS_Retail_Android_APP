package com.uhm.uhmcs.bean;

import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;

public class GrouponGoodsBean implements Serializable {
    @SerializedName("code")
    private int code;
    @SerializedName("msg")
    private String msg;
    @SerializedName("time")
    private String time;

    @SerializedName("data")
    private DataWrapper data;

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

    public DataWrapper getData() {
        return data;
    }

    public void setData(DataWrapper data) {
        this.data = data;
    }

    // 中间层包装类
    public static class DataWrapper implements Serializable {
        @SerializedName("data")
        private ArrayList<GrouponGoodsModel> goodsList;

        @SerializedName("pagination")
        private Pagination pagination;

        public ArrayList<GrouponGoodsModel> getGoodsList() {
            return goodsList;
        }

        public void setGoodsList(ArrayList<GrouponGoodsModel> goodsList) {
            this.goodsList = goodsList;
        }

        public Pagination getPagination() {
            return pagination;
        }

        public void setPagination(Pagination pagination) {
            this.pagination = pagination;
        }
    }

    // 分页信息类
    public static class Pagination implements Serializable {
        private int total;
        private String page;
        private String strip;
        private int start;
        private int totalpage;

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public String getPage() { return page; }
        public void setPage(String page) { this.page = page; }
        public String getStrip() { return strip; }
        public void setStrip(String strip) { this.strip = strip; }
        public int getStart() { return start; }
        public void setStart(int start) { this.start = start; }
        public int getTotalpage() { return totalpage; }
        public void setTotalpage(int totalpage) { this.totalpage = totalpage; }
    }

    // 商品实体类
    public static class GrouponGoodsModel extends LitePalSupport implements Serializable {

        // LitePal 默认本地主键，保持自增即可，不要手动干预
        @SerializedName("litepal_id")
        private int local_db_id;

        // ★ 后台数字 ID (如 54142)
        @SerializedName("id")
        private int pid;

        // 字符串 ID (如 SPDP...)，仅做普通索引，不做唯一约束，防止因 ID 重复导致的数据覆盖
        @Column(index = true)
        private String goods_id;

        // 店铺 ID
        @SerializedName("shop_id")
        private String shop_id;

        // 条形码映射
        @Column(index = true)
        @SerializedName(value = "sn", alternate = {"barcode", "bar_code", "code"})
        private String sn;

        // 商品内部编码
        private String goods_sn;

        private int ggspid;
        private int goods_sku_price_id;

        // 商品标题
        @SerializedName(value = "title", alternate = {"goods_name", "name", "product_name", "goods_title"})
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
        private String updatetime;

        // --- 标签打印新增属性 ---
        @SerializedName("specs_title")
        private String specs_title;      // 规格

        @SerializedName("unit")
        private String unit;             // 单位

        @SerializedName("reward_points")
        private double reward_points;    // 积分

        @SerializedName("deduction_golive")
        private double deduction_golive; // 抵扣金额/券

        @Column(ignore = true)
        private BigDecimal heji = new BigDecimal("0.00");

        private String flname;
        private int shuliang = 1;
        private boolean is_zengsong = false;
        private boolean isSelected = false;
        private String discount = "100";

        @Column(ignore = true)
        private BigDecimal discounted_price = new BigDecimal("0.00");

        // --- Getters and Setters ---

        public int getId() {
            return local_db_id;
        }

        public void setId(int id) {
            this.local_db_id = local_db_id;
        }

        public int getPid() {
            return pid;
        }

        public void setPid(int pid) {
            this.pid = pid;
        }

        public String getGoods_id() {
            return goods_id;
        }

        public void setGoods_id(String goods_id) {
            this.goods_id = goods_id;
        }

        public String getShop_id() {
            return shop_id;
        }

        public void setShop_id(String shop_id) {
            this.shop_id = shop_id;
        }

        public String getSn() {
            return sn;
        }

        public void setSn(String sn) {
            this.sn = sn;
        }

        public String getGoods_sn() {
            return goods_sn;
        }

        public void setGoods_sn(String goods_sn) {
            this.goods_sn = goods_sn;
        }

        public String getUpdatetime() {
            return updatetime;
        }

        public void setUpdatetime(String updatetime) {
            this.updatetime = updatetime;
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

        public void setGoods_sku_price_id(int goods_sku_price_id) {
            this.goods_sku_price_id = goods_sku_price_id;
        }

        public void setPay_price(String pay_price) {
            this.pay_price = pay_price;
        }

        public int getGgspid() {
            return ggspid;
        }

        public void setGgspid(int ggspid) {
            this.ggspid = ggspid;
        }

        public int getGoods_sku_price_id() { return goods_sku_price_id; }

        public String getPay_price() { return pay_price; }

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

        public String getPrice() {
            if (!TextUtils.isEmpty(ggprice)){
                return ggprice;
            } else {
                return price;
            }
        }

        public void setPrice(String price) {
            this.price = price;
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


        // --- 标签打印属性的 Getter 和 Setter ---

        public String getSpecs_title() {
            return TextUtils.isEmpty(specs_title) ? "" : specs_title;
        }

        public void setSpecs_title(String specs_title) {
            this.specs_title = specs_title;
        }

        public String getUnit() {
            return TextUtils.isEmpty(unit) ? "1" : unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public double getReward_points() {
            return reward_points;
        }

        public void setReward_points(double reward_points) {
            this.reward_points = reward_points;
        }

        public double getDeduction_golive() {
            return deduction_golive;
        }

        public void setDeduction_golive(double deduction_golive) {
            this.deduction_golive = deduction_golive;
        }


    }
}