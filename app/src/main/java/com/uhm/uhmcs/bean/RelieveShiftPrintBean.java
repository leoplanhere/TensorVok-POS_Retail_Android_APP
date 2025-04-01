package com.uhm.uhmcs.bean;

import java.util.ArrayList;

public class RelieveShiftPrintBean {
   private int id;
    private String shift_id;
    private int user_id;
    private long createtime;
    private String turnover;
    private String refund;
    private ArrayList<LblisstBean> lblisst;

    private ArrayList<SplisstBean> splisst;
    private ArrayList<ThlisstBean> thlisst;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShift_id() {
        return shift_id;
    }

    public void setShift_id(String shift_id) {
        this.shift_id = shift_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public long getCreatetime() {
        return createtime;
    }

    public void setCreatetime(long createtime) {
        this.createtime = createtime;
    }

    public String getTurnover() {
        return turnover;
    }

    public void setTurnover(String turnover) {
        this.turnover = turnover;
    }

    public String getRefund() {
        return refund;
    }

    public void setRefund(String refund) {
        this.refund = refund;
    }

    public ArrayList<LblisstBean> getLblisst() {
        return lblisst;
    }

    public void setLblisst(ArrayList<LblisstBean> lblisst) {
        this.lblisst = lblisst;
    }

    public ArrayList<SplisstBean> getSplisst() {
        return splisst;
    }

    public void setSplisst(ArrayList<SplisstBean> splisst) {
        this.splisst = splisst;
    }

    public ArrayList<ThlisstBean> getThlisst() {
        return thlisst;
    }

    public void setThlisst(ArrayList<ThlisstBean> thlisst) {
        this.thlisst = thlisst;
    }

    class LblisstBean{
       private String flname;
       private  String sum;
       private  int count;

        public String getFlname() {
            return flname;
        }

        public void setFlname(String flname) {
            this.flname = flname;
        }

        public String getSum() {
            return sum;
        }

        public void setSum(String sum) {
            this.sum = sum;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
   class SplisstBean{
       private  String title;
       private int goods_num;
       private String goods_weight;
       private String pay_price;

       public String getTitle() {
           return title;
       }

       public void setTitle(String title) {
           this.title = title;
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

       public String getPay_price() {
           return pay_price;
       }

       public void setPay_price(String pay_price) {
           this.pay_price = pay_price;
       }
   }
    class ThlisstBean{
        private  String title;
        private int goods_num;
        private String goods_weight;
        private String pay_price;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
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

        public String getPay_price() {
            return pay_price;
        }

        public void setPay_price(String pay_price) {
            this.pay_price = pay_price;
        }
    }
}
