package com.uhm.uhmcs.popupwindow;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.MemberBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class GoodsWarehousingPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private CustomInputTextView shangpintiaoma_tv,kuchunshuliang_tv;
    private TextView shangpin_tv,warehousing_btn;
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> allGrouponGoodsModelList ;
    ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList;
    private PopupWindowOnClickListener.GoodsWarehousingOnClickListener goodsWarehousingOnClickListener;



    public GoodsWarehousingPopupWindow(Activity context,ArrayList<GrouponGoodsBean.GrouponGoodsModel> allGrouponGoodsModelList,PopupWindowOnClickListener.GoodsWarehousingOnClickListener goodsWarehousingOnClickListener ) {
        this.goodsWarehousingOnClickListener = goodsWarehousingOnClickListener;
        this.allGrouponGoodsModelList = allGrouponGoodsModelList;
        this.context = context;

        initPopup();
    }



    private Runnable shangpintiaomaRunnable = new Runnable() {
        @Override
        public void run() {
            shangpintiaoma_tv.requestFocus();
        }
    };
    private Runnable kuchunshuliangRunnable = new Runnable() {
        @Override
        public void run() {
            kuchunshuliang_tv.requestFocus();
        }
    };
    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_goods_warehousing, null);
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

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        shangpintiaoma_tv=popupView.findViewById(R.id.shangpintiaoma_tv);
        kuchunshuliang_tv=popupView.findViewById(R.id.kuchunshuliang_tv);
        shangpin_tv=popupView.findViewById(R.id.shangpin_tv);
        warehousing_btn=popupView.findViewById(R.id.warehousing_btn);
        // 自动获取焦点
        shangpintiaoma_tv.postDelayed(shangpintiaomaRunnable,100);
        shangpintiaoma_tv.setOnInputCompleteListener(text -> {
            grouponGoodsModelArrayList= allGrouponGoodsModelList.stream()
                    .filter(grouponGoodsModel -> grouponGoodsModel.getSn().equals(text))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (grouponGoodsModelArrayList.isEmpty()){
                new DeleteShopPopupWindow(context,"商品库中无该商品",true).show();
            }else {
                shangpin_tv.setText(grouponGoodsModelArrayList.get(0).getTitle());
                shangpintiaoma_tv.removeCallbacks(shangpintiaomaRunnable);
                kuchunshuliang_tv.postDelayed(kuchunshuliangRunnable,100);
            }

        });
        // 自动获取焦点
//        kuchunshuliang_tv.postDelayed(() -> kuchunshuliang_tv.requestFocus(), 100);
        kuchunshuliang_tv.setOnInputCompleteListener(text -> {

        });
        shangpintiaoma_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shangpintiaoma_tv.postDelayed(shangpintiaomaRunnable,100);
                kuchunshuliang_tv.removeCallbacks(kuchunshuliangRunnable);
            }
        });
        kuchunshuliang_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (grouponGoodsModelArrayList!=null&&!grouponGoodsModelArrayList.isEmpty()){
                    shangpintiaoma_tv.removeCallbacks(shangpintiaomaRunnable);
                    kuchunshuliang_tv.postDelayed(kuchunshuliangRunnable,100);
                }
            }
        });

        warehousing_btn.setOnClickListener(v -> {
            if (grouponGoodsModelArrayList==null||grouponGoodsModelArrayList.isEmpty()){
                grouponGoodsModelArrayList= allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> grouponGoodsModel.getSn().equals(shangpintiaoma_tv.getText().toString()))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (grouponGoodsModelArrayList.isEmpty()){
                    new DeleteShopPopupWindow(context,"商品库中无该商品",true).show();
                }else {
                    shangpin_tv.setText(grouponGoodsModelArrayList.get(0).getTitle());
                    shangpintiaoma_tv.removeCallbacks(shangpintiaomaRunnable);
                    kuchunshuliang_tv.postDelayed(kuchunshuliangRunnable,100);
                }
                return;
            }
            if (TextUtils.isEmpty(kuchunshuliang_tv.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入库存数量",true).show();
                return;
            }
            addStore();


        });


    }

    public void addStore(){
        Map<String, String> params = new HashMap<>();
        params.put("goods_id", grouponGoodsModelArrayList.get(0).getId()+"");
        params.put("offline_stock",kuchunshuliang_tv.getText().toString());
        params.put("sn",shangpintiaoma_tv.getText().toString());
        String url = POSApiSerview.POS_URL + POSApiSerview.addStore;
        OkHttpUtil.postFormAsync(url, params, context,new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt",">>>>>>>>>>>>>");
                context.runOnUiThread(new Runnable() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject=new JSONObject(response);
                            int code=jsonObject.getInt("code");
                            goodsWarehousingOnClickListener.onClick(code,jsonObject.getString("msg"));
                            popupWindow.dismiss();
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {

            }
        });
    }

    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);

    }
}
