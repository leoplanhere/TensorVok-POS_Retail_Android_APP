package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.PaymentBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddPaymentPopupWindow implements View.OnClickListener {
    private PopupWindow popupWindow;
    private Activity context;



    private CustomInputTextView zfb_gongyao,zfb_token,wx_shanghuhao,wx_appid,wx_miyao,wx_zhishanghuid,wx_app_id;

    private TextView pay_shi,pay_fou,pay_submit;

    private PopupWindowOnClickListener.DeleteShopOnClickListener listener;



    PaymentBean.DataBean dataBean;

    public AddPaymentPopupWindow(Activity context, boolean isAdd, PaymentBean.DataBean dataBean, PopupWindowOnClickListener.DeleteShopOnClickListener listener){
        this.listener = listener;
        this.context = context;
        this.isAdd = isAdd;
        this.dataBean = dataBean;
        initPopup();
    }

    private void initPopup() {

        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_add_payment, null);
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

        zfb_gongyao=popupView.findViewById(R.id.zfb_gongyao);
        zfb_token=popupView.findViewById(R.id.zfb_token);
        wx_shanghuhao=popupView.findViewById(R.id.wx_shanghuhao);
        wx_appid=popupView.findViewById(R.id.wx_appid);
        wx_miyao=popupView.findViewById(R.id.wx_miyao);
        wx_zhishanghuid=popupView.findViewById(R.id.wx_zhishanghuid);
        wx_app_id=popupView.findViewById(R.id.wx_app_id);
        pay_shi=popupView.findViewById(R.id.pay_shi);
        pay_fou=popupView.findViewById(R.id.pay_fou);
        pay_submit=popupView.findViewById(R.id.pay_submit);
        pay_shi.setOnClickListener(v -> {
            pay_shi.setBackgroundResource(R.drawable.blue_bg3);
            pay_fou.setBackgroundResource(R.drawable.blue_bg2);
            zh_default=1;
        });
        pay_fou.setOnClickListener(v -> {
            pay_fou.setBackgroundResource(R.drawable.blue_bg3);
            pay_shi.setBackgroundResource(R.drawable.blue_bg2);
            zh_default=2;
        });

        zfb_gongyao.setOnClickListener(this);
        zfb_token.setOnClickListener(this);
        wx_shanghuhao.setOnClickListener(this);
        wx_appid.setOnClickListener(this);
        wx_zhishanghuid.setOnClickListener(this);
        wx_app_id.setOnClickListener(this);
        wx_miyao.setOnClickListener(this);


        zfb_gongyao.setOnInputCompleteListener(text -> {

        });
        zfb_token.setOnInputCompleteListener(text -> {

        });
        wx_shanghuhao.setOnInputCompleteListener(text -> {

        });
        wx_appid.setOnInputCompleteListener(text -> {

        });
        wx_zhishanghuid.setOnInputCompleteListener(text -> {

        });
        wx_app_id.setOnInputCompleteListener(text -> {

        });
        wx_miyao.setOnInputCompleteListener(text -> {

        });
        if (!isAdd){
            zfb_gongyao.setText(dataBean.getZfb_public_key());
            zfb_token.setText(dataBean.getApp_auth_token());
            wx_shanghuhao.setText(dataBean.getMerchant());
            wx_appid.setText(dataBean.getWxappid());
            wx_miyao.setText(dataBean.getWxsecret());
            wx_zhishanghuid.setText(dataBean.getSub_mch_id());
            wx_app_id.setText(dataBean.getSub_app_id());
        }

        pay_submit.setOnClickListener(v -> {
            if (TextUtils.isEmpty(zfb_gongyao.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入支付宝服务商公钥",true).show();
                return;
            }
            if (TextUtils.isEmpty(zfb_token.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入支付宝授权token",true).show();
                return;
            }
            if (TextUtils.isEmpty(wx_shanghuhao.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入微信服务商商户号",true).show();
                return;
            }
            if (TextUtils.isEmpty(wx_appid.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入服务商微信支付appid",true).show();
                return;
            }
            if (TextUtils.isEmpty(wx_miyao.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入服务商微信支付密钥",true).show();
                return;
            }
            if (TextUtils.isEmpty(wx_zhishanghuid.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入微信子商户商户号ID",true).show();
                return;
            }
            if (TextUtils.isEmpty(wx_app_id.getText().toString())){
                new DeleteShopPopupWindow(context,"请输入微信子商户商户号ID",true).show();
                return;
            }
            PaymentMethod();

        });

    }
    private int zh_default=1;

    private boolean isAdd=true;
    private void PaymentMethod() {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        params.put("merchant", wx_shanghuhao.getText().toString());
        params.put("wxappid",wx_appid.getText().toString());
        params.put("wxsecret", wx_miyao.getText().toString());
        params.put("sub_mch_id", wx_zhishanghuid.getText().toString());
        params.put("sub_app_id", wx_app_id.getText().toString());
        params.put("zh_default", zh_default+"");
        params.put("zfb_public_key", zfb_gongyao.getText().toString());
        params.put("app_auth_token", zfb_token.getText().toString());
        String url;
        if(isAdd){
             url = POSApiSerview.POS_URL + POSApiSerview.addPaymentMethod;
        }else {
            params.put("id", dataBean.getId()+"");
             url = POSApiSerview.POS_URL + POSApiSerview.updatePaymentMethod;
        }

        OkHttpUtil.postFormAsync(url, params,context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", response);
                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code == 1 ) {

                                new DeleteShopPopupWindow(context,"操作成功",true).show();
                                listener.onClick("");
                                popupWindow.dismiss();
                            }else {


                            }

                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }

                    }
                });

            }

            @Override
            public void onFailure(IOException e) {
                System.err.println("请求失败: " + e.getMessage());
            }
        });
    }


    public void show() {
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);
        // 自动获取焦点
        zfb_gongyao.postDelayed(() -> zfb_gongyao.requestFocus(), 100);
    }

    @Override
    public void onClick(View v) {
        Log.i("ttt",">>zfb_gongyao>>>>>");
        int id=v.getId();
        if (id==R.id.zfb_gongyao){
            // 自动获取焦点
            zfb_gongyao.postDelayed(() -> zfb_gongyao.requestFocus(), 100);
        }
        if (id==R.id.zfb_token){
// 自动获取焦点
            zfb_token.postDelayed(() -> zfb_token.requestFocus(), 100);
        }
        if (id==R.id.wx_shanghuhao){
// 自动获取焦点
            wx_shanghuhao.postDelayed(() -> wx_shanghuhao.requestFocus(), 100);
        }
        if (id==R.id.wx_appid){
// 自动获取焦点
            wx_appid.postDelayed(() -> wx_appid.requestFocus(), 100);
        }
        if (id==R.id.wx_zhishanghuid){
// 自动获取焦点
            wx_zhishanghuid.postDelayed(() -> wx_zhishanghuid.requestFocus(), 100);
        }
        if (id==R.id.wx_app_id){
// 自动获取焦点
            wx_app_id.postDelayed(() -> wx_app_id.requestFocus(), 100);
        }
        if (id==R.id.wx_miyao){
// 自动获取焦点
            wx_miyao.postDelayed(() -> wx_miyao.requestFocus(), 100);
        }
    }
}
