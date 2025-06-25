package com.uhm.uhmcs.bean;

import java.io.Serializable;
import java.util.List;


public class ClubCardBean implements Serializable {

    /**
     * code : 1
     * msg : 会员卡信息
     * time : 1750415865
     * data : [{"id":1,"block_id":null,"number":"2481506871","amount":"0.00","status":1,"custom_id":null,"shop_id":"DP2025021528377"}]
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
         * id : 1
         * block_id : null
         * number : 2481506871
         * amount : 0.00
         * status : 1
         * custom_id : null
         * shop_id : DP2025021528377
         */

        private String id;
        private String block_id;
        private String number;
        private String amount;
        private String status;
        private String custom_id;
        private String shop_id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBlock_id() {
            return block_id;
        }

        public void setBlock_id(String block_id) {
            this.block_id = block_id;
        }

        public String getNumber() {
            return number;
        }

        public void setNumber(String number) {
            this.number = number;
        }

        public String getAmount() {
            return amount;
        }

        public void setAmount(String amount) {
            this.amount = amount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getCustom_id() {
            return custom_id;
        }

        public void setCustom_id(String custom_id) {
            this.custom_id = custom_id;
        }

        public String getShop_id() {
            return shop_id;
        }

        public void setShop_id(String shop_id) {
            this.shop_id = shop_id;
        }
    }
}
