package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.GuaDanAdapter;
import com.uhm.uhmcs.adapter.GuaDanShopAdapter;
import com.uhm.uhmcs.adapter.SelectedShopAdapter;
import com.uhm.uhmcs.bean.RegistrationShopBean;

import java.util.ArrayList;

public class GetRegistrationShopPopupWindow {
    private PopupWindow popupWindow;
    private Context context;
    private PopupWindowOnClickListener.GetRegistrationShopOnClickListener listener;

    private ArrayList<RegistrationShopBean> registrationShopBeanArrayList;
    private int index=0;

    private RecyclerView guadan_rv,guadan_shop_rv;
    private GuaDanAdapter guaDanAdapter;
    private GuaDanShopAdapter guaDanShopAdapter;

    public GetRegistrationShopPopupWindow(Context context,ArrayList<RegistrationShopBean> registrationShopBeanArrayList,  PopupWindowOnClickListener.GetRegistrationShopOnClickListener listener) {
        this.registrationShopBeanArrayList = registrationShopBeanArrayList;
        this.context = context;
        this.listener = listener;
        initPopup();
    }
    public void setDataDelect(){
        guaDanAdapter.setNewData(registrationShopBeanArrayList);
        guaDanAdapter.setIndex(0);
        guaDanShopAdapter.setNewData(registrationShopBeanArrayList.get(0).getRegistrationShopList());
    }
    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_get_registration_shop, null);
        popupWindow = new PopupWindow(
                popupView,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                true
        );
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupView.setBackgroundColor(context.getColor(R.color.black60));
        popupWindow.setOutsideTouchable(true);
        // 计算居中位置
        popupView.post(() -> {
            DisplayMetrics metrics = new DisplayMetrics();
            ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int x = (metrics.widthPixels - popupView.getWidth()) / 2;
            int y = (metrics.heightPixels - popupView.getHeight()) / 2;
            popupWindow.update(x, y, -1, -1); // 更新位置
        });

        // 绑定子 View 事件
        popupView.findViewById(R.id.qudan_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
            listener.onClick(index,1);
        });
        popupView.findViewById(R.id.delete_shop).setOnClickListener(v -> {
            if (registrationShopBeanArrayList.size()==1){
                popupWindow.dismiss();
            }
            listener.onClick(index,2);

        });
        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        guadan_rv=popupView.findViewById(R.id.guadan_rv);
        guadan_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
        guaDanAdapter=new GuaDanAdapter();
        guadan_rv.setAdapter(guaDanAdapter);
        guaDanAdapter.setIndex(0);
        guaDanAdapter.setNewData(registrationShopBeanArrayList);
        guaDanAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                index=position;
                guaDanAdapter.setIndex(position);
                guaDanShopAdapter.setNewData(registrationShopBeanArrayList.get(position).getRegistrationShopList());
            }
        });


        guadan_shop_rv=popupView.findViewById(R.id.guadan_shop_rv);
        guadan_shop_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
        guaDanShopAdapter=new GuaDanShopAdapter();
        guadan_shop_rv.setAdapter(guaDanShopAdapter);
        guaDanShopAdapter.setNewData(registrationShopBeanArrayList.get(0).getRegistrationShopList());



    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
