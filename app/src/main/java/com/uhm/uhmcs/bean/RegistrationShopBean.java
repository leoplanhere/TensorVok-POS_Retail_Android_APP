package com.uhm.uhmcs.bean;

import java.math.BigDecimal;
import java.util.ArrayList;

public class RegistrationShopBean {
    private String time;
    private BigDecimal total_price;

    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> registrationShopList;

    private boolean isSelected=false;


    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public BigDecimal getTotal_price() {
        return total_price;
    }

    public void setTotal_price(BigDecimal total_price) {
        this.total_price = total_price;
    }

    public ArrayList<GrouponGoodsBean.GrouponGoodsModel> getRegistrationShopList() {
        return registrationShopList;
    }

    public void setRegistrationShopList(ArrayList<GrouponGoodsBean.GrouponGoodsModel> registrationShopList) {
        this.registrationShopList = registrationShopList;
    }
}
