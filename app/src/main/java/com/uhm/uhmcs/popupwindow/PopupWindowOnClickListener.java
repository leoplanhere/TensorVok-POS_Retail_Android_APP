package com.uhm.uhmcs.popupwindow;

import android.view.View;

import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.MemberBean;

import java.util.ArrayList;

public class PopupWindowOnClickListener {
    public interface DeleteShopOnClickListener {
        /**
         * Called when a view has been clicked.
         *
         */
        void onClick(String text);
    }
    public interface MorefunctionOnClickListener {
        /**
         * Called when a view has been clicked.
         *
         */
        void onClick(int btnType);
    }
    public interface GoodsWarehousingOnClickListener {
        /**
         * Called when a view has been clicked.
         *
         */
        void onClick(int code,String msg);
    }
    public interface ShopOnClickListener {
        /**
         * Called when a view has been clicked.
         *
         */
        void onClick(ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList);
    }


    public interface CheckoutOnClickListener {
        /**
         * Called when a view has been clicked.
         *
         */
        void onClick();
    }
    public interface MemberOnClickListener {
        /**
         * Called when a view has been clicked.
         *
         */
        void onClick(MemberBean memberBean);
    }
    public interface TimeOnClickListener {
        /**
         * Called when a view has been clicked.
         *
         */
        void onClick(String startTime,String endTime);
    }

    public interface GetRegistrationShopOnClickListener {
        /**
         * Called when a view has been clicked.
         *
         */
        void onClick(int index,int type);
    }
    public interface DiscountOnClickListener {
        /**
         * Called when a view has been clicked.
         *
         */
        void onClick(String discount);
    }
}
