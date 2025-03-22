package com.uhm.uhmcs.bean;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ShopDataBean {
    @SerializedName("code")
    private int code;
    @SerializedName("msg")
    private String msg;
    @SerializedName("time")
    private String time;
    @SerializedName("data")
    private ArrayList<ShopData> data;


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

    public ArrayList<ShopData> getData() {
        return data;
    }

    public void setData(ArrayList<ShopData> data) {
        this.data = data;
    }

    public class ShopData{
        public int id;
        public String shopuid;

        public String name;
        public String shoppeople;
        public String phone;
        public String address;

        @SerializedName("switch")
        public int switch_1;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getShopuid() {
            return shopuid;
        }

        public void setShopuid(String shopuid) {
            this.shopuid = shopuid;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getShoppeople() {
            return shoppeople;
        }

        public void setShoppeople(String shoppeople) {
            this.shoppeople = shoppeople;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getSwitch_1() {
            return switch_1;
        }

        public void setSwitch_1(int switch_1) {
            this.switch_1 = switch_1;
        }
    }
}
