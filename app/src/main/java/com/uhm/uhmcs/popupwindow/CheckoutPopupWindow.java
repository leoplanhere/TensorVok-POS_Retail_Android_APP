package com.uhm.uhmcs.popupwindow;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.activity.LoginActivity;
import com.uhm.uhmcs.activity.MainActivity;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.uhm.uhmcs.bean.ClubCardBean;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.http.NetworkErrorInterceptor;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.EcrSocketManager;
import com.uhm.uhmcs.utils.GsonSandL;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.NetworkUtils;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.utils.Utilis;
import com.uhm.uhmcs.view.CustomInputTextView;
import com.uhm.uhmcs.view.MyPresentation;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class CheckoutPopupWindow {
    private PopupWindow popupWindow;
    private Activity context;
    private CheckoutBean checkoutBean;
    private TextView yingshou_tv, youhui_tv, shijishou_tv, weixin_btn, xianjin_btn, zhaolin_tv, zhifubao_btn, yishou_tv, huiyuanka_btn, huiyuanchaxun_btn;

    // --- NETS 新增组件 ---
    private TextView nets_purchase_btn, nets_qr_btn, nets_cc_btn;
    private EcrSocketManager netsManager = new EcrSocketManager();

    private CustomInputTextView shoukuan_tv;
    ClubCardBean.DataBean clubCardData;
    TextView huiyuankahao_tv;
    TextView huiyuanyue_tv;
    private LinearLayout huiyuan_view;
    private DeleteShopPopupWindow deleteShopPopupWindow;
    private String yinshou = "0.00";

    private String pay_type = "";
    private int order_status = 0;

//    private boolean weixin_type=false;
//    private boolean zhifubao_type=false;

    private boolean isyouhuijuan = false;

    private Runnable shoukuan_tvRunnable = new Runnable() {
        @Override
        public void run() {
            shoukuan_tv.requestFocus();
        }
    };

    private PopupWindowOnClickListener.CheckoutOnClickListener checkoutOnClickListener;

    public boolean isShow() {
        return popupWindow.isShowing();
    }

    public CheckoutPopupWindow(Activity context, CheckoutBean checkoutBean, PopupWindowOnClickListener.CheckoutOnClickListener checkoutOnClickListener) {
        this.context = context;
        this.checkoutBean = checkoutBean;
        this.checkoutOnClickListener = checkoutOnClickListener;
        initPopup();
    }

    View popupView;
    BuildBean buildBean;
    private LinearLayout youhuijuan_view;
    private TextView youhuijuan_jine, shiyong_btn, bushiyong_btn, qufen_btn;

    @SuppressLint("SetTextI18n")
    private void initPopup() {
        popupView = LayoutInflater.from(context).inflate(R.layout.popupwindow_checkout, null);
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
        yingshou_tv = popupView.findViewById(R.id.yingshou_tv);
        youhui_tv = popupView.findViewById(R.id.youhui_tv);
        shijishou_tv = popupView.findViewById(R.id.shijishou_tv);
        zhaolin_tv = popupView.findViewById(R.id.zhaolin_tv);
        yishou_tv = popupView.findViewById(R.id.yishou_tv);

        youhuijuan_view = popupView.findViewById(R.id.youhuijuan_view);
        youhuijuan_jine = popupView.findViewById(R.id.youhuijuan_jine);
        shiyong_btn = popupView.findViewById(R.id.shiyong_btn);
        bushiyong_btn = popupView.findViewById(R.id.bushiyong_btn);

        qufen_btn = popupView.findViewById(R.id.qufen_btn);

        shoukuan_tv = popupView.findViewById(R.id.shoukuan_tv);

        // --- NETS 按钮绑定 ---
        nets_purchase_btn = popupView.findViewById(R.id.nets_purchase_btn);
        nets_qr_btn = popupView.findViewById(R.id.nets_qr_btn);
        nets_cc_btn = popupView.findViewById(R.id.nets_cc_btn);

        // --- 找到这段代码进行替换 ---
        if (!TextUtils.isEmpty(checkoutBean.getCoupon_fee())) {
            youhuijuan_view.setVisibility(VISIBLE);
            youhuijuan_jine.setText(checkoutBean.getCoupon_fee());
            // 默认不使用，所以这里不需要 subtract 扣除金额
            youhui_tv.setText("￥" + checkoutBean.getDiscount_fee());
        } else {
            youhui_tv.setText("￥" + checkoutBean.getDiscount_fee());
        }

// 设置“不使用”按钮为选中样式，“使用”按钮为普通样式
        shiyong_btn.setBackgroundResource(R.drawable.blue_line);
        shiyong_btn.setTextColor(Color.parseColor("#FF3B82F6"));
        bushiyong_btn.setBackgroundResource(R.drawable.blue_bg5);
        bushiyong_btn.setTextColor(Color.parseColor("#FFFFFFFF"));

        yishou_tv.setText("￥0.00");
        yingshou_tv.setText("￥" + checkoutBean.getTotal_amount());
        shijishou_tv.setText("￥" + checkoutBean.getTotal_fee());
// -----------------------------

        shiyong_btn.setOnClickListener(v -> {
            if (isyouhuijuan) {
                return;
            }
            isyouhuijuan = true;
            shiyong_btn.setBackgroundResource(R.drawable.blue_bg5);
            shiyong_btn.setTextColor(Color.parseColor("#FFFFFFFF"));
            bushiyong_btn.setBackgroundResource(R.drawable.blue_line);
            bushiyong_btn.setTextColor(Color.parseColor("#FF3B82F6"));
            youhui_tv.setText("￥" + new BigDecimal(checkoutBean.getDiscount_fee()).add(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
            checkoutBean.setTotal_fee(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
            shijishou_tv.setText("￥" + checkoutBean.getTotal_fee());
            shoukuan_tv.setText(checkoutBean.getTotal_fee() + "");
        });
        qufen_btn.setOnClickListener(v -> {
            // 原始金额
            BigDecimal original = new BigDecimal(checkoutBean.getTotal_fee());
            // 抹去分位（向下取整）
            BigDecimal truncated = original.setScale(1, RoundingMode.DOWN);
            // 计算差额
            BigDecimal diff = original.subtract(truncated);
            checkoutBean.setDiscount_fee(new BigDecimal(checkoutBean.getDiscount_fee()).add(diff).toString());
            youhui_tv.setText("￥" + checkoutBean.getDiscount_fee());
            checkoutBean.setTotal_fee(new BigDecimal(checkoutBean.getTotal_fee()).subtract(diff).toString());
            shijishou_tv.setText("￥" + checkoutBean.getTotal_fee());
            shoukuan_tv.setText(checkoutBean.getTotal_fee() + "");
        });

        bushiyong_btn.setOnClickListener(v -> {
            if (!isyouhuijuan) {
                return;
            }
            bushiyong_btn.setBackgroundResource(R.drawable.blue_bg5);
            bushiyong_btn.setTextColor(Color.parseColor("#FFFFFFFF"));
            shiyong_btn.setBackgroundResource(R.drawable.blue_line);
            shiyong_btn.setTextColor(Color.parseColor("#FF3B82F6"));
            isyouhuijuan = false;
            youhui_tv.setText("￥" + checkoutBean.getDiscount_fee());
            checkoutBean.setTotal_fee(new BigDecimal(checkoutBean.getTotal_fee()).add(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
            shijishou_tv.setText("￥" + checkoutBean.getTotal_fee());
            shoukuan_tv.setText(checkoutBean.getTotal_fee() + "");
        });

        shoukuan_tv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s.toString())) {
                    zhaolin_tv.setText("0.00");
                    return;
                }
                if (count > before) {
                    CharSequence newChar = s.subSequence(start, start + count);
                    if (newChar.equals(".") || newChar.equals("-")) {
                        return;
                    }
                }
                if (!isValidNumber(s.toString())) {
                    return;
                }

                BigDecimal zhaolin;
                zhaolin = new BigDecimal(TextUtils.isEmpty(s.toString()) ? "0.00" : s.toString()).subtract(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)));
                if (zhaolin.compareTo(BigDecimal.ZERO) > 0) {
                    zhaolin_tv.setText(zhaolin.toString());
                } else {
                    zhaolin_tv.setText("0.00");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        // 自动获取焦点
        shoukuan_tv.postDelayed(shoukuan_tvRunnable, 100);
        shoukuan_tv.setText(checkoutBean.getTotal_fee() + "");


        shoukuan_tv.setOnInputCompleteListener(text -> {
            // --- 1. 通用金额校验 (无论什么支付方式，点击确定前都要先检查金额) ---
            String inputAmount = shoukuan_tv.getText().toString();

            if (TextUtils.isEmpty(inputAmount)) {
                new DeleteShopPopupWindow(context, context.getString(R.string.enter_payment_amount), true).show();
                return;
            }
            if (!isValidNumber(inputAmount)) {
                new DeleteShopPopupWindow(context, context.getString(R.string.Please_input_in_correct_price_format), true).show();
                return;
            }
            if (new BigDecimal(inputAmount).compareTo(BigDecimal.ZERO) <= 0) {
                new DeleteShopPopupWindow(context, context.getString(R.string.Amount_must_0), true).show();
                return;
            }

            // --- 2. 根据当前选中的 pay_type 分流处理逻辑 ---

            // 情况 A: 选中了 NETS 系列支付 (netsp, netsqr, netscc)
            if (pay_type.equals("netsp") || pay_type.equals("netsqr") || pay_type.equals("netscc")) {
                // ✅ 这里是你的核心需求：点击确定/回车后，才真正调起 NETS 终端
                startNetsTransaction(pay_type);

            }
            // 情况 B: 现金支付
            else if (pay_type.equals("cash")) {
                checkoutBean.setPay_type(pay_type);
                checkoutBean.setPay_fee(inputAmount);
                checkoutBean.setCash_price(inputAmount);
                checkoutBean.setCash_change(zhaolin_tv.getText().toString());

                if (!NetworkUtils.getInstance().isNetworkConnected(context)) {
                    if (new BigDecimal(inputAmount).subtract(new BigDecimal(checkoutBean.getTotal_fee())).compareTo(BigDecimal.ZERO) < 0) {
                        new DeleteShopPopupWindow(context, context.getString(R.string.Cannot_underpay), true).show();
                        return;
                    }
                }
                SubmitCheckout();
            }
            // 情况 C: 会员卡余额支付
            else if (pay_type.equals("wallet")) {
                if (clubCardData == null) {
                    new DeleteShopPopupWindow(context, "请先查询会员信息", true).show();
                    return;
                }
                if (new BigDecimal(inputAmount).subtract(new BigDecimal(checkoutBean.getTotal_fee())).add(new BigDecimal(yinshou)).compareTo(BigDecimal.ZERO) > 0) {
                    new DeleteShopPopupWindow(context, context.getString(R.string.Cannot_overpay), true).show();
                    return;
                }
                if (new BigDecimal(clubCardData.getAmount()).subtract(new BigDecimal(inputAmount)).compareTo(BigDecimal.ZERO) < 0) {
                    new DeleteShopPopupWindow(context, "会员卡余额不足", true).show();
                    return;
                }
                checkoutBean.setPay_type(pay_type);
                checkoutBean.setPay_fee(inputAmount);
                checkoutBean.setCash_price(inputAmount);
                checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                checkoutBean.setCardnumber(clubCardData.getNumber());
                SubmitCheckout();
            }
            // 情况 D: 其他支付 (通常是扫码支付：微信/支付宝)
            else {
                if (new BigDecimal(inputAmount).subtract(new BigDecimal(checkoutBean.getTotal_fee())).add(new BigDecimal(yinshou)).compareTo(BigDecimal.ZERO) > 0) {
                    new DeleteShopPopupWindow(context, context.getString(R.string.Cannot_overpay), true).show();
                    return;
                }
                // 扫码前清理焦点，弹出扫码框
                shoukuan_tv.removeCallbacks(shoukuan_tvRunnable);
                shoukuan_tv.clearFocus();
                deleteShopPopupWindow.show();
            }
        });


        shoukuan_tv.setOnFnListener(() -> popupWindow.dismiss());
        buildBean = DialogUIUtils.showLoading(context, context.getString(R.string.paying), true, true, false, false);
        deleteShopPopupWindow = new DeleteShopPopupWindow(context, context.getString(R.string.Scan_to_pay), false, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
            @Override
            public void onClick(String text) {
                shoukuan_tv.postDelayed(shoukuan_tvRunnable, 100);
                Log.i("ttt", ">>>1112>>>>支付码>" + text);
                if (TextUtils.isEmpty(text)) {
                    return;
                }
                String type = detectPaymentType(text);
                if (type.equals("unknown")) {
                    new DeleteShopPopupWindow(context, context.getString(R.string.Scan_payment_QR_code), true).show();
                    return;
                }

                pay_type = type;
                checkoutBean.setPay_type(pay_type);
                checkoutBean.setShop_id(UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
                checkoutBean.setAuthCode(text);
                checkoutBean.setPay_fee(shoukuan_tv.getText().toString());
                checkoutBean.setCash_price(shoukuan_tv.getText().toString());
                checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                SubmitCheckout();
            }
        });

        weixin_btn = popupView.findViewById(R.id.weixin_btn);
        zhifubao_btn = popupView.findViewById(R.id.zhifubao_btn);
        huiyuanka_btn = popupView.findViewById(R.id.huiyuanka_btn);
        xianjin_btn = popupView.findViewById(R.id.xianjin_btn);
        huiyuan_view = popupView.findViewById(R.id.huiyuan_view);
        huiyuankahao_tv = popupView.findViewById(R.id.huiyuankahao_tv);
        huiyuanyue_tv = popupView.findViewById(R.id.huiyuanyue_tv);
        huiyuanchaxun_btn = popupView.findViewById(R.id.huiyuanchaxun_btn);

        weixin_btn.setOnClickListener(v -> {
            if (!NetworkUtils.getInstance().isNetworkConnected(context)) {
                new DeleteShopPopupWindow(context, context.getString(R.string.Cash_only), true).show();
                return;
            }
            huiyuan_view.setVisibility(GONE);
            cleadView();
            weixin_btn.setBackgroundResource(R.drawable.blue_bg8);
            weixin_btn.setTextColor(Color.parseColor("#FF3B82F6"));
            pay_type = "wechat";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");
            qufen_btn.setVisibility(GONE);
        });
        xianjin_btn.setOnClickListener(v -> {
            huiyuan_view.setVisibility(GONE);
            cleadView();
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg8);
            xianjin_btn.setTextColor(Color.parseColor("#FF3B82F6"));
            pay_type = "cash";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");
            qufen_btn.setVisibility(VISIBLE);
        });
        zhifubao_btn.setOnClickListener(v -> {
            if (!NetworkUtils.getInstance().isNetworkConnected(context)) {
                new DeleteShopPopupWindow(context, context.getString(R.string.Cash_only), true).show();
                return;
            }
            huiyuan_view.setVisibility(GONE);
            cleadView();
            zhifubao_btn.setBackgroundResource(R.drawable.blue_bg8);
            zhifubao_btn.setTextColor(Color.parseColor("#FF3B82F6"));
            pay_type = "alipay";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");
            qufen_btn.setVisibility(GONE);
        });
        huiyuanka_btn.setOnClickListener(v -> {
            if (!NetworkUtils.getInstance().isNetworkConnected(context)) {
                new DeleteShopPopupWindow(context, context.getString(R.string.Cash_only), true).show();
                return;
            }
            huiyuan_view.setVisibility(VISIBLE);
            huiyuankahao_tv.setText("");
            huiyuanyue_tv.setText("");
            clubCardData = null;
            cleadView();
            huiyuanka_btn.setBackgroundResource(R.drawable.blue_bg8);
            huiyuanka_btn.setTextColor(Color.parseColor("#FF3B82F6"));
            pay_type = "wallet";
            shoukuan_tv.setText(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou)).toString());
            zhaolin_tv.setText("0.00");
            new ClubCardPopupWindow(context, clubCardBean -> {
                clubCardData = clubCardBean.getData().get(0);
                huiyuankahao_tv.setText(clubCardData.getNumber());
                huiyuanyue_tv.setText(clubCardData.getAmount());
            }).show();
            qufen_btn.setVisibility(GONE);
        });

        // --- NETS 支付点击事件集成 ---
        nets_purchase_btn.setOnClickListener(v -> handleNetsClick("netsp", nets_purchase_btn));
        nets_qr_btn.setOnClickListener(v -> handleNetsClick("netsqr", nets_qr_btn));
        nets_cc_btn.setOnClickListener(v -> handleNetsClick("netscc", nets_cc_btn));

        popupView.findViewById(R.id.guanbi_btn).setOnClickListener(v -> {
            popupWindow.dismiss();
        });
        initKey();
        time = new TimeCount(30000, 5000);
        huiyuanchaxun_btn.setOnClickListener(v -> {
            new ClubCardPopupWindow(context, clubCardBean -> {
                clubCardData = clubCardBean.getData().get(0);
                huiyuankahao_tv.setText(clubCardData.getNumber());
                huiyuanyue_tv.setText(clubCardData.getAmount());
            }).show();
        });
    }

    /**
     * 新增：处理 NETS 支付方式点击
     */
    private void handleNetsClick(String typeCode, TextView btn) {
        if (!NetworkUtils.getInstance().isNetworkConnected(context)) {
            new DeleteShopPopupWindow(context, "请检查网络连接", true).show();
            return;
        }
        huiyuan_view.setVisibility(GONE);
        cleadView();
        btn.setBackgroundResource(R.drawable.blue_bg8);
        btn.setTextColor(Color.parseColor("#FF3B82F6"));
        pay_type = typeCode;

        // 自动填入剩余金额
        BigDecimal remaining = new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou));
        shoukuan_tv.setText(remaining.toString());
        zhaolin_tv.setText("0.00");
        qufen_btn.setVisibility(GONE);


    }

    /**
     * 新增：调起 NETS 终端 Socket 通信
     */
    /**
     * 发起 NETS 终端通信逻辑
     */
    private void startNetsTransaction(String typeCode) {
        String amountStr = shoukuan_tv.getText().toString();
        if (TextUtils.isEmpty(amountStr) || new BigDecimal(amountStr).compareTo(BigDecimal.ZERO) <= 0) {
            Toast.makeText(context, "请输入有效金额", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. 金额转为分 (协议要求)
        int cents = new BigDecimal(amountStr).multiply(new BigDecimal("100")).intValue();
        // 2. 生成前端唯一流水号
        String myTxnId = "UHM" + System.currentTimeMillis();

        // ⭐ [日志 1：发起请求阶段]
        Log.e("NETS_DEBUG_REPORT", ">>> 1. 发起 NETS 交易 | 类型: " + typeCode + " | 金额: " + amountStr + " | 流水号: " + myTxnId);

        buildBean.show();

        new Thread(() -> {
            byte[] command;
            switch (typeCode) {
                case "netsp":
                    // 处理终端刷卡/插卡 (Purchase)
                    command = EcrSocketManager.netsPurchaseCommand(cents, myTxnId, false);
                    break;
                case "netsqr":
                    // 处理 QR 扫码支付
                    command = EcrSocketManager.netsPurchaseCommand(cents, myTxnId, true);
                    break;
                case "netscc":
                    // 显式处理信用卡 (Credit Card Sale)
                    command = EcrSocketManager.creditCardSaleCommand(cents, myTxnId);
                    break;
                default:
                    context.runOnUiThread(() -> {
                        DialogUIUtils.dismiss(buildBean);
                        Toast.makeText(context, "未知的支付类型: " + typeCode, Toast.LENGTH_SHORT).show();
                    });
                    return;
            }

            // ⭐ [日志 2：通信阶段]
            Log.e("NETS_DEBUG_REPORT", ">>> 2. 正在连接 ECR 终端... IP: " + UserUtils.getInstance().getEcrIp());

            // 执行 Socket 通讯
            Map<String, String> result = netsManager.executeCommand(
                    UserUtils.getInstance().getEcrIp(),
                    UserUtils.getInstance().getEcrPort(),
                    command
            );

            context.runOnUiThread(() -> {
                DialogUIUtils.dismiss(buildBean);
                String rawNetsJson = new Gson().toJson(result);

                // ⭐ [日志 3：终端响应阶段]
                Log.e("NETS_DEBUG_REPORT", ">>> 3. NETS 响应报文: " + rawNetsJson);

                if ("00".equals(result.get("ResponseCode"))) {
                    // ✅ 支付成功，进行像素级参数对齐
                    checkoutBean.setPay_type(typeCode);
                    checkoutBean.setPay_fee(amountStr);
                    checkoutBean.setAuthCode("");  // 必须为空字符串，对齐现金模式
                    checkoutBean.setTransaction_id(myTxnId);
                    checkoutBean.setNets_json(rawNetsJson); // 存储完整全报文

                    // 辅助统计字段补全
                    checkoutBean.setCash_price(amountStr);
                    checkoutBean.setCash_change("0.00");

                    Toast.makeText(context, "NETS 支付成功", Toast.LENGTH_SHORT).show();

                    // 调起统一提交逻辑
                    SubmitCheckout();
                } else {
                    String err = result.get("Error");
                    if (TextUtils.isEmpty(err)) err = "交易被拒绝 (" + result.get("ResponseCode") + ")";
                    Log.e("NETS_DEBUG_REPORT", ">>> 支付失败: " + err);
                    new DeleteShopPopupWindow(context, "NETS 支付失败: " + err, true).show();
                }
            });
        }).start();
    }

    public boolean isValidNumber(String input) {
        if (TextUtils.isEmpty(input)) return false;
        String regex = "^\\d+(\\.\\d*)?$";
        if (!input.matches(regex)) return false;
        int dotCount = input.length() - input.replace(".", "").length();
        return dotCount <= 1;
    }

    public static String detectPaymentType(String code) {
        if (TextUtils.isEmpty(code)) return "unknown";
        if (!code.matches("\\d+")) return "unknown";
        if (code.length() == 18 && code.matches("^(10|11|12|13|14|15)\\d{16}$")) {
            return "wechat";
        }
        if (code.length() >= 16 && code.length() <= 24 && code.matches("^(25|26|27|28|29|30)\\d+")) {
            return "alipay";
        }
        return "unknown";
    }


    public void show() {
        if (popupWindow.isShowing()) {
            return;
        }
        View rootView = ((Activity) context).getWindow().getDecorView();
        popupWindow.showAtLocation(rootView, Gravity.NO_GRAVITY, 0, 0);

        // 1. 获取支付类型，并加一个空保护
        pay_type = checkoutBean.getPay_type();
        if (pay_type == null) pay_type = "";

        cleadView();

        // 2. ⭐ 将 "cash".equals(pay_type) 写在前面，这样就算 pay_type 是 null 也不会崩
        if ("cash".equals(pay_type)) {
            xianjin_btn.setBackgroundResource(R.drawable.blue_bg8);
            xianjin_btn.setTextColor(Color.parseColor("#FF3B82F6"));
            shoukuan_tv.setText(checkoutBean.getTotal_fee() + "");
            qufen_btn.setVisibility(VISIBLE);
        } else {
            // 默认 fallback 到移动支付或其他
            weixin_btn.setBackgroundResource(R.drawable.blue_bg8);
            weixin_btn.setTextColor(Color.parseColor("#FF3B82F6"));
            pay_type = "wechat";
            shoukuan_tv.setText(checkoutBean.getTotal_fee() + "");
            qufen_btn.setVisibility(GONE);
        }
    }




    public void cleadView() {
        weixin_btn.setBackgroundResource(R.drawable.gray_line1);
        xianjin_btn.setBackgroundResource(R.drawable.gray_line1);
        zhifubao_btn.setBackgroundResource(R.drawable.gray_line1);
        huiyuanka_btn.setBackgroundResource(R.drawable.gray_line1);

        // 重置 NETS 按钮样式
        nets_purchase_btn.setBackgroundResource(R.drawable.gray_line1);
        nets_qr_btn.setBackgroundResource(R.drawable.gray_line1);
        nets_cc_btn.setBackgroundResource(R.drawable.gray_line1);

        weixin_btn.setTextColor(Color.parseColor("#FF000000"));
        xianjin_btn.setTextColor(Color.parseColor("#FF000000"));
        zhifubao_btn.setTextColor(Color.parseColor("#FF000000"));
        huiyuanka_btn.setTextColor(Color.parseColor("#FF000000"));

        nets_purchase_btn.setTextColor(Color.parseColor("#FF000000"));
        nets_qr_btn.setTextColor(Color.parseColor("#FF000000"));
        nets_cc_btn.setTextColor(Color.parseColor("#FF000000"));
    }

    public void dismiss() {
        popupWindow.dismiss();
    }

    @SuppressLint("SetTextI18n")
    private void initKey() {
        try {
            LinearLayout llkeyArea = (LinearLayout) popupView.findViewById(R.id.llkeyArea);
            for (int i = 0; i < llkeyArea.getChildCount(); i++) {
                // 第一部分：处理 1-9, 0, . , - 等数字按键
                if (i == 0) {
                    LinearLayout numview = (LinearLayout) llkeyArea.getChildAt(0);
                    for (int j = 0; j < numview.getChildCount(); j++) {
                        LinearLayout llNum01_09 = (LinearLayout) numview.getChildAt(j);
                        for (int u = 0; u < llNum01_09.getChildCount(); u++) {
                            llNum01_09.getChildAt(u).setOnClickListener(v -> {
                                if (v.getTag().toString().equals("-")) {
                                    if (!TextUtils.isEmpty(shoukuan_tv.getText().toString())) return;
                                }
                                if (v.getTag().toString().equals(".")) {
                                    if (shoukuan_tv.getText().toString().contains(".")) return;
                                }
                                if (!TextUtils.isEmpty(shoukuan_tv.getText().toString()) && isValidDecimal(shoukuan_tv.getText().toString(), 2)) {
                                    return;
                                }
                                shoukuan_tv.append(v.getTag().toString());
                            });
                        }
                    }
                }

                // 第二部分：处理 C (清空), D (删除), submit (确认) 按键
                if (i == 1) {
                    LinearLayout d_c_submit_view = (LinearLayout) llkeyArea.getChildAt(1);
                    for (int j = 0; j < d_c_submit_view.getChildCount(); j++) {
                        d_c_submit_view.getChildAt(j).setOnClickListener(v -> {
                            String tag = v.getTag().toString();

                            if (tag.equals("c")) {
                                shoukuan_tv.setText("");
                            } else if (tag.equals("d")) {
                                if (!shoukuan_tv.getText().toString().isEmpty()) {
                                    shoukuan_tv.setText(shoukuan_tv.getText().toString().substring(0, shoukuan_tv.getText().toString().length() - 1));
                                }
                            } else if (tag.equals("submit")) {
                                // --- 1. 公共校验：无论什么支付，都要先检查金额 ---
                                String inputAmount = shoukuan_tv.getText().toString();
                                if (TextUtils.isEmpty(inputAmount)) {
                                    new DeleteShopPopupWindow(context, context.getString(R.string.enter_payment_amount), true).show();
                                    return;
                                }
                                if (!isValidNumber(inputAmount)) {
                                    new DeleteShopPopupWindow(context, context.getString(R.string.Please_input_in_correct_price_format), true).show();
                                    return;
                                }
                                if (new BigDecimal(inputAmount).compareTo(BigDecimal.ZERO) <= 0) {
                                    new DeleteShopPopupWindow(context, context.getString(R.string.Amount_must_0), true).show();
                                    return;
                                }

                                // --- 2. 分支付方式执行逻辑 ---

                                // 情况 A: 现金支付
                                if (pay_type.equals("cash")) {
                                    checkoutBean.setPay_type(pay_type);
                                    checkoutBean.setPay_fee(inputAmount);
                                    checkoutBean.setCash_price(inputAmount);
                                    checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                                    if (!NetworkUtils.getInstance().isNetworkConnected(context)) {
                                        if (new BigDecimal(inputAmount).subtract(new BigDecimal(checkoutBean.getTotal_fee())).compareTo(BigDecimal.ZERO) < 0) {
                                            new DeleteShopPopupWindow(context, context.getString(R.string.Cannot_underpay), true).show();
                                            return;
                                        }
                                    }
                                    SubmitCheckout();
                                }
                                // 情况 B: 会员卡余额
                                else if (pay_type.equals("wallet")) {
                                    if (clubCardData == null) {
                                        new DeleteShopPopupWindow(context, "请先查询会员信息", true).show();
                                        return;
                                    }
                                    if (new BigDecimal(inputAmount).subtract(new BigDecimal(checkoutBean.getTotal_fee())).add(new BigDecimal(yinshou)).compareTo(BigDecimal.ZERO) > 0) {
                                        new DeleteShopPopupWindow(context, context.getString(R.string.Cannot_overpay), true).show();
                                        return;
                                    }
                                    if (new BigDecimal(clubCardData.getAmount()).subtract(new BigDecimal(inputAmount)).compareTo(BigDecimal.ZERO) < 0) {
                                        new DeleteShopPopupWindow(context, "会员卡余额不足", true).show();
                                        return;
                                    }
                                    checkoutBean.setPay_type(pay_type);
                                    checkoutBean.setPay_fee(inputAmount);
                                    checkoutBean.setCash_price(inputAmount);
                                    checkoutBean.setCash_change(zhaolin_tv.getText().toString());
                                    checkoutBean.setCardnumber(clubCardData.getNumber());
                                    SubmitCheckout();
                                }
                                // 情况 C: NETS 系列支付 (netsp, netsqr, netscc)
                                else if (pay_type.equals("netsp") || pay_type.equals("netsqr") || pay_type.equals("netscc")) {
                                    // ✅ 这里改为：虚拟键盘点确认后，正式调起 NETS
                                    startNetsTransaction(pay_type);
                                }
                                // 情况 D: 其他扫码支付 (微信/支付宝)
                                else {
                                    if (new BigDecimal(inputAmount).subtract(new BigDecimal(checkoutBean.getTotal_fee())).add(new BigDecimal(yinshou)).compareTo(BigDecimal.ZERO) > 0) {
                                        new DeleteShopPopupWindow(context, context.getString(R.string.Cannot_overpay), true).show();
                                        return;
                                    }
                                    shoukuan_tv.removeCallbacks(shoukuan_tvRunnable);
                                    shoukuan_tv.clearFocus();
                                    deleteShopPopupWindow.show();
                                }
                            }
                        });
                    }
                }
            }
        } catch (Exception ex) {
            Log.i("错误返回", ex.getMessage() + "");
        }
    }



    public static boolean isValidDecimal(String input, int maxDecimalDigits) {
        if (input == null || input.isEmpty()) return false;
        if (!input.contains(".")) return false;
        int dotIndex = input.indexOf('.');
        return dotIndex != -1 && input.substring(dotIndex + 1).length() >= maxDecimalDigits;
    }

    public String order_sn = "";
    public String xinjin_pice = "", weixin_pice = "", zhifubao_pice = "", huiyuanka_pice = "";

    /**
     * 统一提交订单到后端接口
     */
    public void SubmitCheckout() {
        if (Utilis.isFastClick()) {
            return;
        }
        buildBean.show();
        checkoutBean.setOrder_sn(order_sn);

        // 1. 判定支付类型 (1=普通, 2=组合) 与订单状态
        BigDecimal currentPay = new BigDecimal(shoukuan_tv.getText().toString());
        BigDecimal remaining = new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou));

        if (currentPay.compareTo(remaining) < 0) {
            order_status = 1; // 部分支付
            checkoutBean.setType(2);
        } else {
            // 如果 order_status 还是初始值 0，说明是第一笔就付清，设为 1；否则设为 2
            checkoutBean.setType(order_status == 0 ? 1 : 2);
            order_status = 2; // 已结清
        }

        if (!isyouhuijuan) {
            checkoutBean.setCoupon_fee("");
        }
        checkoutBean.setXf_type("1");
        checkoutBean.setOrder_status(order_status);
        checkoutBean.setShop_id(UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        checkoutBean.setUser_id(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getId());

        String url = POSApiSerview.POS_URL + POSApiSerview.addOrder;
        Gson gson = new Gson();
        String finalJsonBody = gson.toJson(checkoutBean);

        // ⭐ [日志 4：最终报账阶段]
        Log.e("NETS_DEBUG_REPORT", ">>> 4. 最终发给后端 (addOrderzh) 的数据: " + finalJsonBody);

        if (!NetworkUtils.getInstance().isNetworkConnected(context)) {
            // --- 离线处理逻辑 (保持不变) ---
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            checkoutBean.setPay_time(sdf.format(System.currentTimeMillis()));
            ArrayList<CheckoutBean> list = new ArrayList<>();
            if (!TextUtils.isEmpty(UserUtils.getInstance().getOrderListJson())) {
                list = (ArrayList<CheckoutBean>) GsonSandL.getInstance().GsonStoL(UserUtils.getInstance().getOrderListJson(), CheckoutBean.class);
            }
            list.add(checkoutBean);
            UserUtils.getInstance().setOrderListJson(context, gson.toJson(list));

            DialogUIUtils.dismiss(buildBean);
            popupWindow.dismiss();
            checkoutOnClickListener.onClick(checkoutBean, shoukuan_tv.getText().toString(), weixin_pice, zhifubao_pice, huiyuanka_pice);
            MyPrinterHelper.getInstance().asyncPrintCheckout(context, checkoutBean, null, shoukuan_tv.getText().toString(), weixin_pice, zhifubao_pice, huiyuanka_pice, order_sn);
        } else {
            // --- 在线提交逻辑 ---
            RequestBody body = RequestBody.create(finalJsonBody, MediaType.parse("application/json; charset=utf-8"));
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken())
                    .post(body)
                    .build();

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .addInterceptor(new NetworkErrorInterceptor())
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                    context.runOnUiThread(() -> {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(context, "请求失败: " + e.getMessage(), true).show();
                    });
                }

                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                    final int statusCode = response.code();
                    final String rawBody = response.body() != null ? response.body().string() : "";

                    // ⭐ [后端响应审计日志]
                    Log.e("BACKEND_DEBUG", "======= 接口响应详情 =======");
                    Log.e("BACKEND_DEBUG", "状态码: " + statusCode);
                    Log.e("BACKEND_DEBUG", "返回内容: " + rawBody);
                    Log.e("BACKEND_DEBUG", "============================");

                    context.runOnUiThread(() -> {
                        DialogUIUtils.dismiss(buildBean);
                        if (!response.isSuccessful()) {
                            new DeleteShopPopupWindow(context, "服务器报错: " + statusCode, true).show();
                            return;
                        }
                        if (TextUtils.isEmpty(rawBody.trim())) {
                            new DeleteShopPopupWindow(context, "后端返回空数据，请检查接口", true).show();
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(rawBody);
                            // 调用统一的响应处理方法（即之前添加的 handleBackendResponse）
                            handleBackendResponse(jsonObject);
                        } catch (JSONException e) {
                            Log.e("BACKEND_DEBUG", "JSON 解析失败: " + rawBody);
                            new DeleteShopPopupWindow(context, "数据格式错误", true).show();
                        }
                    });
                }
            });
        }
    }

    public String out_trade_no = "";

    public void fwsgetOrderInformation() {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.fwsgetOrderInformation;
        Request.Builder builder = new Request.Builder().url(url);
        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        builder.post(formBody);

        Request request = builder.build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.SECONDS).addInterceptor(new NetworkErrorInterceptor()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(() -> { DialogUIUtils.dismiss(buildBean); new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected), true).show(); });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        context.runOnUiThread(() -> {
                            try {
                                JSONObject jsonObject = new JSONObject(success);
                                String trade_state_desc = new JSONObject(jsonObject.getString("code")).getString("trade_state_desc");
                                if (trade_state_desc.contains("输入支付密码")) {
                                    fwsgetOrderInformation();
                                } else if (trade_state_desc.contains("支付成功")) {
                                    checkoutBean.setTransaction_id(new JSONObject(jsonObject.getString("code")).getString("transaction_id"));
                                    checkoutBean.setOrder_sn(order_sn);
                                    pushorders(checkoutBean);
                                } else if (trade_state_desc.contains("支付失败")) {
                                    fwscancelanOrder();
                                } else if (trade_state_desc.contains("订单已撤销")) {
                                    order_sn = ""; out_trade_no = "";
                                    DialogUIUtils.dismiss(buildBean);
                                    new DeleteShopPopupWindow(context, context.getString(R.string.order_canceled), true).show();
                                }
                            } catch (JSONException e) { Log.e("ttt", "Error occurred", e); }
                        });
                    } catch (Exception e) { Log.e("ttt", "Error occurred", e); }
                }
            }
        });
    }

    public void queryOrder() {
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.queryOrder;
        Request.Builder builder = new Request.Builder().url(url);
        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        builder.post(formBody);

        Request request = builder.build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.SECONDS).addInterceptor(new NetworkErrorInterceptor()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(() -> { DialogUIUtils.dismiss(buildBean); new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected), true).show(); });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        context.runOnUiThread(() -> {
                            try {
                                JSONObject jsonObject = new JSONObject(success);
                                String trade_status = jsonObject.getString("trade_status");
                                if (is_chaoshi && !trade_status.contains("TRADE_FINISHED") && !trade_status.contains("TRADE_SUCCESS")) {
                                    is_chaoshi = false; revokeOrder(); return;
                                }
                                if (trade_status.contains("TRADE_FINISHED") || trade_status.contains("TRADE_SUCCESS")) {
                                    time.cancel();
                                    checkoutBean.setTransaction_id(jsonObject.getString("trade_no"));
                                    checkoutBean.setOrder_sn(order_sn);
                                    pushorders(checkoutBean);
                                }
                                if (trade_status.contains("TRADE_CLOSED")) {
                                    order_sn = ""; out_trade_no = "";
                                    DialogUIUtils.dismiss(buildBean);
                                    new DeleteShopPopupWindow(context, context.getString(R.string.order_canceled), true).show();
                                }
                            } catch (JSONException e) { Log.e("ttt", "Error occurred", e); }
                        });
                    } catch (Exception e) { Log.e("ttt", "Error occurred", e); }
                }
            }
        });
    }

    public void fwscancelanOrder() {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.fwscancelanOrder;
        Request.Builder builder = new Request.Builder().url(url);
        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        builder.post(formBody);

        Request request = builder.build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.SECONDS).addInterceptor(new NetworkErrorInterceptor()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(() -> { DialogUIUtils.dismiss(buildBean); new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected), true).show(); });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        context.runOnUiThread(this::fwsgetOrderInformation);
                    } catch (Exception e) { Log.e("ttt", "Error occurred", e); }
                }
            }

            private void fwsgetOrderInformation() {
                CheckoutPopupWindow.this.fwsgetOrderInformation();
            }
        });
    }

    public void revokeOrder() {
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.revokeOrder;
        Request.Builder builder = new Request.Builder().url(url);
        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        builder.post(formBody);

        Request request = builder.build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.SECONDS).addInterceptor(new NetworkErrorInterceptor()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(() -> { DialogUIUtils.dismiss(buildBean); new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected), true).show(); });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        context.runOnUiThread(() -> {
                            order_sn = ""; out_trade_no = "";
                            DialogUIUtils.dismiss(buildBean);
                            new DeleteShopPopupWindow(context, context.getString(R.string.order_canceled), true).show();
                        });
                    } catch (Exception e) { Log.e("ttt", "Error occurred", e); }
                }
            }
        });
    }

    public void pushorders(CheckoutBean checkoutBean) {
        String url = POSApiSerview.POS_URL + POSApiSerview.pushorders;
        Gson gson = new Gson();
        RequestBody body = RequestBody.create(gson.toJson(checkoutBean), MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder().url(url);
        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        builder.post(body);

        Request request = builder.build();
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10000, TimeUnit.SECONDS).addInterceptor(new NetworkErrorInterceptor()).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                context.runOnUiThread(() -> { DialogUIUtils.dismiss(buildBean); new DeleteShopPopupWindow(context, context.getString(R.string.no_network_detected), true).show(); });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String success = response.body().string();
                    context.runOnUiThread(() -> {
                        try {
                            JSONObject jsonObject = new JSONObject(success);
                            if (jsonObject.getString("msg").contains("成功") || jsonObject.getString("msg").contains("Success")) {
                                DialogUIUtils.dismiss(buildBean);
                                MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.yidong);
                                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                                mediaPlayer.start();

                                if (pay_type.equals("wechat")) { weixin_pice = shoukuan_tv.getText().toString(); }
                                if (pay_type.equals("alipay")) { zhifubao_pice = shoukuan_tv.getText().toString(); }

                                yinshou = new BigDecimal(yinshou).add(new BigDecimal(shoukuan_tv.getText().toString())).toString();
                                yishou_tv.setText(yinshou);

                                if (order_status == 2) {
                                    deleteShopPopupWindow.dismiss();
                                    popupWindow.dismiss();
                                    checkoutOnClickListener.onClick(checkoutBean, xinjin_pice, weixin_pice, zhifubao_pice, huiyuanka_pice);
                                    MyPrinterHelper.getInstance().asyncPrintCheckout(context, checkoutBean, null, xinjin_pice, weixin_pice, zhifubao_pice, huiyuanka_pice, order_sn);
                                    order_sn = ""; out_trade_no = "";
                                } else {
                                    new DeleteShopPopupWindow(context, context.getString(R.string.Payment_succeeded), true).show();
                                    deleteShopPopupWindow.dismiss();
                                    cleadView();
                                    xianjin_btn.setBackgroundResource(R.drawable.blue_bg8);
                                    xianjin_btn.setTextColor(Color.parseColor("#FF3B82F6"));
                                    pay_type = "cash";
                                    huiyuan_view.setVisibility(GONE);
                                    BigDecimal daizhifu = new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou));
                                    MyPresentation.setDaizhifu_tv(daizhifu.toString());
                                    shoukuan_tv.setText(daizhifu.toString());
                                }
                            }
                        } catch (JSONException e) { Log.e("ttt", "Error occurred", e); }
                    });
                }
            }
        });
    }

    public void operateDetails() {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        String url = POSApiSerview.POS_URL + POSApiSerview.operateDetails;
        OkHttpUtil.postFormAsync(url, params, context, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                context.runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        int code = jsonObject.getInt("code");
                        if (code == 1) {
                            ArrayList<PrintDataBean> printDataBeanArrayList = new Gson().fromJson(jsonObject.getString("data"), new TypeToken<ArrayList<PrintDataBean>>() {}.getType());
                            MyPrinterHelper.getInstance().asyncPrintCheckout(context, checkoutBean, printDataBeanArrayList.get(0), xinjin_pice, weixin_pice, zhifubao_pice, huiyuanka_pice, order_sn);
                        } else {

                            MyPrinterHelper.getInstance().asyncPrintCheckout(context, checkoutBean, null, xinjin_pice, weixin_pice, zhifubao_pice, huiyuanka_pice, order_sn);
                        }
                    } catch (JSONException e) { Log.e("ttt", "Error occurred", e); }
                });
            }
            @Override public void onFailure(IOException e) {}
        });
    }

    private TimeCount time;
    private boolean is_chaoshi = false;

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) { super(millisInFuture, countDownInterval); }
        @Override
        public void onTick(long millisUntilFinished) { queryOrder(); }
        @Override
        public void onFinish() { is_chaoshi = true; }
    }


    /**
     * 核心逻辑：处理后端 addOrderzh 接口成功后的业务流程
     * 支持组合支付：如果没付清，则更新 UI 准备下一笔支付
     */
    private void handleBackendResponse(JSONObject jsonObject) {
        try {
            // 1. 判断后端是否真的处理成功
            if (jsonObject.getString("msg").contains("成功") || jsonObject.getString("msg").contains("Success")) {

                // 播放支付成功音效
                MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.yidong);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(MediaPlayer::release);
                mediaPlayer.start();

                // 2. 统计金额：将本次支付的金额累加到“已收” (yinshou) 中
                String currentPayAmount = shoukuan_tv.getText().toString();
                yinshou = new BigDecimal(yinshou).add(new BigDecimal(currentPayAmount)).toString();
                yishou_tv.setText("￥" + yinshou);

                // 3. 判断订单是否彻底结清
                if (order_status == 2) {
                    // ✅ 全部付清：记录后端返回的 code (通常是 order_sn)
                    order_sn = jsonObject.optString("code", "");

                    // 关闭弹窗并回调 MainActivity 刷新 UI
                    popupWindow.dismiss();
                    checkoutOnClickListener.onClick(checkoutBean, xinjin_pice, weixin_pice, zhifubao_pice, huiyuanka_pice);

                    // 打印小票 (传入 8 个完整参数)
                    MyPrinterHelper.getInstance().asyncPrintCheckout(context, checkoutBean, null, xinjin_pice, weixin_pice, zhifubao_pice, huiyuanka_pice, order_sn);

                    // 重置流水号，防止下一单误用
                    order_sn = "";
                    out_trade_no = "";
                } else {
                    // 🌗 组合支付：本笔成功，但还有余款
                    new DeleteShopPopupWindow(context, "本笔支付成功，请继续支付余款", true).show();

                    // 自动计算并显示剩余待付金额
                    BigDecimal remaining = new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(yinshou));
                    shoukuan_tv.setText(remaining.toString());
                    zhaolin_tv.setText("0.00");

                    // 清除按钮选中状态，让收银员选下一种支付方式
                    cleadView();
                }
            } else {
                // 后端逻辑返回失败
                new DeleteShopPopupWindow(context, "提交失败: " + jsonObject.optString("msg"), true).show();
            }
        } catch (Exception e) {
            Log.e("ttt", "处理后端响应异常", e);
        }
    }


}