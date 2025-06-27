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
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AddPaymentPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;



    private CustomInputTextView zfb_gongyao,zfb_token,wx_shanghuhao,wx_appid,wx_miyao,wx_zhishanghuid,wx_app_id;

    private TextView pay_shi,pay_fou,pay_submit;






    public AddPaymentPopupWindow(Activity context){

        this.context = context;

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
        pay_submit.setOnClickListener(v -> {

        });

    }
    private int zh_default=1;
    private void getPaymentList() {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
//        params.put("page", grouponGoods_page + "");
//        params.put("strip", "20");
        String url = POSApiSerview.POS_URL + POSApiSerview.listPaymentMethod;
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
                            if (code == 1 && !TextUtils.isEmpty(jsonObject.getString("data"))) {


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
    }
}
