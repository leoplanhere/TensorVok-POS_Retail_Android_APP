package com.uhm.uhmcs.popupwindow;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
import com.uhm.uhmcs.activity.MainActivity;
import com.uhm.uhmcs.adapter.OrderShopAdapter;
import com.uhm.uhmcs.adapter.RelieveShiftAdapter;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.RelieveShiftBean;
import com.uhm.uhmcs.bean.RelieveShiftPrintBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.GsonSandL;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.UserUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RelieveShiftPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private RecyclerView recyclerview;
    private RelieveShiftAdapter relieveShiftAdapter;
    private TextView jiaojiedan_btn1_c,jiaojiedan_btn1_tv,jiaojiedan_btn2_c,jiaojiedan_btn2_tv;
    private TextView leibie_btn1_c;
    private TextView leibie_btn2_tv;
    private TextView leibie_btn1_tv;
    private TextView leibie_btn2_c;
    private TextView shangpin_btn1_c;
    private TextView shangpin_btn1_tv;
    private TextView shangpin_btn2_c;
    private TextView shangpin_btn2_tv;
    private TextView tuikuan_btn2_tv;
    private TextView tuikuan_btn1_c;
    private TextView tuikuan_btn1_tv;
    private TextView tuikuan_btn2_c;


    public RelieveShiftPopupWindow(Activity context){

        this.context = context;

        initPopup();
    }
    private int handover_sheet=1;
    private int category_summary=1;
    private int product_summary=1;
    private int return_summary=1;
    private int number=1;
    private void initPopup() {
        View popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_relieve_shift, null);
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

        popupView.findViewById(R.id.jiaojiedan_btn1).setOnClickListener(v -> {
            jiaojiedan_btn1_c.setBackgroundResource(R.mipmap.checkbox_2);
            jiaojiedan_btn1_tv.setTextColor(context.getColor(R.color.buleshape));
            jiaojiedan_btn2_c.setBackgroundResource(R.drawable.gray_line);
            jiaojiedan_btn2_tv.setTextColor(context.getColor(R.color.black));
            handover_sheet=1;

        });
        popupView.findViewById(R.id.jiaojiedan_btn2).setOnClickListener(v -> {
            jiaojiedan_btn2_c.setBackgroundResource(R.mipmap.checkbox_2);
            jiaojiedan_btn2_tv.setTextColor(context.getColor(R.color.buleshape));
            jiaojiedan_btn1_c.setBackgroundResource(R.drawable.gray_line);
            jiaojiedan_btn1_tv.setTextColor(context.getColor(R.color.black));
            handover_sheet=2;
        });
        popupView.findViewById(R.id.leibie_btn1).setOnClickListener(v -> {
            leibie_btn1_c.setBackgroundResource(R.mipmap.checkbox_2);
            leibie_btn1_tv.setTextColor(context.getColor(R.color.buleshape));
            leibie_btn2_c.setBackgroundResource(R.drawable.gray_line);
            leibie_btn2_tv.setTextColor(context.getColor(R.color.black));
            category_summary=1;

        });
        popupView.findViewById(R.id.leibie_btn2).setOnClickListener(v -> {
            leibie_btn2_c.setBackgroundResource(R.mipmap.checkbox_2);
            leibie_btn2_tv.setTextColor(context.getColor(R.color.buleshape));
            leibie_btn1_c.setBackgroundResource(R.drawable.gray_line);
            leibie_btn1_tv.setTextColor(context.getColor(R.color.black));
            category_summary=2;
        });
        popupView.findViewById(R.id.shangpin_btn1).setOnClickListener(v -> {
            shangpin_btn1_c.setBackgroundResource(R.mipmap.checkbox_2);
            shangpin_btn1_tv.setTextColor(context.getColor(R.color.buleshape));
            shangpin_btn2_c.setBackgroundResource(R.drawable.gray_line);
            shangpin_btn2_tv.setTextColor(context.getColor(R.color.black));
            product_summary=1;

        });
        popupView.findViewById(R.id.shangpin_btn2).setOnClickListener(v -> {
            shangpin_btn2_c.setBackgroundResource(R.mipmap.checkbox_2);
            shangpin_btn2_tv.setTextColor(context.getColor(R.color.buleshape));
            shangpin_btn1_c.setBackgroundResource(R.drawable.gray_line);
            shangpin_btn1_tv.setTextColor(context.getColor(R.color.black));
            product_summary=2;
        });

        popupView.findViewById(R.id.tuikuan_btn1).setOnClickListener(v -> {
            tuikuan_btn1_c.setBackgroundResource(R.mipmap.checkbox_2);
            tuikuan_btn1_tv.setTextColor(context.getColor(R.color.buleshape));
            tuikuan_btn2_c.setBackgroundResource(R.drawable.gray_line);
            tuikuan_btn2_tv.setTextColor(context.getColor(R.color.black));
            return_summary=1;

        });
        popupView.findViewById(R.id.tuikuan_btn2).setOnClickListener(v -> {
            tuikuan_btn2_c.setBackgroundResource(R.mipmap.checkbox_2);
            tuikuan_btn2_tv.setTextColor(context.getColor(R.color.buleshape));
            tuikuan_btn1_c.setBackgroundResource(R.drawable.gray_line);
            tuikuan_btn1_tv.setTextColor(context.getColor(R.color.black));
            return_summary=2;
        });

        TextView dayingshu_tv=popupView.findViewById(R.id.dayingshu_tv);
        popupView.findViewById(R.id.dayingshu_jian).setOnClickListener(v -> {
            if (number<=1){
                return;
            }
            number--;
            dayingshu_tv.setText(number+"");
        });
        popupView.findViewById(R.id.dayingshu_jia).setOnClickListener(v -> {
            number++;
            dayingshu_tv.setText(number+"");
        });
        popupView.findViewById(R.id.jiaoban_btn).setOnClickListener(v -> {
            shiftHandover();
        });

        jiaojiedan_btn1_c=popupView.findViewById(R.id.jiaojiedan_btn1_c);
        jiaojiedan_btn1_tv=popupView.findViewById(R.id.jiaojiedan_btn1_tv);
        jiaojiedan_btn2_c=popupView.findViewById(R.id.jiaojiedan_btn2_c);
        jiaojiedan_btn2_tv=popupView.findViewById(R.id.jiaojiedan_btn2_tv);

        leibie_btn1_c=popupView.findViewById(R.id.leibie_btn1_c);
        leibie_btn1_tv=popupView.findViewById(R.id.leibie_btn1_tv);
        leibie_btn2_c=popupView.findViewById(R.id.leibie_btn2_c);
        leibie_btn2_tv=popupView.findViewById(R.id.leibie_btn2_tv);

        shangpin_btn1_c=popupView.findViewById(R.id.shangpin_btn1_c);
        shangpin_btn1_tv=popupView.findViewById(R.id.shangpin_btn1_tv);
        shangpin_btn2_c=popupView.findViewById(R.id.shangpin_btn2_c);
        shangpin_btn2_tv=popupView.findViewById(R.id.shangpin_btn2_tv);

        tuikuan_btn1_c=popupView.findViewById(R.id.tuikuan_btn1_c);
        tuikuan_btn1_tv=popupView.findViewById(R.id.tuikuan_btn1_tv);
        tuikuan_btn2_c=popupView.findViewById(R.id.tuikuan_btn2_c);
        tuikuan_btn2_tv=popupView.findViewById(R.id.tuikuan_btn2_tv);


        popupView.findViewById(R.id.fanhui_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        recyclerview=popupView.findViewById(R.id.recyclerview);
        recyclerview.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL,false));
        relieveShiftAdapter=new RelieveShiftAdapter();
        recyclerview.setAdapter(relieveShiftAdapter);
        recyclerview.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();


                if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) { // 当滚动到列表底部时触发加载更多事件
                    page++;
                    if (page>totalpage){
                        return;
                    }
                    gethandoverList();

                }


            }
        });
        relieveShiftAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            RelieveShiftBean relieveShiftBean=relieveShiftAdapter.getItem(position);
            RelieveShiftPrintBean relieveShiftPrintBean=new RelieveShiftPrintBean();
            RelieveShiftPrintBean.DataBean dataBean=new RelieveShiftPrintBean.DataBean();
            dataBean.setLogintime(relieveShiftBean.getLogintime());
            dataBean.setEndtime(relieveShiftBean.getCreatetime());
            dataBean.setNickname(relieveShiftBean.getNickname());

            if (!TextUtils.isEmpty(relieveShiftBean.getPaymentjson())){
                List<RelieveShiftPrintBean.DataBean.TotalBean> totalBeans= GsonSandL.getInstance().GsonStoL(relieveShiftBean.getPaymentjson(),RelieveShiftPrintBean.DataBean.TotalBean.class);
                dataBean.setTotal(totalBeans);
            }else {
                dataBean.setTotal(new ArrayList<>());
            }
            if (!TextUtils.isEmpty(relieveShiftBean.getRefundjson())){
                List<RelieveShiftPrintBean.DataBean.RefundBean> refundBeans= GsonSandL.getInstance().GsonStoL(relieveShiftBean.getRefundjson(),RelieveShiftPrintBean.DataBean.RefundBean.class);
                dataBean.setRefund(refundBeans);
            }else {
                dataBean.setRefund(new ArrayList<>());
            }




            relieveShiftPrintBean.setData(dataBean);
            MyPrinterHelper.getInstance().asyncPrintRelieveShift(context,relieveShiftPrintBean);
        });

        gethandoverList();

    }

    private void shiftHandover() {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getUserId()+"");
        params.put("machine_number", "001");
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid()+"");
        String url = POSApiSerview.POS_URL + POSApiSerview.shiftHandover;
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
                            if (code!=1){
                                return;
                            }
                            RelieveShiftPrintBean relieveShiftPrintBean=new Gson().fromJson(response, RelieveShiftPrintBean.class);
                            new DeleteShopPopupWindow(context, true, "交班成功", new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                                @Override
                                public void onClick(String text) {
                                    UserUtils.getInstance().setLoginBase(context, null);
                                    Intent intent=new Intent(context, LoginActivity.class);
                                    context.startActivity(intent);
                                }
                            }).show();

                            MyPrinterHelper.getInstance().asyncPrintRelieveShift(context,relieveShiftPrintBean);

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
    private int page=1;
    private int totalpage=1;
    private void gethandoverList() {
        Map<String, String> params = new HashMap<>();
        params.put("page", page+"");
        params.put("strip", "10");
//        params.put("page", grouponGoods_page + "");
//        params.put("strip", "20");
        String url = POSApiSerview.POS_URL + POSApiSerview.handoverList;
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
                                JSONObject jsonObject1=new JSONObject(jsonObject.getString("data"));
                                ArrayList<RelieveShiftBean> relieveShiftBeans = new Gson().fromJson(jsonObject1.getString("data"), new TypeToken<ArrayList<RelieveShiftBean>>() {
                                }.getType());
                                totalpage=new JSONObject(jsonObject1.getString("pagination")).getInt("totalpage");
//                                operateDetails(lastOrderBeanArrayList.get(0));
                                if (page>1){
                                    relieveShiftAdapter.addData(relieveShiftBeans);
                                }else {
                                    relieveShiftAdapter.setNewData(relieveShiftBeans);
                                }

                            }else {
                                relieveShiftAdapter.setNewData(new ArrayList<>());
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
//    private void shiftHandover() {
//        Map<String, String> params = new HashMap<>();
//        params.put("user_id", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getUserId()+"");
//        params.put("machine_number", "001");
//        params.put("handover_sheet", handover_sheet+"");
//        params.put("number", number+"");
//        params.put("category_summary", category_summary+"");
//        params.put("product_summary",product_summary +"");
//        params.put("return_summary", return_summary+"");
//        String url = POSApiSerview.POS_URL + POSApiSerview.shiftHandover;
//        OkHttpUtil.postFormAsync(url, params,context, new OkHttpUtil.OkHttpCallback() {
//            @Override
//            public void onSuccess(String response) {
//                Log.i("ttt", response);
//                context.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            JSONObject jsonObject = new JSONObject(response);
//                            int code = jsonObject.getInt("code");
//                            if (code!=1){
//                                return;
//                            }
//                            new DeleteShopPopupWindow(context, true, "交班成功", new PopupWindowOnClickListener.DeleteShopOnClickListener() {
//                                @Override
//                                public void onClick(String text) {
//                                    Intent intent=new Intent(context, LoginActivity.class);
//                                    context.startActivity(intent);
//                                }
//                            }).show();
//                            ArrayList<RelieveShiftPrintBean> relieveShiftPrintBeanArrayList = new Gson().fromJson(jsonObject.getString("data"), new TypeToken<ArrayList<RelieveShiftPrintBean>>() {
//                            }.getType());
//                            MyPrinterHelper.getInstance().asyncPrintRelieveShift(context,relieveShiftPrintBeanArrayList.get(0));
//
//                        } catch (JSONException e) {
//                            throw new RuntimeException(e);
//                        }
//
//                    }
//                });
//
//            }
//
//            @Override
//            public void onFailure(IOException e) {
//                System.err.println("请求失败: " + e.getMessage());
//            }
//        });
//    }
}
