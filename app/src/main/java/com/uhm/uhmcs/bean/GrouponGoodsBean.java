package com.uhm.uhmcs.bean;

import android.text.TextUtils;
import com.google.gson.annotations.SerializedName;
import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 完整重构版 GrouponGoodsBean
 * 1. 适配 LitePal 数据库索引
 * 2. 兼容根节点 sn 与 skuPrice 嵌套数据
 * 3. 严格保留原有 460+ 行所有业务逻辑，无任何省略
 */
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

    /**
     * 分页包装类
     */
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

    /**
     * 分页信息类
     */
    public static class Pagination implements Serializable {
        private int total;
        private int totalpage;
        private String page;
        private String strip;

        public int getTotal() { return total; }
        public void setTotal(int total) { this.total = total; }
        public int getTotalpage() { return totalpage; }
        public void setTotalpage(int totalpage) { this.totalpage = totalpage; }
        public String getPage() { return page; }
        public void setPage(String page) { this.page = page; }
        public String getStrip() { return strip; }
        public void setStrip(String strip) { this.strip = strip; }
    }

    /**
     * 商品实体类
     * 继承 LitePalSupport 以实现本地高速缓存
     */
    public static class GrouponGoodsModel extends LitePalSupport implements Serializable {

        @SerializedName("id")
        private String serverId; // 服务器返回的原始ID，通过 SerializedName 映射避免与 LitePal 内部 ID 冲突

        private String goods_id;
        private String goods_sn;

        @SerializedName("barcode")
        private String barcode; // 兼容部分接口使用的 barcode 字段

        @Column(index = true)
        @SerializedName("sn")
        private String sn; // 核心条形码，增加数据库索引提升扫码查询速度


        private String pinyin;    // 存储全拼，如 "pingguo"
        private String pyInitial; // 存储首字母，如 "pg"

        // 补齐 Getter/Setter
        public String getPinyin() { return pinyin; }
        public void setPinyin(String pinyin) { this.pinyin = pinyin; }
        public String getPyInitial() { return pyInitial; }
        public void setPyInitial(String pyInitial) { this.pyInitial = pyInitial; }



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
        private String flname;
        private String reward_points;
        private String deduction_golive;
        private String company;
        private String goods_weight;
        private String unit;
        private String specs_title;
        private String online_type;

        // --- 用于解析嵌套在数组里的 SKU 信息的字段 ---
        @SerializedName("skuPrice")
        private ArrayList<SkuPriceBean> skuPrice;

        public ArrayList<SkuPriceBean> getSkuPrice() {
            return skuPrice;
        }

        public void setSkuPrice(ArrayList<SkuPriceBean> skuPrice) {
            this.skuPrice = skuPrice;
        }

        // 增加这个 Getter 方便同步时取值
        public String getBarcode() { return barcode; }

        /**
         * 内部类：对应接口返回的 skuPrice 数组项
         */
        public static class SkuPriceBean implements Serializable {
            private String id;
            private String sn;
            public String getId() { return id; }
            public void setId(String id) { this.id = id; }
            public String getSn() { return sn; }
            public void setSn(String sn) { this.sn = sn; }
        }

        // --- 交易运行时字段 (标记为 ignore 不存入库存数据库) ---
        @Column(ignore = true)
        private BigDecimal heji = new BigDecimal("0.00");
        @Column(ignore = true)
        private int shuliang = 1;
        @Column(ignore = true)
        private boolean is_zengsong = false;
        @Column(ignore = true)
        private boolean isSelected = false;
        @Column(ignore = true)
        private String discount = "100";
        @Column(ignore = true)
        private BigDecimal discounted_price = new BigDecimal("0.00");

        // --- Getter & Setter 方法（全量保留，无一省略） ---

        public void setBarcode(String barcode) { this.barcode = barcode; }

        public String getOnline_type() {
            return online_type;
        }

        public void setOnline_type(String online_type) {
            this.online_type = online_type;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getSpecs_title() {
            return specs_title;
        }

        public void setSpecs_title(String specs_title) {
            this.specs_title = specs_title;
        }

        public String getGoods_weight() {
            return goods_weight;
        }

        public void setGoods_weight(String goods_weight) {
            this.goods_weight = goods_weight;
        }

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

        // --- 核心业务判定逻辑（严格保留） ---

        public String getPrice() {
            // 判定逻辑：如果存在非空的规格价，返回规格价，否则返回基础价
            if (!TextUtils.isEmpty(ggprice) && !ggprice.equals("0.00") && !ggprice.equals("0")) {
                return ggprice;
            } else {
                return price;
            }
        }

        public void setPrice(String price) {
            if (!TextUtils.isEmpty(ggprice)) {
                this.ggprice = price;
            } else {
                this.price = price;
            }
        }

        public String getId() {
            // 判定逻辑：优先返回规格ID，无规格ID时返回原始服务器ID
            if (TextUtils.isEmpty(ggspid)) {
                return serverId;
            } else {
                return ggspid;
            }
        }

        public String getIds() {
            return serverId;
        }

        public void setId(String id) {
            this.serverId = id;
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