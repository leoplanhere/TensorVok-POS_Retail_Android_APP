package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.GuaDanAdapter;
import com.uhm.uhmcs.adapter.GuaDanShopAdapter;
import com.uhm.uhmcs.adapter.ShopAdapter;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.RegistrationShopBean;
import com.uhm.uhmcs.utils.MyLabeksPrinterHelper;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class PrintLabelsPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private ShopAdapter shopAdapter;
    private RecyclerView shop_rv;

    private TextView all_select;
    private boolean is_all_select = false;
    private LinearLayout all_select_btn;


    public PrintLabelsPopupWindow(Activity context) {

        this.context = context;

        initPopup();
    }

    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_print_labels, null);
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
        popupView.findViewById(R.id.shop_btn).setOnClickListener(v -> {
            new ShopPopupWindow(context, new PopupWindowOnClickListener.ShopOnClickListener() {
                @Override
                public void onClick(ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList) {
                    is_all_select = true;
                    all_select.setBackgroundResource(R.mipmap.checkbox_2);
                    shopAdapter.setNewData(grouponGoodsModelArrayList);
                }
            }).show();
        });
        shop_rv = popupView.findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        shopAdapter = new ShopAdapter();
        shop_rv.setAdapter(shopAdapter);
        shopAdapter.setOnItemClickListener((adapter, view, position) -> {
            GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = shopAdapter.getData().get(position);
            if (grouponGoodsModel.isSelected()) {
                grouponGoodsModel.setSelected(false);
                is_all_select = false;
                all_select.setBackgroundResource(R.mipmap.checkbox_1);
            } else {
                grouponGoodsModel.setSelected(true);
                ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList = new ArrayList<>();
                grouponGoodsModelArrayList = shopAdapter.getData().stream()
                        .filter(grouponGoodsModel1 -> !grouponGoodsModel1.isSelected())
                        .collect(Collectors.toCollection(ArrayList::new));
                if (grouponGoodsModelArrayList.isEmpty()) {
                    is_all_select = true;
                    all_select.setBackgroundResource(R.mipmap.checkbox_2);
                }
            }
            shopAdapter.notifyItemChanged(position);
        });

        all_select = popupView.findViewById(R.id.all_select);
        all_select_btn = popupView.findViewById(R.id.all_select_btn);

        all_select_btn.setOnClickListener(v -> {
            if (is_all_select) {
                is_all_select = false;
                all_select.setBackgroundResource(R.mipmap.checkbox_1);
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : shopAdapter.getData()) {
                    grouponGoodsModel.setSelected(false);
                }
            } else {
                is_all_select = true;
                all_select.setBackgroundResource(R.mipmap.checkbox_2);
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : shopAdapter.getData()) {
                    grouponGoodsModel.setSelected(true);
                }
            }
            shopAdapter.notifyDataSetChanged();
        });
        popupView.findViewById(R.id.fanhui_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        popupView.findViewById(R.id.all_delete_btn).setOnClickListener(v -> {
            shopAdapter.setNewData(new ArrayList<>());
            is_all_select = false;
            all_select.setBackgroundResource(R.mipmap.checkbox_1);
        });
        popupView.findViewById(R.id.delete_btn).setOnClickListener(v -> {
            shopAdapter.setNewData(shopAdapter.getData().stream()
                    .filter(grouponGoodsModel -> !grouponGoodsModel.isSelected())
                    .collect(Collectors.toCollection(ArrayList::new)));
            if (shopAdapter.getItemCount()==0){
                is_all_select = false;
                all_select.setBackgroundResource(R.mipmap.checkbox_1);
            }
        });
        popupView.findViewById(R.id.print_btn).setOnClickListener(v -> {
            ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList = shopAdapter.getData().stream()
                    .filter(GrouponGoodsBean.GrouponGoodsModel::isSelected)
                    .collect(Collectors.toCollection(ArrayList::new));
            if (grouponGoodsModelArrayList.isEmpty()){
                return;
            }
            new PrintNumPopupWindow(context, new PopupWindowOnClickListener.DiscountOnClickListener() {
                @Override
                public void onClick(String discount) {
                    MyLabeksPrinterHelper.getInstance().asyncPrintCheckout(context,grouponGoodsModelArrayList,Integer.parseInt(discount));
                    popupWindow.dismiss();
                }
            }).show();


        });


    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
    }
}
