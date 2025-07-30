package com.uhm.uhmcs.bean;

public class RelieveShiftBean {
    private int createtime;
    private int id;
    private String nickname;
    private String refund;
    private String shift_id;
    private String turnover;
    private int user_id;

    private int logintime;

    private String refundjson;

    private String paymentjson;


    public int getLogintime() {
        return logintime;
    }

    public void setLogintime(int logintime) {
        this.logintime = logintime;
    }

    public String getRefundjson() {
        return refundjson;
    }

    public void setRefundjson(String refundjson) {
        this.refundjson = refundjson;
    }

    public String getPaymentjson() {
        return paymentjson;
    }

    public void setPaymentjson(String paymentjson) {
        this.paymentjson = paymentjson;
    }

    public int getCreatetime() {
        return createtime;
    }

    public void setCreatetime(int createtime) {
        this.createtime = createtime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getRefund() {
        return refund;
    }

    public void setRefund(String refund) {
        this.refund = refund;
    }

    public String getShift_id() {
        return shift_id;
    }

    public void setShift_id(String shift_id) {
        this.shift_id = shift_id;
    }

    public String getTurnover() {
        return turnover;
    }

    public void setTurnover(String turnover) {
        this.turnover = turnover;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }
}
