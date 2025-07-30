package com.uhm.uhmcs.bean;

import java.io.Serializable;
import java.util.List;


public class RelieveShiftPrintBean implements Serializable {

    /**
     * code : 1
     * msg : 交班成功
     * time : 1753435177
     * data : {"nickname":"优海猫测试用","logintime":1753434588,"endtime":1753435177,"total":[{"pay_type":"cash","total":"0.03"},{"pay_type":"wallet","total":"0.01"},{"pay_type":"wechat","total":"0.01"},{"pay_type":"alipay","total":"0.01"}],"refund":[{"pay_type":"wechat","total":"0.01"},{"pay_type":"alipay","total":"0.01"}]}
     */

    private int code;
    private String msg;
    private String time;
    private DataBean data;

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

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean implements Serializable {
        /**
         * nickname : 优海猫测试用
         * logintime : 1753434588
         * endtime : 1753435177
         * total : [{"pay_type":"cash","total":"0.03"},{"pay_type":"wallet","total":"0.01"},{"pay_type":"wechat","total":"0.01"},{"pay_type":"alipay","total":"0.01"}]
         * refund : [{"pay_type":"wechat","total":"0.01"},{"pay_type":"alipay","total":"0.01"}]
         */

        private String nickname;
        private int logintime;
        private int endtime;
        private List<TotalBean> total;
        private List<RefundBean> refund;

        public String getNickname() {
            return nickname;
        }

        public void setNickname(String nickname) {
            this.nickname = nickname;
        }

        public int getLogintime() {
            return logintime;
        }

        public void setLogintime(int logintime) {
            this.logintime = logintime;
        }

        public int getEndtime() {
            return endtime;
        }

        public void setEndtime(int endtime) {
            this.endtime = endtime;
        }

        public List<TotalBean> getTotal() {
            return total;
        }

        public void setTotal(List<TotalBean> total) {
            this.total = total;
        }

        public List<RefundBean> getRefund() {
            return refund;
        }

        public void setRefund(List<RefundBean> refund) {
            this.refund = refund;
        }

        public static class TotalBean implements Serializable {
            /**
             * pay_type : cash
             * total : 0.03
             */

            private String pay_type;
            private String total;

            public String getPay_type() {
                return pay_type;
            }

            public void setPay_type(String pay_type) {
                this.pay_type = pay_type;
            }

            public String getTotal() {
                return total;
            }

            public void setTotal(String total) {
                this.total = total;
            }
        }

        
        public static class RefundBean implements Serializable {
            /**
             * pay_type : wechat
             * total : 0.01
             */

            private String pay_type;
            private String total;

            public String getPay_type() {
                return pay_type;
            }

            public void setPay_type(String pay_type) {
                this.pay_type = pay_type;
            }

            public String getTotal() {
                return total;
            }

            public void setTotal(String total) {
                this.total = total;
            }
        }
    }
}
