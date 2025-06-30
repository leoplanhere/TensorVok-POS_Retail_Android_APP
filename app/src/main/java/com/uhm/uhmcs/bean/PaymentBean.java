package com.uhm.uhmcs.bean;

import java.io.Serializable;
import java.util.List;


public class PaymentBean implements Serializable {


    /**
     * code : 1
     * msg : 配置列表
     * time : 1751249123
     * data : [{"id":21,"shop_id":"DP2025021528377","start":null,"end":null,"merchant":"1690693014","wxappid":"wx959f486ed2155fdf","wxsecret":"fd31cf54986e14950b400316f89a8c5f","zfbappid":"1","zfbsecret":"1","gzhappid":"","gzhsecret":"","xcxappid":"","xcxsecret":"","sub_mch_id":"1695111499","sub_appid":"","sub_app_id":"wx9f783e66d50a2005","sub_miniapp_id":"","zh_default":1,"zfb_public_key":null,"app_auth_token":null}]
     */

    private int code;
    private String msg;
    private String time;
    private List<DataBean> data;

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

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean implements Serializable {
        /**
         * id : 21
         * shop_id : DP2025021528377
         * start : null
         * end : null
         * merchant : 1690693014
         * wxappid : wx959f486ed2155fdf
         * wxsecret : fd31cf54986e14950b400316f89a8c5f
         * zfbappid : 1
         * zfbsecret : 1
         * gzhappid :
         * gzhsecret :
         * xcxappid :
         * xcxsecret :
         * sub_mch_id : 1695111499
         * sub_appid :
         * sub_app_id : wx9f783e66d50a2005
         * sub_miniapp_id :
         * zh_default : 1
         * zfb_public_key : null
         * app_auth_token : null
         */

        private int id;
        private String shop_id;
        private String start;
        private String end;
        private String merchant;
        private String wxappid;
        private String wxsecret;
        private String zfbappid;
        private String zfbsecret;
        private String gzhappid;
        private String gzhsecret;
        private String xcxappid;
        private String xcxsecret;
        private String sub_mch_id;
        private String sub_appid;
        private String sub_app_id;
        private String sub_miniapp_id;
        private int zh_default;
        private String zfb_public_key;
        private String app_auth_token;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getShop_id() {
            return shop_id;
        }

        public void setShop_id(String shop_id) {
            this.shop_id = shop_id;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public String getMerchant() {
            return merchant;
        }

        public void setMerchant(String merchant) {
            this.merchant = merchant;
        }

        public String getWxappid() {
            return wxappid;
        }

        public void setWxappid(String wxappid) {
            this.wxappid = wxappid;
        }

        public String getWxsecret() {
            return wxsecret;
        }

        public void setWxsecret(String wxsecret) {
            this.wxsecret = wxsecret;
        }

        public String getZfbappid() {
            return zfbappid;
        }

        public void setZfbappid(String zfbappid) {
            this.zfbappid = zfbappid;
        }

        public String getZfbsecret() {
            return zfbsecret;
        }

        public void setZfbsecret(String zfbsecret) {
            this.zfbsecret = zfbsecret;
        }

        public String getGzhappid() {
            return gzhappid;
        }

        public void setGzhappid(String gzhappid) {
            this.gzhappid = gzhappid;
        }

        public String getGzhsecret() {
            return gzhsecret;
        }

        public void setGzhsecret(String gzhsecret) {
            this.gzhsecret = gzhsecret;
        }

        public String getXcxappid() {
            return xcxappid;
        }

        public void setXcxappid(String xcxappid) {
            this.xcxappid = xcxappid;
        }

        public String getXcxsecret() {
            return xcxsecret;
        }

        public void setXcxsecret(String xcxsecret) {
            this.xcxsecret = xcxsecret;
        }

        public String getSub_mch_id() {
            return sub_mch_id;
        }

        public void setSub_mch_id(String sub_mch_id) {
            this.sub_mch_id = sub_mch_id;
        }

        public String getSub_appid() {
            return sub_appid;
        }

        public void setSub_appid(String sub_appid) {
            this.sub_appid = sub_appid;
        }

        public String getSub_app_id() {
            return sub_app_id;
        }

        public void setSub_app_id(String sub_app_id) {
            this.sub_app_id = sub_app_id;
        }

        public String getSub_miniapp_id() {
            return sub_miniapp_id;
        }

        public void setSub_miniapp_id(String sub_miniapp_id) {
            this.sub_miniapp_id = sub_miniapp_id;
        }

        public int getZh_default() {
            return zh_default;
        }

        public void setZh_default(int zh_default) {
            this.zh_default = zh_default;
        }

        public String getZfb_public_key() {
            return zfb_public_key;
        }

        public void setZfb_public_key(String zfb_public_key) {
            this.zfb_public_key = zfb_public_key;
        }

        public String getApp_auth_token() {
            return app_auth_token;
        }

        public void setApp_auth_token(String app_auth_token) {
            this.app_auth_token = app_auth_token;
        }
    }
}
