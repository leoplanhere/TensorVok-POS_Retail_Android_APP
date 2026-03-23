package com.uhm.uhmcs.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import org.json.JSONArray;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.media.AudioAttributes; // ★ 新增
import android.media.MediaRouter;
import android.media.SoundPool;      // ★ 新增
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.text.TextWatcher;
import android.text.Editable;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.bumptech.glide.Glide;
import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lxj.xpopup.XPopup;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.GrouponGoodsAdapter;
import com.uhm.uhmcs.adapter.SelectedShopAdapter;
import com.uhm.uhmcs.adapter.ShopTypeAdapter;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.MemberBean;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.bean.RegistrationShopBean;
import com.uhm.uhmcs.http.NetworkLatencyMonitor;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.popupwindow.*;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.MyUsbDeviceHelper;
import com.uhm.uhmcs.utils.NetworkUtils;
import com.uhm.uhmcs.utils.SerializableUtils;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.utils.Utilis;
import com.uhm.uhmcs.view.CustomInputTextView;
import com.uhm.uhmcs.view.MyPresentation;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;
import org.litepal.LitePal;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class MainActivity extends Activity implements View.OnClickListener {

    // UI 组件
    private RecyclerView rv_choose_menu3, shop_rv, selected_shop_rv;
    private LinearLayoutManager selected_LinearLayoutManager;
    private ShopTypeAdapter shopTypeAdapter;
    private CheckoutPopupWindow currentCheckoutPopup;

    private TextView tv_empty_cart;
    private GrouponGoodsAdapter grouponGoodsAdapter;
    private SelectedShopAdapter selectedShopAdapter;
    private CustomInputTextView et_tiaoxingma;

    private EditText et_search_keyword; // 【新增】手动关键词搜索框


    private Animation animation;
    private TextView tv_zongjia, tv_zongjian, qingkong_btn, qudan_btn, guadan_btn, dazhe_one_btn, dazhe_all_btn, checkout_btn, daying_btn;
    private LinearLayout huiyuan_btn;
    private TextView huiyuan_name, pingText, httpText, bendin_view, caozuo_view, tv_nickname, tv_phone;
    private LinearLayout have_paid_view, wangluo_view;
    private View shop_mocheng;

    // 【新增】上一单详情相关控件
    private LinearLayout llLastOrderInfo;
    private TextView tvLastTime, tvLastType, tvLastCount, tvLastTotal, tvLastDiscount, tvLastPay;
    // 【新增】状态锁
    private boolean isShowingLastOrder = false;

    // 数据变量
    private Integer selectedShopIndex;
    private BigDecimal zongjia = new BigDecimal("0.00");
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> selectedShopList = new ArrayList<>();
    private ArrayList<RegistrationShopBean> registrationShopBeanArrayList = new ArrayList<>();
    private String memben_discount;
    private boolean is_tongbu = false;
    private BuildBean buildBean;
    private MyPresentation presentation;
    private NetworkLatencyMonitor monitor;
    private NetworkChangeReceiver networkChangeReceiver;

    // 线程池用于数据库操作，避免主线程卡顿
    private final ExecutorService dbExecutor = Executors.newSingleThreadExecutor();

    // 同步和分页相关
    private static final int SYNC_PAGE_SIZE = 1000;
    private static final int UI_PAGE_SIZE = 20;
    private int ui_current_page = 0;
    private String current_category_id = ""; // 当前选中的分类ID
    private SyncLoadingPopup loadingPopup;

    // 弹窗
    private DeleteShopPopupWindow deleteShopPopupWindow;
    private GetRegistrationShopPopupWindow getRegistrationShopPopupWindow;

    // 支付相关
    private boolean is_kuangjie = false;
    private boolean is_jiezhang_qingkong = false;
    private int allNum = 0;
    public String out_trade_no = "", order_sn = "", transaction_id = "";
    private TimeCount timeCount;
    private boolean is_chaoshi = false;
    private CheckoutBean checkoutBean;

    // ★★★ 新增：声音反馈相关变量 ★★★
    private SoundPool soundPool;
    private int soundID_success;
    private int soundID_error;

    // 退款监听
    private PaymentStatusListener paymentStatusListener;

    public interface PaymentStatusListener {
        void onRefundCompleted(boolean success, int position);
    }

    public void setPaymentStatusListener(PaymentStatusListener listener) {
        this.paymentStatusListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ★★★ 初始化声音池 ★★★
        initSoundPool();

        initView();
        Log.i("MainActivity", ">>>>onCreate>>>>");
        MyUsbDeviceHelper.getInstance().inti(this);

        boolean isConnected = NetworkUtils.getInstance().isNetworkConnected(this);
        wangluo_view.setVisibility(isConnected ? GONE : VISIBLE);

        if (isConnected) {
            OverviewList(); // 加载分类（联网）

            // 异步检查数据库并决定是否同步
            dbExecutor.execute(() -> {
                int count = LitePal.count(GrouponGoodsBean.GrouponGoodsModel.class);
                runOnUiThread(() -> {
                    if (count == 0) {
                        syncGoodsData(); // 没数据，自动同步
                    } else {
                        loadLocalGoods(0); // 有数据，直接加载
                    }
                });
            });
        } else {
            loadLocalCategories(); // 离线加载分类
            loadLocalGoods(0);     // 离线加载商品
        }
        checkPendingOrders();

        if (networkChangeReceiver == null) {
            networkChangeReceiver = new NetworkChangeReceiver(this);
            registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        }
    }

    // ★★★ 新增：初始化 SoundPool 的方法 ★★★
    private void initSoundPool() {
        try {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            soundPool = new SoundPool.Builder()
                    .setMaxStreams(2) // 允许同时播放2个声音
                    .setAudioAttributes(audioAttributes)
                    .build();

            // 加载资源文件 (需确保 res/raw 下有这两个文件，否则可以注释掉避免崩溃)
            soundID_success = soundPool.load(this, R.raw.scan_success, 1);
            soundID_error = soundPool.load(this, R.raw.scan_error, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ★★★ 新增：播放成功声音 ★★★
    private void playSuccessSound() {
        if (soundPool != null) {
            // 参数：ID, 左声道, 右声道, 优先级, 循环次数, 速率
            soundPool.play(soundID_success, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    // ★★★ 新增：播放失败声音 ★★★
    private void playErrorSound() {
        if (soundPool != null) {
            soundPool.play(soundID_error, 1.0f, 1.0f, 0, 0, 1.0f);
        }
    }

    private void checkPendingOrders() {
        if (TextUtils.isEmpty(UserUtils.getInstance().getOrderListJson())) {
            bendin_view.setVisibility(GONE);
        } else {
            bendin_view.setVisibility(VISIBLE);
        }
    }

    private void initView() {
        initPresentation();
        bindViews();
        setupAdapters();
        setupListeners();
        setupNetworkMonitor();
    }

    private void initPresentation() {
        MediaRouter mediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        if (route != null) {
            Display presentationDisplay = route.getPresentationDisplay();
            if (presentationDisplay != null) {
                presentation = new MyPresentation(this, presentationDisplay);
                presentation.show();
            }
        }
    }

    private void bindViews() {




        pingText = findViewById(R.id.pingValue);

        tv_empty_cart = findViewById(R.id.tv_empty_cart);

        if (tv_empty_cart != null) {
            tv_empty_cart.setVisibility(View.VISIBLE);
        }
        httpText = findViewById(R.id.httpValue);
        tv_nickname = findViewById(R.id.tv_nickname);
        tv_phone = findViewById(R.id.tv_phone);
        tv_zongjia = findViewById(R.id.tv_zongjia);
        tv_zongjian = findViewById(R.id.tv_zongjian);
        caozuo_view = findViewById(R.id.caozuo_view);
        shop_mocheng = findViewById(R.id.shop_mocheng);
        bendin_view = findViewById(R.id.bendin_view);
        wangluo_view = findViewById(R.id.wangluo_view);
        have_paid_view = findViewById(R.id.have_paid_view);
        huiyuan_name = findViewById(R.id.huiyuan_name);
        et_tiaoxingma = findViewById(R.id.et_tiaoxingma);
        et_search_keyword = findViewById(R.id.et_search_keyword); // ★ 新增这一行

        qingkong_btn = findViewById(R.id.qingkong_btn);
        guadan_btn = findViewById(R.id.guadan_btn);
        qudan_btn = findViewById(R.id.qudan_btn);
        dazhe_one_btn = findViewById(R.id.dazhe_one_btn);
        dazhe_all_btn = findViewById(R.id.dazhe_all_btn);
        checkout_btn = findViewById(R.id.checkout_btn);
        huiyuan_btn = findViewById(R.id.huiyuan_btn);
        daying_btn = findViewById(R.id.daying_btn);


        // 【新增】初始化上一单控件
        llLastOrderInfo = findViewById(R.id.ll_last_order_info);
        tvLastTime = findViewById(R.id.tv_last_time);
        tvLastType = findViewById(R.id.tv_last_type);
        tvLastCount = findViewById(R.id.tv_last_count);
        tvLastTotal = findViewById(R.id.tv_last_total);
        tvLastDiscount = findViewById(R.id.tv_last_discount);
        tvLastPay = findViewById(R.id.tv_last_pay);



        if (UserUtils.getInstance().getLoginBase() != null && UserUtils.getInstance().getLoginBase().getData() != null) {
            tv_nickname.setText(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname());
            tv_phone.setText(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getMobile());
        }

        Glide.with(this).load(R.drawable.have_paid_img).into((ImageView) findViewById(R.id.image));
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_click);
        buildBean = DialogUIUtils.showLoading(this, getString(R.string.paying), true, false, false, false);
        timeCount = new TimeCount(90000, 3000);



    }



    /**
     * 【新增】显示上一单详情
     */
    private void showLastOrderInfo(String payType, int count, String total, String discount, String realPay) {
        isShowingLastOrder = true;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        String currentTime = sdf.format(new java.util.Date());

        // 转换支付名称
        String typeName = "现金支付";
        if ("wechat".equals(payType)) typeName = "微信支付";
        else if ("alipay".equals(payType)) typeName = "支付宝";
        else if ("cash".equals(payType)) typeName = "现金支付";

        if (tvLastTime != null) {
            tvLastTime.setText("时间：" + currentTime);
            tvLastType.setText("方式：" + typeName);
            tvLastCount.setText("数量：" + count);
            tvLastTotal.setText("总额：" + (total.startsWith("￥") ? total : "￥" + total));
            tvLastDiscount.setText("优惠：" + (discount.startsWith("￥") ? discount : "￥" + discount));
            tvLastPay.setText("实付：" + (realPay.startsWith("￥") ? realPay : "￥" + realPay));
        }

        if (selected_shop_rv != null) selected_shop_rv.setVisibility(View.GONE);
        if (tv_empty_cart != null) tv_empty_cart.setVisibility(View.GONE);
        if (llLastOrderInfo != null) {
            llLastOrderInfo.setVisibility(View.VISIBLE);
            llLastOrderInfo.bringToFront();
        }
    }

    /**
     * 【新增】隐藏上一单详情
     */
    private void hideLastOrderInfo() {
        isShowingLastOrder = false;
        if (llLastOrderInfo != null && llLastOrderInfo.getVisibility() == View.VISIBLE) {
            llLastOrderInfo.setVisibility(View.GONE);
        }
    }



    private void setupAdapters() {
        selected_shop_rv = findViewById(R.id.selected_shop_rv);
        selected_LinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        selected_shop_rv.setLayoutManager(selected_LinearLayoutManager);
        selectedShopAdapter = new SelectedShopAdapter(this, R.layout.item_selected_shop);
        selected_shop_rv.setAdapter(selectedShopAdapter);
        ((SimpleItemAnimator) selected_shop_rv.getItemAnimator()).setSupportsChangeAnimations(false);

        rv_choose_menu3 = findViewById(R.id.rv_choose_menu3);
        rv_choose_menu3.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        shopTypeAdapter = new ShopTypeAdapter(this, R.layout.item_shop_type);
        rv_choose_menu3.setAdapter(shopTypeAdapter);

        shop_rv = findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new GridLayoutManager(this, 4));
        grouponGoodsAdapter = new GrouponGoodsAdapter(this, R.layout.item_groupon_goods);
        shop_rv.setAdapter(grouponGoodsAdapter);
        grouponGoodsAdapter.setPreLoadNumber(3);
    }

    private void setupListeners() {
        findViewById(R.id.delete_shop).setOnClickListener(this);
        findViewById(R.id.shanchuhuiyuan_btn).setOnClickListener(this);
        findViewById(R.id.more_function_btn).setOnClickListener(this);
        findViewById(R.id.add_no_code_btn).setOnClickListener(this);

        qingkong_btn.setOnClickListener(this);
        guadan_btn.setOnClickListener(this);
        qudan_btn.setOnClickListener(this);
        dazhe_one_btn.setOnClickListener(this);
        dazhe_all_btn.setOnClickListener(this);
        checkout_btn.setOnClickListener(this);
        huiyuan_btn.setOnClickListener(this);
        daying_btn.setOnClickListener(this);

        updateDiscountView();

        shop_mocheng.setOnClickListener(v -> {});
        shop_mocheng.setVisibility(!UserUtils.getInstance().isDianji() ? VISIBLE : GONE);

        et_tiaoxingma.setOnClickListener(v -> et_tiaoxingma.postDelayed(() -> et_tiaoxingma.requestFocus(), 100));
        et_tiaoxingma.postDelayed(() -> et_tiaoxingma.requestFocus(), 100);

        et_tiaoxingma.setOnInputCompleteListener(this::handleScanInput);

        // ★★★ 新增：手动关键词搜索监听逻辑 ★★★
        if (et_search_keyword != null) {
            // 1. 设置触摸监听：判定是否点击了右侧的“叉叉”
            et_search_keyword.setOnTouchListener((v, event) -> {
                // 获取右侧图标 (index 为 2)
                android.graphics.drawable.Drawable drawableRight = et_search_keyword.getCompoundDrawables()[2];

                // 如果没有叉叉图标，直接放行，让系统处理点击（弹出键盘）
                if (drawableRight == null) return false;

                // 计算点击区域是否在叉叉图标上
                // 计算公式：输入框总宽度 - 右侧内边距 - 图标宽度 - 额外感应区
                boolean isClickClear = event.getX() >= (et_search_keyword.getWidth() - et_search_keyword.getPaddingRight() - drawableRight.getIntrinsicWidth() - 50);

                if (isClickClear) {
                    // 只有手指抬起时才执行清空动作
                    if (event.getAction() == android.view.MotionEvent.ACTION_UP) {
                        et_search_keyword.setText("");
                        et_tiaoxingma.requestFocus();
                    }
                    // 关键点：点击叉叉区域时，无论是按下还是抬起，都返回 true，表示这个点击是我们要拦截的
                    return true;
                }

                // 关键点：点击非叉叉区域（即文字区域），必须返回 false
                // 这样系统才会认为你是在正常点击输入框，从而立即弹出键盘
                return false;
            });

            // 2. 键盘回车监听（保留之前的逻辑）
            et_search_keyword.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    et_tiaoxingma.requestFocus();
                    return true;
                }
                return false;
            });

            // 3. 文字变化监听：根据是否有文字显示/隐藏“叉叉”
            et_search_keyword.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String keyword = s.toString().trim();

                    if (keyword.length() > 0) {
                        // 有文字时，显示右侧图标 (使用你的 guanbi.xml)
                        et_search_keyword.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.guanbi, 0);
                    } else {
                        // 没文字时，隐藏图标
                        et_search_keyword.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
                    }

                    searchLocalGoods(keyword); // 执行实时本地搜索
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }






        selectedShopAdapter.setOnItemClickListener((adapter, view, position) -> {
            for (GrouponGoodsBean.GrouponGoodsModel model : selectedShopAdapter.getData()) {
                model.setSelected(false);
            }
            if (position >= 0 && position < selectedShopAdapter.getData().size()) {
                selectedShopAdapter.getData().get(position).setSelected(true);
                selectedShopIndex = position;
                selectedShopAdapter.notifyDataSetChanged();
            }
        });

        selectedShopAdapter.setOnItemChildClickListener((adapter, view, position) -> handleCartItemAction(view.getId(), position));

        // ▼▼▼▼▼▼ 核心修复：分类Tab点击事件 ▼▼▼▼▼▼
        shopTypeAdapter.setOnItemClickListener((adapter, view, position) -> {
            // 1. 设置选中状态
            shopTypeAdapter.setIndex(position);

            // 2. 获取分类数据
            CategoryListBean.CategoryListModel item = shopTypeAdapter.getData().get(position);

            // 3. 提取ID（如果是"全部"，ID通常为空字符串）
            if (item != null) {
                current_category_id = TextUtils.isEmpty(item.getId()) ? "" : item.getId();
            } else {
                current_category_id = "";
            }

            Log.i("MainActivity", "切换分类: " + (item != null ? item.getName() : "null") + " ID: " + current_category_id);

            // ★ 切换分类时自动清空手动搜索框，确保显示的是该分类下的数据
            if (et_search_keyword != null) {
                et_search_keyword.setText("");
            }

            // 4. 重置列表并加载第一页
            loadLocalGoods(0);
        });
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

        grouponGoodsAdapter.setOnItemClickListener((adapter, view, position) -> {
            view.startAnimation(animation);
            addGoodsToCart(grouponGoodsAdapter.getData().get(position));
        });

        shop_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!grouponGoodsAdapter.hasMore) return;
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastVisibleItemPosition() >= layoutManager.getItemCount() - 1) {
                    loadLocalGoods(ui_current_page + 1);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE) Glide.with(MainActivity.this).resumeRequests();
                else Glide.with(MainActivity.this).pauseRequests();
            }
        });
    }

    private void setupNetworkMonitor() {
        monitor = new NetworkLatencyMonitor();
        monitor.startMonitoring((pingMs, httpMs) -> runOnUiThread(() -> {
            if (isFinishing()) return;
            pingText.setText(String.valueOf(pingMs));
            httpText.setText(String.valueOf(httpMs));
            updateColor(pingText, pingMs);
            updateColor(httpText, httpMs);
        }));
    }

    // ----------------- 数据同步 -----------------

    private void syncGoodsData() {
        is_tongbu = true;
        // ★ 核心修改 1：同步前先在子线程清空本地库，防止新旧数据冲突
        dbExecutor.execute(() -> {
            LitePal.deleteAll(GrouponGoodsBean.GrouponGoodsModel.class);

            runOnUiThread(() -> {
                loadingPopup = (SyncLoadingPopup) new XPopup.Builder(this)
                        .dismissOnTouchOutside(false)
                        .dismissOnBackPressed(false)
                        .asCustom(new SyncLoadingPopup(this))
                        .show();
                downloadGoodsPage(1); // 开始从第一页下载
            });
        });
    }


    private void downloadGoodsPage(int requestPage) {
        // 1. 获取 shop_id
        String shopId = "";
        try {
            if (UserUtils.getInstance().getShopDataBean() != null &&
                    UserUtils.getInstance().getShopDataBean().getData() != null &&
                    !UserUtils.getInstance().getShopDataBean().getData().isEmpty()) {
                shopId = UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid();
            }
        } catch (Exception e) {
            Log.e("SyncDebug", "获取店铺ID异常: " + e.getMessage());
        }

        if (TextUtils.isEmpty(shopId)) {
            Log.e("SyncDebug", "致命错误: shop_id 为空！");
            finishSync();
            return;
        }

        // 2. 请求接口
        String url = POSApiSerview.POS_URL + "Supermarket/getGrouponGoods1";
        Map<String, String> params = new HashMap<>();
        params.put("category_ids", "");
        params.put("goods_sn", "");
        params.put("shop_id", shopId);
        params.put("page", String.valueOf(requestPage));
        params.put("strip", String.valueOf(SYNC_PAGE_SIZE));

        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                dbExecutor.execute(() -> {
                    try {
                        Gson gson = new Gson();
                        GrouponGoodsBean bean = gson.fromJson(response, GrouponGoodsBean.class);

                        List<GrouponGoodsBean.GrouponGoodsModel> list = (bean != null && bean.getData() != null) ? bean.getData().getGoodsList() : null;
                        GrouponGoodsBean.Pagination pagination = (bean != null && bean.getData() != null) ? bean.getData().getPagination() : null;

                        // ============ ★★★ 日志校验段 ★★★ ============
                        if (list != null && list.size() > 0) {
                            GrouponGoodsBean.GrouponGoodsModel firstItem = list.get(0);
                            Log.e("SYNC_CHECK", "---------------------------------------");
                            Log.e("SYNC_CHECK", "第 " + requestPage + " 页数据校验中...");
                            Log.e("SYNC_CHECK", "String ID (goods_id): " + firstItem.getGoods_id());
                            Log.e("SYNC_CHECK", "Int ID (pid): " + firstItem.getPid());
                            Log.e("SYNC_CHECK", "---------------------------------------");
                        }
                        // ===============================================

                        if (list != null && !list.isEmpty()) {
                            LitePal.beginTransaction();
                            try {
                                // ★★★ 核心修复：解决 9688 变 9614 的关键 ★★★
                                // 不再调用 updateAll，因为重复的 goods_id 必须存为多行
                                for (GrouponGoodsBean.GrouponGoodsModel newItem : list) {
                                    // 必须清除 LitePal 内部 ID 引用，强制作为新数据插入
                                    newItem.assignBaseObjId(0);
                                }

                                // 批量插入本页所有数据
                                LitePal.saveAll(list);
                                Log.i("SyncDebug", "第 " + requestPage + " 页成功插入数据: " + list.size() + " 条");

                                LitePal.setTransactionSuccessful();
                            } catch (Exception e) {
                                e.printStackTrace();
                                Log.e("SyncDebug", "数据库写入异常: " + e.getMessage());
                            } finally {
                                LitePal.endTransaction();
                            }

                            // 进度更新
                            if (pagination != null) {
                                int totalCount = pagination.getTotal();
                                int totalPage = pagination.getTotalpage();

                                if (totalCount > 0) {
                                    int currentCount = Math.min((requestPage * SYNC_PAGE_SIZE), totalCount);
                                    int percent = Math.min((int) (((double) currentCount / totalCount) * 100), 100);
                                    runOnUiThread(() -> {
                                        if (loadingPopup != null && !loadingPopup.isDismiss())
                                            loadingPopup.updateProgress(percent, currentCount, totalCount);
                                    });
                                }

                                if (requestPage < totalPage) {
                                    // 递归请求下一页
                                    downloadGoodsPage(requestPage + 1);
                                    return;
                                }
                            }
                        }
                        // 全部页面加载完毕
                        finishSync();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("SyncDebug", "解析异常: " + e.getMessage());
                        runOnUiThread(() -> {
                            if (loadingPopup != null) loadingPopup.dismiss();
                            Toast.makeText(MainActivity.this, "数据解析异常", LENGTH_SHORT).show();
                        });
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                Log.e("SyncDebug", "网络请求失败: " + e.getMessage());
                runOnUiThread(() -> {
                    if (loadingPopup != null) loadingPopup.dismiss();
                    Toast.makeText(MainActivity.this, "同步失败: " + e.getMessage(), LENGTH_SHORT).show();
                });
            }
        });
    }





    private void finishSync() {
        runOnUiThread(() -> {
            if (loadingPopup != null) loadingPopup.dismiss();
            is_tongbu = false;
            loadLocalGoods(0);
            //new DeleteShopPopupWindow(MainActivity.this, getString(R.string.Sync_completed), true).show();
        });
    }

    /**
     * 【优化】异步加载本地商品数据，修复分类查询逻辑
     */
    private void loadLocalGoods(int page) {
        if (page == 0) {
            ui_current_page = 0;
            grouponGoodsAdapter.hasMore = true;


            // ★ 修复：UI操作必须切回主线程
            runOnUiThread(() -> {
                if (shop_rv != null) shop_rv.scrollToPosition(0);
            });
        }

        dbExecutor.execute(() -> {
            List<GrouponGoodsBean.GrouponGoodsModel> list;

            // ▼▼▼▼▼▼ 核心修复：根据分类ID查询数据库 ▼▼▼▼▼▼
            if (TextUtils.isEmpty(current_category_id)) {
                // 1. 如果ID为空，查询全部
                list = LitePal.limit(UI_PAGE_SIZE)
                        .offset(page * UI_PAGE_SIZE)
                        .find(GrouponGoodsBean.GrouponGoodsModel.class);
            } else {
                // 2. 如果有ID，使用模糊查询 (category_ids 包含该ID)
                list = LitePal.where("category_ids like ?", "%" + current_category_id + "%")
                        .limit(UI_PAGE_SIZE)
                        .offset(page * UI_PAGE_SIZE)
                        .find(GrouponGoodsBean.GrouponGoodsModel.class);
            }
            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

            // 日志调试，方便你查看是否查到了数据
            Log.d("DB_SEARCH", "CatID: " + current_category_id + ", Result Size: " + list.size());

            runOnUiThread(() -> {
                if (isFinishing()) return;
                if (page == 0) {
                    grouponGoodsAdapter.setNewData(list);
                    if (list.size() < UI_PAGE_SIZE) grouponGoodsAdapter.hasMore = false;
                } else {
                    if (list.isEmpty()) {
                        grouponGoodsAdapter.hasMore = false;
                    } else {
                        grouponGoodsAdapter.loadMoreData(list);
                        ui_current_page = page;
                    }
                }
            });
        });
    }

    /**
     * 【优化】异步处理扫码
     */
    private void handleScanInput(String text) {

        hideLastOrderInfo();


        if (TextUtils.isEmpty(text)) return;

        String code = text.trim().replace("\r", "").replace("\n", "");
        Log.i("Scan", "开始查询商品，输入码: [" + code + "]");
        et_tiaoxingma.setText("");

        String textType = detectPaymentType(code);

        if ("unknown".equals(textType)) {
            dbExecutor.execute(() -> {
                // 1. 正常查询逻辑 (保持不变)
                List<GrouponGoodsBean.GrouponGoodsModel> results = LitePal
                        .where("sn = ? or goods_sn = ?", code, code)
                        .find(GrouponGoodsBean.GrouponGoodsModel.class);

                if (results != null && !results.isEmpty()) {
                    // 1. 【新增这一行】定义变量名并赋值
                    GrouponGoodsBean.GrouponGoodsModel testItem = results.get(0);

                    // 2. 打印日志（就是你刚才报错的那行）
                    Log.e("SCAN_ID_CHECK", "商品名称: " + testItem.getTitle() + " | 业务PID: " + testItem.getPid());

                    // 3. 原有的逻辑保持不变
                    Log.i("Scan", "查询成功: " + testItem.getTitle());
                    runOnUiThread(() -> {
                        playSuccessSound();
                        addGoodsToCart(testItem);
                    });
                } else {
                    // 2. 没查到，尝试模糊
                    List<GrouponGoodsBean.GrouponGoodsModel> fuzzyList = LitePal
                            .where("sn like ? or goods_sn like ?", "%" + code + "%", "%" + code + "%")
                            .limit(20)
                            .find(GrouponGoodsBean.GrouponGoodsModel.class);

                    runOnUiThread(() -> {
                        if (fuzzyList.isEmpty()) {
                            // ★★★ 修改：没找到商品，播放错误音效
                            playErrorSound();

                            // ★★★★★ 强力调试：批量打印前 20 条正常商品 ★★★★★
                            int totalCount = LitePal.count(GrouponGoodsBean.GrouponGoodsModel.class);
                            Log.e("ScanDebug", "【查询失败】 库内总数: " + totalCount);

                            // 查找前 20 条 不叫“无码收银” 的商品
                            List<GrouponGoodsBean.GrouponGoodsModel> samples = LitePal
                                    .where("title != ?", "无码收银")
                                    .limit(20)
                                    .find(GrouponGoodsBean.GrouponGoodsModel.class);

                            if (samples != null && !samples.isEmpty()) {
                                Log.e("ScanDebug", ">>> 批量抽查 (共 " + samples.size() + " 条) <<<");
                                for (int i = 0; i < samples.size(); i++) {
                                    GrouponGoodsBean.GrouponGoodsModel s = samples.get(i);
                                    Log.e("ScanDebug", "[" + i + "] " + s.getTitle()
                                            + " | sn=[" + s.getSn() + "]"
                                            + " | goods_sn=[" + s.getGoods_sn() + "]");
                                }
                                Log.e("ScanDebug", ">>> 抽查结束 <<<");
                            } else {
                                Log.e("ScanDebug", "太离谱了，查不到任何非'无码收银'的商品！");
                            }

                            new DeleteShopPopupWindow(MainActivity.this, getString(R.string.product_not_found_in_inventory), true).show();
                        } else if (fuzzyList.size() == 1) {
                            // ★★★ 修改：模糊找到一个，播放成功音效
                            playSuccessSound();
                            addGoodsToCart(fuzzyList.get(0));
                        } else {
                            // ★★★ 修改：模糊找到多个，也算成功
                            playSuccessSound();
                            grouponGoodsAdapter.setNewData(fuzzyList);
                            grouponGoodsAdapter.hasMore = false;
                        }
                    });
                }
            });
        } else {
            // ★★★ 修改：扫描支付码，播放成功音效
            playSuccessSound();
            processPayment(code, textType);
        }
    }




    // ----------------- 购物车逻辑 -----------------

    private void addGoodsToCart(GrouponGoodsBean.GrouponGoodsModel goods) {


        // 【新增拦截逻辑】
        // 如果商品 subtitle 是数字，说明它是系统自动发放的赠品，禁止手动/扫码加入
        if (goods.getSubtitle() != null && goods.getSubtitle().matches("\\d+")) {
            // 播放失败音效并提示
            playErrorSound();
            Toast.makeText(this, "该商品为系统满赠礼品，不可手动加购", Toast.LENGTH_SHORT).show();
            return;
        }



        hideLastOrderInfo();


        allNum++;
        for (int i = 0; i < selectedShopList.size(); i++) {
            GrouponGoodsBean.GrouponGoodsModel model = selectedShopList.get(i);
            if (model.getPid() == goods.getPid() && model.getGgspid() == goods.getGgspid()){
                model.setShuliang(model.getShuliang() + 1);
                updateCartItemPrice(model);
                selectedShopAdapter.notifyItemChanged(i);
                updateCartSummary();
                return;
            }
        }

        GrouponGoodsBean.GrouponGoodsModel newGoods = SerializableUtils.deepCopy(goods);
        newGoods.setShuliang(1);
        if (!TextUtils.isEmpty(memben_discount)) {
            newGoods.setDiscount(memben_discount);
        }
        updateCartItemPrice(newGoods);
        selectedShopList.add(0, newGoods);
        have_paid_view.setVisibility(GONE);
        selectedShopAdapter.setNewData(selectedShopList);
        selected_LinearLayoutManager.scrollToPosition(0);
        updateCartSummary();
    }

    private void handleCartItemAction(int viewId, int position) {


        hideLastOrderInfo();

        if (position < 0 || position >= selectedShopList.size()) return;
        GrouponGoodsBean.GrouponGoodsModel model = selectedShopAdapter.getData().get(position);

        if (viewId == R.id.shuliang_jian) {
            allNum--;
            if (model.getShuliang() > 1) {
                model.setShuliang(model.getShuliang() - 1);
                updateCartItemPrice(model);
                selectedShopAdapter.notifyItemChanged(position);
            } else {
                showDeleteConfirm(position);
                return;
            }
        } else if (viewId == R.id.shuliang_jia) {
            allNum++;
            model.setShuliang(model.getShuliang() + 1);
            updateCartItemPrice(model);
            selectedShopAdapter.notifyItemChanged(position);
        } else if (viewId == R.id.zengsong) {
            model.setIs_zengsong(!model.isIs_zengsong());
            if (model.isIs_zengsong()) {
                model.setDiscount("0");
                model.setDiscounted_price(new BigDecimal(model.getPrice()).multiply(new BigDecimal(model.getShuliang())));
                model.setHeji(BigDecimal.ZERO);
            } else {
                model.setDiscount("100");
                model.setDiscounted_price(BigDecimal.ZERO);
                updateCartItemPrice(model);
            }
            selectedShopAdapter.notifyItemChanged(position);
        }
        updateCartSummary();
    }

    private void updateCartItemPrice(GrouponGoodsBean.GrouponGoodsModel model) {
        BigDecimal price = new BigDecimal(model.getPrice());
        BigDecimal totalRaw = price.multiply(new BigDecimal(model.getShuliang()));

        if (!TextUtils.isEmpty(model.getDiscount()) && !model.isIs_zengsong()) {
            BigDecimal discountRate = new BigDecimal(model.getDiscount()).divide(new BigDecimal(100));
            BigDecimal finalPrice = totalRaw.multiply(discountRate);
            model.setDiscounted_price(totalRaw.subtract(finalPrice));
            model.setHeji(finalPrice.setScale(2, RoundingMode.UP));
        } else if (model.isIs_zengsong()) {
            model.setHeji(BigDecimal.ZERO);
        } else {
            model.setHeji(totalRaw.setScale(2, RoundingMode.UP));
        }
    }

    private void updateCartSummary() {
        // 1. 运行赠品核算逻辑（它会修改 selectedShopList）
        autoProcessGifts();

        zongjia = BigDecimal.ZERO;
        int realAllNum = 0;

        // 2. 重新统计总价和总件数
        for (GrouponGoodsBean.GrouponGoodsModel model : selectedShopList) {
            zongjia = zongjia.add(model.getHeji());
            realAllNum += model.getShuliang();
        }

        // 3. 更新 UI
        this.allNum = realAllNum;
        zongjia = zongjia.setScale(2, RoundingMode.UP);
        tv_zongjia.setText(zongjia.toString());
        tv_zongjian.setText(String.valueOf(allNum));

        // 【重要：必须刷新适配器】
        if (selectedShopAdapter != null) {
            // 使用 setNewData 或 notifyDataSetChanged 确保列表刷新
            selectedShopAdapter.notifyDataSetChanged();
        }

        // 更新副屏显示
        MyPresentation.setShopArrayList(selectedShopList, allNum);
        MyPresentation.setZongjia(zongjia.toString());

        // 控制空购物车显示逻辑...
        if (selectedShopList == null || selectedShopList.isEmpty()) {
            if (tv_empty_cart != null) tv_empty_cart.setVisibility(View.VISIBLE);
            if (selected_shop_rv != null) selected_shop_rv.setVisibility(View.GONE);
        } else {
            if (tv_empty_cart != null) tv_empty_cart.setVisibility(View.GONE);
            if (selected_shop_rv != null) selected_shop_rv.setVisibility(View.VISIBLE);
        }
    }






    private void showDeleteConfirm(int position) {
        deleteShopPopupWindow = new DeleteShopPopupWindow(MainActivity.this, text -> {
            if (position >= 0 && position < selectedShopList.size()) {
                selectedShopList.remove(position);
                selectedShopAdapter.setNewData(selectedShopList);
                updateCartSummary();
            }
        });
        deleteShopPopupWindow.show();
    }

    // ----------------- 点击事件 -----------------

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.delete_shop) {
            if (selectedShopIndex != null && selectedShopIndex < selectedShopList.size()) showDeleteConfirm(selectedShopIndex);
            else if (selectedShopAdapter.getItemCount() > 0) new DeleteShopPopupWindow(this, getString(R.string.please_select_product)).show();
        } else if (id == R.id.qingkong_btn) {
            if (!selectedShopList.isEmpty()) {
                selectedShopList.clear();
                selectedShopAdapter.notifyDataSetChanged();
                allNum = 0;
                updateCartSummary();
            }
        } else if (id == R.id.guadan_btn) handleGuadan();
        else if (id == R.id.qudan_btn) handleQudan();
        else if (id == R.id.dazhe_one_btn) {
            if (selectedShopIndex != null) showDiscountPopup(false);
            else if (selectedShopAdapter.getItemCount() > 0) new DeleteShopPopupWindow(this, getString(R.string.please_select_product)).show();
        } else if (id == R.id.dazhe_all_btn) {
            if (selectedShopAdapter.getItemCount() > 0) showDiscountPopup(true);
        } else if (id == R.id.checkout_btn) {
            if (selectedShopAdapter.getItemCount() > 0 && !Utilis.isFastClick()) handleCheckout();
        } else if (id == R.id.huiyuan_btn) new MemberPopupWindow(this, this::handleMemberSelect).show();
        else if (id == R.id.shanchuhuiyuan_btn) resetMember();
        else if (id == R.id.daying_btn) getLastOder();
        else if (id == R.id.more_function_btn) showMoreFunction();
        else if (id == R.id.add_no_code_btn) new AddNoCodePopupWindow(this, this::addGoodsToCart).show();
    }

    // ----------------- 业务逻辑 (挂单/取单/会员/打折) -----------------

    private void handleGuadan() {
        if (selectedShopAdapter.getItemCount() <= 0) return;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        RegistrationShopBean bean = new RegistrationShopBean();
        bean.setTime(sdf.format(System.currentTimeMillis()));
        bean.setTotal_price(zongjia);
        bean.setAllNum(allNum);
        bean.setRegistrationShopList(SerializableUtils.deepCopyList(selectedShopList));
        registrationShopBeanArrayList.add(bean);
        qudan_btn.setText(getString(R.string.qudan_num, String.valueOf(registrationShopBeanArrayList.size())));
        selectedShopList.clear();
        selectedShopAdapter.setNewData(selectedShopList);
        allNum = 0;
        updateCartSummary();
    }

    private void handleQudan() {


        hideLastOrderInfo();

        if (!registrationShopBeanArrayList.isEmpty()) {
            getRegistrationShopPopupWindow = new GetRegistrationShopPopupWindow(this, registrationShopBeanArrayList, (index, type) -> {
                if (type == 1) { // 取单
                    RegistrationShopBean bean = registrationShopBeanArrayList.get(index);
                    selectedShopList = bean.getRegistrationShopList();
                    allNum = bean.getAllNum();
                    selectedShopAdapter.setNewData(selectedShopList);
                    updateCartSummary();
                } else if (type == 2 && !registrationShopBeanArrayList.isEmpty()) {
                    getRegistrationShopPopupWindow.setDataDelect();
                }
                registrationShopBeanArrayList.remove(index);
                qudan_btn.setText(registrationShopBeanArrayList.isEmpty() ? getString(R.string.qudan_null) : getString(R.string.qudan_num, String.valueOf(registrationShopBeanArrayList.size())));
            });
            getRegistrationShopPopupWindow.show();
        }
    }

    private void showDiscountPopup(boolean isAll) {
        new DiscountPopupWindow(this, isAll ? getString(R.string.order_discount) : getString(R.string.item_discount), discount -> {
            if (isAll) {
                for (GrouponGoodsBean.GrouponGoodsModel model : selectedShopList) {
                    model.setDiscount(discount);
                    updateCartItemPrice(model);
                }
                selectedShopAdapter.notifyDataSetChanged();
            } else {
                if(selectedShopIndex != null && selectedShopIndex < selectedShopList.size()) {
                    GrouponGoodsBean.GrouponGoodsModel model = selectedShopAdapter.getData().get(selectedShopIndex);
                    model.setDiscount(discount);
                    updateCartItemPrice(model);
                    selectedShopAdapter.notifyItemChanged(selectedShopIndex);
                }
            }
            updateCartSummary();
        }).show();
    }

    private void handleMemberSelect(MemberBean memberBean) {
        huiyuan_name.setText(memberBean.getNickname());
        int vipLevel = memberBean.getVip();
        memben_discount = (vipLevel == 17) ? "98" : (vipLevel == 18) ? "95" : (vipLevel == 19) ? "90" : "100";
        applyMemberDiscount();
    }

    private void resetMember() {
        huiyuan_name.setText(getString(R.string.member_nickname));
        memben_discount = "100";
        applyMemberDiscount();
    }

    private void applyMemberDiscount() {
        if (selectedShopAdapter.getItemCount() <= 0) return;
        for (GrouponGoodsBean.GrouponGoodsModel model : selectedShopList) {
            model.setDiscount(memben_discount);
            updateCartItemPrice(model);
        }
        selectedShopAdapter.notifyDataSetChanged();
        updateCartSummary();
    }

    // ----------------- 更多功能 -----------------

    private void showMoreFunction() {
        new MorefunctionPopupWindow(this, btnType -> {
            switch (btnType) {
                case 1: syncGoodsData(); break;
                case 2: new PrintLabelsPopupWindow(this).show(); break;
                case 3: new GoodsWarehousingPopupWindow(this, null, (code, msg) ->
                        new DeleteShopPopupWindow(this, code == 1 ? getString(R.string.product_stocked_successfully) : msg, true).show()
                ).show(); break;
                case 4: new HistoryOrderPopupWindow(this).show(); break;
                case 5:
                    UserUtils.getInstance().setCategoryListBeanJson(this, "");
                    UserUtils.getInstance().setLoginBase(this, null);
                    startActivity(new Intent(this, LoginActivity.class));
                    break;
                case 6: new MoneyBoxPopupWindow(this).show(); break;
                case 7: new RelieveShiftPopupWindow(this).show(); break;
                case 8: new PrintDevicePopupWindow(this, 1, usbDevice -> setupPrinter(usbDevice, false)).show(); break;
                case 9: new PrintDevicePopupWindow(this, 2, usbDevice -> setupPrinter(usbDevice, true)).show(); break;
                case 10:
                    UserUtils.getInstance().setDazhe(this, !UserUtils.getInstance().isDazhe());
                    updateDiscountView();
                    break;
                case 11: new PaymentListPopupWindow(this).show(); break;
                case 12:
                    UserUtils.getInstance().setDianji(this, !UserUtils.getInstance().isDianji());
                    shop_mocheng.setVisibility(!UserUtils.getInstance().isDianji() ? VISIBLE : GONE);
                    break;


                // ★ 新增：处理小票样式设置弹窗
                case 13:
                    new ReceiptDiyPopupWindow(this).show();
                    break;




            }
        }).show();
    }

    private void setupPrinter(UsbDevice usbDevice, boolean isLabel) {
        if (isLabel) {
            UserUtils.getInstance().setLABEKS_VENDOR_ID(this, usbDevice.getVendorId());
            UserUtils.getInstance().setLABEKS_PRODUCT_ID(this, usbDevice.getProductId());
        } else {
            UserUtils.getInstance().setVENDOR_ID(this, usbDevice.getVendorId());
            UserUtils.getInstance().setPRODUCT_ID(this, usbDevice.getProductId());
        }
        MyUsbDeviceHelper.getInstance().requestUsbPermission(usbDevice);
        new DeleteShopPopupWindow(this, getString(R.string.setup_completed), true).show();
    }

    private void updateDiscountView() {
        boolean show = UserUtils.getInstance().isDazhe();
        dazhe_all_btn.setVisibility(show ? VISIBLE : GONE);
        dazhe_one_btn.setVisibility(show ? VISIBLE : GONE);
        caozuo_view.setVisibility(show ? VISIBLE : GONE);
    }

    // ----------------- 支付逻辑 -----------------

    private void handleCheckout() {
        // 1. 防重复弹窗检查
        if (currentCheckoutPopup != null && currentCheckoutPopup.isShowing()) {
            currentCheckoutPopup.dismiss();
        }

        checkoutBean = new CheckoutBean();
        checkoutBean.setAllNum(allNum);

        // 获取用户登录信息
        if (UserUtils.getInstance().getLoginBase() != null && UserUtils.getInstance().getLoginBase().getData() != null) {
            checkoutBean.setUser_id(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getUserId());
        }
        checkoutBean.setMachineNumber("001");

        // 设置收银员名字
        String nickname = "管理员";
        try {
            if (UserUtils.getInstance().getLoginBase() != null) {
                nickname = UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
            }
        } catch (Exception e) {}
        checkoutBean.setCashierName(nickname);

        checkoutBean.setTotal_fee(zongjia.toString());
        checkoutBean.setPay_type((is_kuangjie || !NetworkUtils.getInstance().isNetworkConnected(this)) ? "cash" : "");
        checkoutBean.setShop_id(UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        is_kuangjie = false;

        // 2. 定义变量：优惠总额
        BigDecimal discount_fee = BigDecimal.ZERO;
        ArrayList<CheckoutBean.GoodsJsonBean> goodsJsonList = new ArrayList<>();

        // 3. 循环构建商品数据 (核心修改：增加赠品处理逻辑)
        for (GrouponGoodsBean.GrouponGoodsModel model : selectedShopList) {

            // 统计普通商品的优惠金额
            if (model.getDiscounted_price() != null) {
                discount_fee = discount_fee.add(model.getDiscounted_price());
            }

            CheckoutBean.GoodsJsonBean jsonBean = new CheckoutBean.GoodsJsonBean();
            jsonBean.setGoods_id(model.getPid());

            // --- 赠品特殊处理开始 ---
            if (model.isIs_zengsong()) {
                // 标题增加[赠品]前缀，打印和小票预览都会显示
                jsonBean.setTitle("[赠品]" + (model.getTitle() != null ? model.getTitle() : "未知商品"));
                // 赠品价格强制设为 0
                jsonBean.setGoods_price("0.00");
                jsonBean.setPay_price("0.00");
                jsonBean.setDiscount("0");
                jsonBean.setDiscounted_price("0.00");
            } else {
                // 普通商品逻辑
                jsonBean.setTitle(model.getTitle() != null ? model.getTitle() : "未知商品");
                jsonBean.setGoods_price(model.getPrice());
                jsonBean.setPay_price(model.getHeji() != null ? model.getHeji().toString() : "0.00");
                jsonBean.setDiscount(TextUtils.isEmpty(model.getDiscount()) ? "100" : model.getDiscount());
                jsonBean.setDiscounted_price(model.getDiscounted_price() == null ? "0.00" : model.getDiscounted_price().toString());
            }
            // --- 赠品特殊处理结束 ---

            jsonBean.setGoods_num(model.getShuliang());
            jsonBean.setGoods_sn(model.getGoods_sn() != null ? model.getGoods_sn() : "");
            jsonBean.setSn(model.getSn() != null ? model.getSn() : "");
            jsonBean.setGoods_sku_price_id(String.valueOf(model.getGgspid()));

            // 修复 goods_sku_text 为空导致 500 错误的问题
//            String safeSkuText = model.getGoods_sku_text();
//            if (TextUtils.isEmpty(safeSkuText)) {
//                safeSkuText = "[\"商品\"]";
//            }
//            jsonBean.setGoods_sku_text(safeSkuText);


            // 根据后端要求，直接传空字符串 20260323修改
            jsonBean.setGoods_sku_text("");

            goodsJsonList.add(jsonBean);
        }

        checkoutBean.setGoodsjson(new Gson().toJson(goodsJsonList));

        // 4. 设置金额
        checkoutBean.setDiscount_fee(discount_fee.toString());
        checkoutBean.setTotal_amount(zongjia.add(discount_fee).toString());
        checkoutBean.setGoods_original_amount(zongjia.add(discount_fee).toString());

        // 5. 创建结账弹窗
        currentCheckoutPopup = new CheckoutPopupWindow(this, checkoutBean, () -> {
            have_paid_view.setVisibility(VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> have_paid_view.setVisibility(GONE), 3000);

            // 结账完成清空购物车
            is_jiezhang_qingkong = true;
            onClick(qingkong_btn);

            // ▼▼▼▼▼▼ 【插入显示逻辑：上一单详情回显】 ▼▼▼▼▼▼
            String pType = checkoutBean.getPay_type();
            if (TextUtils.isEmpty(pType)) pType = "cash";

            int cCount = checkoutBean.getAllNum();
            String cTotal = checkoutBean.getTotal_amount();
            String cDiscount = checkoutBean.getDiscount_fee();
            if (TextUtils.isEmpty(cDiscount)) cDiscount = "0.00";

            String cRealPay = checkoutBean.getPay_fee();
            if (TextUtils.isEmpty(cRealPay)) cRealPay = checkoutBean.getTotal_fee();

            final String fPType = pType;
            final String fRealPay = cRealPay;
            final String fDiscount = cDiscount;

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showLastOrderInfo(fPType, cCount, cTotal, fDiscount, fRealPay);
            }, 200);
            // ▲▲▲▲▲▲ 【插入结束】 ▲▲▲▲▲▲
        });
        currentCheckoutPopup.show();
    }





    private void processPayment(String authCode, String payType) {
        if (selectedShopAdapter.getItemCount() <= 0) return;

        // 1. 必须先调用这个，弹出窗口并初始化 checkoutBean
        handleCheckout();

        // 2. 然后再设置扫码特有的属性
        // 注意：因为 handleCheckout 重新 new 了一个 checkoutBean，
        // 所以必须在 handleCheckout 之后再 setPay_type
        checkoutBean.setPay_type(payType);
        checkoutBean.setAuthCode(authCode);

        // 3. 这些金额设置可能在 handleCheckout 里已经设过了，但这里覆盖一下也没错
        checkoutBean.setPay_fee(zongjia.toString());
        checkoutBean.setCash_price(zongjia.toString());
        checkoutBean.setCash_change("0.00");
        checkoutBean.setType(1);
        checkoutBean.setOrder_status(2);

        // 4. 提交支付
        SubmitCheckout(checkoutBean);
    }

    public void SubmitCheckout(CheckoutBean checkoutBean) {
        if (Utilis.isFastClick()) return;

        // 显示加载框
        if (buildBean != null) {
            buildBean.show();
        }

        String url = POSApiSerview.POS_URL + POSApiSerview.addOrder;

        // 日志方便调试
        Log.i("PayDebug", "开始请求支付接口: " + url);

        // 1. 准备请求体
        Gson gson = new Gson();
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), gson.toJson(checkoutBean));

        // 2. 准备请求对象 (带Token)
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
        if (UserUtils.getInstance().getLoginBase() != null
                && UserUtils.getInstance().getLoginBase().getData() != null
                && UserUtils.getInstance().getLoginBase().getData().getUserinfo() != null) {
            builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        }
        builder.post(body);

        // 3. ★★★ 核心加固：创建专属的 OkHttpClient，加入自动重试 ★★★
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                // 连接超时设为 5秒 (网络彻底断开时快速反馈，别让用户等30秒)
                .connectTimeout(5, java.util.concurrent.TimeUnit.SECONDS)
                // 读取超时设为 20秒 (给服务器处理支付留足时间)
                .readTimeout(20, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(20, java.util.concurrent.TimeUnit.SECONDS)

                // ★★★ 重点：添加自动重试拦截器 ★★★
                .addInterceptor(new okhttp3.Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        okhttp3.Request request = chain.request();
                        okhttp3.Response response = null;
                        boolean responseOK = false;
                        int tryCount = 0;
                        int maxLimit = 3; // 最大重试次数：3次

                        while (!responseOK && tryCount < maxLimit) {
                            try {
                                if (tryCount > 0) {
                                    Log.w("PayDebug", "网络请求不稳定，正在进行第 " + tryCount + " 次自动重试...");
                                }
                                response = chain.proceed(request);
                                responseOK = response.isSuccessful();
                            } catch (Exception e) {
                                Log.e("PayDebug", "第 " + tryCount + " 次请求异常: " + e.getMessage());
                                // 如果是最后一次尝试依然失败，则抛出异常，交给 onFailure 处理
                                if (tryCount >= maxLimit - 1) throw e;
                            } finally {
                                // 如果这一把失败了，但还有重试机会，必须关闭上一把的 response 避免内存泄漏
                                if (!responseOK && response != null) {
                                    response.close();
                                }
                            }
                            tryCount++;
                        }

                        if (response == null) {
                            throw new IOException("自动重试 " + maxLimit + " 次后依然失败");
                        }
                        return response;
                    }
                })
                .build();

        // 4. 执行请求
        client.newCall(builder.build()).enqueue(new okhttp3.Callback() {


            @Override
            public void onFailure(@androidx.annotation.NonNull okhttp3.Call call, @androidx.annotation.NonNull IOException e) {
                runOnUiThread(() -> {
                    // 日志记录
                    Log.e("PayError", "支付请求重试3次后依然失败: " + e.getMessage());

                    // ▼▼▼▼▼▼ 关键修改 ▼▼▼▼▼▼
                    // 以前是直接弹窗报错，现在改为去反查
                    // 只有当反查也失败时，才会在 verifyPaymentResult 内部弹窗
                    verifyPaymentResult(checkoutBean);
                    // ▲▲▲▲▲▲ 修改结束 ▲▲▲▲▲▲
                });
            }






            @Override
            public void onResponse(@androidx.annotation.NonNull okhttp3.Call call, @androidx.annotation.NonNull okhttp3.Response response) throws IOException {
                final String responseStr = response.body().string();
                runOnUiThread(() -> {
                    try {
                        // 无论服务器返回 200 还是 500，只要有回包，都尝试解析
                        if (response.isSuccessful()) {
                            JSONObject jsonObject = new JSONObject(responseStr);
                            String msg = jsonObject.optString("msg");

                            if (msg.contains("成功") || msg.contains("Success")) {
                                handlePaymentSuccess(jsonObject);
                            } else if (msg.contains("密码") || msg.contains("process")) {
                                handlePaymentProcess(jsonObject);
                            } else {
                                DialogUIUtils.dismiss(buildBean);
                                new DeleteShopPopupWindow(MainActivity.this, getString(R.string.Payment_failed) + msg, true).show();
                            }
                        } else {
                            DialogUIUtils.dismiss(buildBean);
                            new DeleteShopPopupWindow(MainActivity.this, "服务器异常 Code: " + response.code(), true).show();
                        }
                    } catch (Exception e) {
                        DialogUIUtils.dismiss(buildBean);
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "数据解析异常", LENGTH_SHORT).show();
                    }
                });
            }
        });
    }



    /**
     * ★★★ 核心加固：支付异常时的反查确认机制 (防止掉单) ★★★
     * 当 SubmitCheckout 彻底超时时调用此方法
     */
    private void verifyPaymentResult(CheckoutBean originalBean) {
        Log.w("PayDebug", "支付请求无响应，正在反查服务器最近一笔订单进行核对...");

        Map<String, String> params = new HashMap<>();
        // 获取店铺ID
        String shopId = "";
        if (UserUtils.getInstance().getShopDataBean() != null && !UserUtils.getInstance().getShopDataBean().getData().isEmpty()) {
            shopId = UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid();
        }
        params.put("shop_id", shopId);

        // 使用 getLastOder 接口查询最后一笔订单
        String url = POSApiSerview.POS_URL + POSApiSerview.getLastOder;

        // 这里使用简单的请求，不需重试，因为如果这里也通不过，说明网络彻底断了
        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        // code=1 表示查询成功
                        if (jsonObject.optInt("code") == 1) {
                            String dataStr = jsonObject.optString("data");
                            if (!TextUtils.isEmpty(dataStr) && !dataStr.equals("[]")) {
                                ArrayList<LastOrderBean> list = new Gson().fromJson(dataStr, new TypeToken<ArrayList<LastOrderBean>>() {}.getType());

                                if (list != null && !list.isEmpty()) {
                                    LastOrderBean lastOrder = list.get(0);

                                    // =========== ★★★ 核心比对逻辑 ★★★ ===========

                                    // 1. 比对金额 (使用BigDecimal避免精度问题)
                                    // 注意：LastOrderBean里通常用 pay_fee 或 total_fee，这里取 pay_fee
                                    String serverPriceStr = lastOrder.getPay_fee();
                                    if (TextUtils.isEmpty(serverPriceStr)) serverPriceStr = lastOrder.getTotal_fee();

                                    String localPriceStr = originalBean.getPay_fee();
                                    if (TextUtils.isEmpty(localPriceStr)) localPriceStr = originalBean.getTotal_fee();

                                    BigDecimal serverPrice = new BigDecimal(TextUtils.isEmpty(serverPriceStr) ? "0" : serverPriceStr);
                                    BigDecimal localPrice = new BigDecimal(TextUtils.isEmpty(localPriceStr) ? "0" : localPriceStr);

                                    boolean isAmountMatch = serverPrice.compareTo(localPrice) == 0;

                                    // 2. 比对时间 (服务器createtime通常是秒，系统时间是毫秒)
                                    // 我们允许 90秒 的误差，只要是刚才生成的单子就算
                                    long serverTime = lastOrder.getCreatetime() * 1000L;
                                    long currentTime = System.currentTimeMillis();
                                    boolean isTimeRecent = Math.abs(currentTime - serverTime) < 90000; // 90秒内

                                    // 3. 判定结果
                                    if (isAmountMatch && isTimeRecent) {
                                        Log.i("PayDebug", "【反查成功】发现掉单！自动恢复。单号：" + lastOrder.getOrder_sn());

                                        // 修正本地 Bean 的订单号
                                        originalBean.setOrder_sn(lastOrder.getOrder_sn());
                                        originalBean.setTransaction_id(lastOrder.getTransaction_id());

                                        // ★ 手动触发成功逻辑 (模拟一个成功的 JSON 返回给 handlePaymentSuccess)
                                        // 这样可以复用你现有的打印、清空购物车、语音逻辑
                                        JSONObject mockSuccessJson = new JSONObject();
                                        mockSuccessJson.put("msg", "反查恢复成功");
                                        mockSuccessJson.put("data", lastOrder.getOrder_sn());
                                        // 有些逻辑可能从 code 对象取值，根据你的 handlePaymentSuccess 逻辑适配
                                        JSONObject codeObj = new JSONObject();
                                        codeObj.put("order_sn", lastOrder.getOrder_sn());
                                        codeObj.put("transaction_id", lastOrder.getTransaction_id());
                                        mockSuccessJson.put("code", codeObj); // 注意这里结构要凑一下，虽然后面可能不一定全用到

                                        handlePaymentSuccess(mockSuccessJson);
                                        return;
                                    } else {
                                        Log.w("PayDebug", "【反查失败】最近一单金额或时间不匹配。ServerTime:" + serverTime + " LocalTime:" + currentTime);
                                    }
                                }
                            }
                        }

                        // 如果代码走到这里，说明服务器没有刚才那笔单子 -> 确实支付失败了
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected) + "\n(请求超时，未生成订单)", true).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        // 解析异常，不敢乱报成功，提示网络错误
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(MainActivity.this, "验证支付状态异常", true).show();
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {
                    // ★★★ 最坏情况：连反查接口都连不上（网彻底断了） ★★★
                    // 必须弹窗警告收银员人工确认
                    showAmbiguousStatusDialog();
                });
            }
        });
    }

    /**
     * 显示“状态不确定”的红色警告弹窗 (人工介入)
     */
    private void showAmbiguousStatusDialog() {
        DialogUIUtils.dismiss(buildBean);
        // 这里建议传 true (isError)，显示红色警告
        new DeleteShopPopupWindow(MainActivity.this, true,
                "⚠️ 严重网络故障 ⚠️\n\n系统【无法确认】顾客是否已扣款。\n\n请务必查看【顾客手机】！\n如果已扣款，请勿重复扫码！",
                text -> {
                    // 点击确定后的操作，通常什么都不做，让收银员自己决定
                }).show();
    }






    // 替换 MainActivity.java 中的 handlePaymentSuccess 方法
    private void handlePaymentSuccess(JSONObject jsonObject) {


        // --- 确保这段代码在最前面 ---
        if (currentCheckoutPopup != null && currentCheckoutPopup.isShowing()) {
            currentCheckoutPopup.dismiss();
        }


        order_sn = ""; // 先重置

        if (jsonObject != null) {
            // 1. 智能解析 data 字段
            Object dataObj = jsonObject.opt("data");

            if (dataObj instanceof JSONArray) {
                // 情况A：data 是数组 (对应你日志里的情况)
                JSONArray arr = (JSONArray) dataObj;
                if (arr.length() > 0) {
                    JSONObject item = arr.optJSONObject(0);
                    if (item != null) {
                        // 优先取 order_sn，如果没有则取 sn
                        order_sn = item.optString("order_sn");
                        if (TextUtils.isEmpty(order_sn)) {
                            order_sn = item.optString("sn");
                        }
                    }
                }
            } else if (dataObj instanceof JSONObject) {
                // 情况B：data 是对象
                JSONObject item = (JSONObject) dataObj;
                order_sn = item.optString("order_sn");
                if (TextUtils.isEmpty(order_sn)) {
                    order_sn = item.optString("sn");
                }
            } else if (dataObj instanceof String) {
                // 情况C：data 是字符串 (可能是支付宝的情况)
                String dataStr = (String) dataObj;
                // 简单判断是否是 JSON 格式的字符串，如果不是才直接用
                if (!dataStr.startsWith("[") && !dataStr.startsWith("{")) {
                    order_sn = dataStr;
                }
            }

            // 2. 微信支付的兜底逻辑 (保留你原有的逻辑，防止结构变化)
            if (TextUtils.isEmpty(order_sn) && "wechat".equals(checkoutBean.getPay_type())) {
                JSONObject codeObj = jsonObject.optJSONObject("code");
                if (codeObj != null) {
                    order_sn = codeObj.optString("order_sn");
                }
            }

            // 3. 支付宝的兜底逻辑
            if (TextUtils.isEmpty(order_sn) && "alipay".equals(checkoutBean.getPay_type())) {
                order_sn = jsonObject.optString("order_sn");
            }
        }

        // 4. 最终保底：如果都没拿到，使用本地生成的单号
        if (TextUtils.isEmpty(order_sn)) {
            order_sn = TextUtils.isEmpty(checkoutBean.getOrder_sn()) ? "LOC" + System.currentTimeMillis() : checkoutBean.getOrder_sn();
        }

        // 更新 Bean 中的单号，确保打印时使用的是解析出来的正确单号
        checkoutBean.setOrder_sn(order_sn);

        DialogUIUtils.dismiss(buildBean);
        have_paid_view.setVisibility(VISIBLE);
        new Handler(Looper.getMainLooper()).postDelayed(() -> have_paid_view.setVisibility(GONE), 3000);

        // 执行打印和后续操作
        operateDetails(checkoutBean);

        is_jiezhang_qingkong = true;
        onClick(qingkong_btn);


        // ▼▼▼▼▼▼ 【插入显示逻辑】 ▼▼▼▼▼▼
        final String payType = checkoutBean.getPay_type();
        final int count = checkoutBean.getAllNum();
        final String total = checkoutBean.getTotal_amount();

        // 获取优惠金额 (优先读 Bean，读不到算一下)
        String discount = checkoutBean.getDiscount_fee();
        if (TextUtils.isEmpty(discount)) {
            BigDecimal original = new BigDecimal(TextUtils.isEmpty(checkoutBean.getGoods_original_amount()) ? "0" : checkoutBean.getGoods_original_amount());
            BigDecimal paid = new BigDecimal(TextUtils.isEmpty(checkoutBean.getPay_fee()) ? "0" : checkoutBean.getPay_fee());
            discount = original.subtract(paid).setScale(2, RoundingMode.HALF_UP).toString();
        }
        final String finalDiscount = discount;

        // 获取实付
        String rPay = checkoutBean.getPay_fee();
        if (TextUtils.isEmpty(rPay)) rPay = checkoutBean.getTotal_fee();
        final String realPay = rPay;

        // 延迟 200ms 显示，避开清空后的刷新
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            showLastOrderInfo(payType, count, total, finalDiscount, realPay);
        }, 200);
        // ▲▲▲▲▲▲ 【插入结束】 ▲▲▲▲▲▲





        new DeleteShopPopupWindow(this, getString(R.string.Payment_succeeded), true).show();
    }







    private void handlePaymentProcess(JSONObject jsonObject) throws JSONException {
        if ("wechat".equals(checkoutBean.getPay_type())) {
            order_sn = new JSONObject(jsonObject.getString("code")).optString("order_sn");
            if (new JSONObject(jsonObject.getString("code")).has("out_trade_no")) {
                out_trade_no = new JSONObject(jsonObject.getString("code")).getString("out_trade_no");
                fwsgetOrderInformation(checkoutBean);
            } else {
                DialogUIUtils.dismiss(buildBean);
                new DeleteShopPopupWindow(this, getString(R.string.No_transaction_ID_recorded), true).show();
            }
        } else if ("alipay".equals(checkoutBean.getPay_type())) {
            out_trade_no = jsonObject.optString("out_trade_no");
            order_sn = jsonObject.optString("order_sn");
            queryOrder(checkoutBean);
            timeCount.start();
        }
    }

    // ----------------- 分类与查询 -----------------

    public void OverviewList() {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        OkHttpUtil.postFormAsync(POSApiSerview.POS_URL + POSApiSerview.getGrouponCategory, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    if (!TextUtils.isEmpty(response)) {
                        Gson gson = new Gson();
                        CategoryListBean bean = gson.fromJson(response, CategoryListBean.class);
                        if (bean.getCode() == 1) {
                            ArrayList<CategoryListBean.CategoryListModel> models = bean.getData();

                            // ▼▼▼▼▼▼ 核心修复：强行插入“全部”选项，并确保ID为"" ▼▼▼▼▼▼
                            CategoryListBean.CategoryListModel all = new CategoryListBean.CategoryListModel();
                            all.setName(getString(R.string.all));
                            all.setId(""); // ID 为空，对应 loadLocalGoods 的查询全部逻辑
                            models.add(0, all);
                            // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

                            UserUtils.getInstance().setCategoryListBeanJson(MainActivity.this, gson.toJson(bean));
                            shopTypeAdapter.setIndex(0);
                            shopTypeAdapter.setNewData(models);

                            current_category_id = "";
                            loadLocalGoods(0);
                        }
                    }
                });
            }
            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> loadLocalCategories());
            }
        });
    }

    private void loadLocalCategories() {
        if (!TextUtils.isEmpty(UserUtils.getInstance().getCategoryListBeanJson())) {
            Gson gson = new Gson();
            CategoryListBean bean = gson.fromJson(UserUtils.getInstance().getCategoryListBeanJson(), CategoryListBean.class);
            shopTypeAdapter.setNewData(bean.getData());
        }
    }

    public static String detectPaymentType(String code) {
        if (TextUtils.isEmpty(code)) return "unknown";
        if (!code.matches("\\d+")) return "unknown";
        if (code.length() == 18 && code.matches("^(10|11|12|13|14|15)\\d{16}$")) return "wechat";
        if (code.length() >= 16 && code.length() <= 24 && code.matches("^(25|26|27|28|29|30)\\d+")) return "alipay";
        return "unknown";
    }

    private void updateColor(TextView view, int latency) {
        if (latency < 0) view.setTextColor(0xFFFF0000);
        else if (latency < 100) view.setTextColor(0xFF4CAF50);
        else if (latency < 300) view.setTextColor(0xFFFFC107);
        else view.setTextColor(0xFFF44336);
    }

    // ----------------- 退款轮询 -----------------

    public void startRefundQuery(String payType, String transactionId, String outTradeNo, String refundFee, int position) {
        Map<String, String> refundParams = new HashMap<>();
        refundParams.put("payType", payType);
        refundParams.put("transaction_id", transactionId != null ? transactionId : "");
        refundParams.put("out_trade_no", outTradeNo != null ? outTradeNo : "");
        refundParams.put("refund_fee", refundFee != null ? refundFee : "0.00");
        refundParams.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        refundParams.put("position", String.valueOf(position));

        new CountDownTimer(30000, 5000) {
            @Override
            public void onTick(long millisUntilFinished) { queryRefundStatus(refundParams); }
            @Override
            public void onFinish() {
                queryRefundStatus(refundParams);
                if (paymentStatusListener != null) runOnUiThread(() -> paymentStatusListener.onRefundCompleted(false, position));
            }
        }.start();
    }

    private void queryRefundStatus(Map<String, String> refundParams) {
        String payType = refundParams.get("payType");
        String url = POSApiSerview.POS_URL + ("alipay".equals(payType) ? POSApiSerview.queryOrder : POSApiSerview.fwsgetOrderInformation);
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", refundParams.get("shop_id"));
        if ("alipay".equals(payType)) params.put("out_trade_no", refundParams.get("out_trade_no"));
        else if ("wechat".equals(payType)) {
            params.put("transaction_id", refundParams.get("transaction_id"));
            params.put("outTradeNo", refundParams.get("out_trade_no"));
        }

        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject json = new JSONObject(response);
                    boolean isSuccess = false;
                    if ("alipay".equals(payType)) {
                        String status = json.optString("trade_status");
                        isSuccess = "REFUND_SUCCESS".equals(status) || "TRADE_CLOSED".equals(status);
                    } else if ("wechat".equals(payType)) {
                        JSONObject code = json.optJSONObject("code");
                        isSuccess = code != null && "SUCCESS".equals(code.optString("refund_status"));
                    }
                    if (isSuccess && paymentStatusListener != null) {
                        int pos = Integer.parseInt(refundParams.get("position"));
                        runOnUiThread(() -> paymentStatusListener.onRefundCompleted(true, pos));
                    }
                } catch (JSONException e) { e.printStackTrace(); }
            }
            @Override
            public void onFailure(IOException e) {}
        });
    }

    // ----------------- 生命周期与辅助 -----------------

    class TimeCount extends CountDownTimer {
        public TimeCount(long millisInFuture, long countDownInterval) { super(millisInFuture, countDownInterval); }
        @Override
        public void onTick(long millisUntilFinished) { queryOrder(checkoutBean); }
        @Override
        public void onFinish() { is_chaoshi = true; queryOrder(checkoutBean); }
    }

    private class NetworkChangeReceiver extends BroadcastReceiver {
        private final Activity activity;
        public NetworkChangeReceiver(Activity activity) { this.activity = activity; }
        @Override
        public void onReceive(Context context, Intent intent) {
            checkPendingOrders();
            wangluo_view.setVisibility(NetworkUtils.getInstance().isNetworkConnected(activity) ? GONE : VISIBLE);
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            if (keyCode == KeyEvent.KEYCODE_F2) onClick(guadan_btn);
            else if (keyCode == KeyEvent.KEYCODE_F3) onClick(qudan_btn);
            else if (keyCode == KeyEvent.KEYCODE_F4) { if (UserUtils.getInstance().isDazhe()) onClick(dazhe_one_btn); }
            else if (keyCode == KeyEvent.KEYCODE_F5) onClick(qingkong_btn);
            else if (keyCode == KeyEvent.KEYCODE_F6) { if (UserUtils.getInstance().isDazhe()) onClick(dazhe_all_btn); }
            else if (keyCode == KeyEvent.KEYCODE_F7) onClick(daying_btn);
            else if (keyCode == KeyEvent.KEYCODE_F8) onClick(checkout_btn);
            else if (keyCode == KeyEvent.KEYCODE_F9) { is_kuangjie = true; onClick(checkout_btn); }
            else if (keyCode == KeyEvent.KEYCODE_F10) onClick(huiyuan_btn);
            else if (keyCode == KeyEvent.KEYCODE_BACK) onBackPressed();
            else return super.dispatchKeyEvent(event);
            return true;
        }
        return super.dispatchKeyEvent(event);
    }




    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkChangeReceiver != null) unregisterReceiver(networkChangeReceiver);
        MyUsbDeviceHelper.getInstance().unregisterReceiver();
        if (timeCount != null) timeCount.cancel();

        // ★★★ 释放 SoundPool
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }

        dbExecutor.shutdown();
    }

    public void queryOrder(CheckoutBean checkoutBean) {
        // 构造请求参数
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());

        // 发起查询
        OkHttpUtil.postFormAsync(POSApiSerview.POS_URL + POSApiSerview.queryOrder, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String trade_status = jsonObject.optString("trade_status");

                        // 1. 支付成功
                        if (trade_status.contains("TRADE_FINISHED") || trade_status.contains("TRADE_SUCCESS")) {
                            if (timeCount != null) timeCount.cancel(); // 停止倒计时
                            is_chaoshi = false;

                            transaction_id = jsonObject.optString("trade_no");
                            checkoutBean.setTransaction_id(transaction_id);
                            checkoutBean.setOrder_sn(order_sn);

                            // ★★★ 核心修复：成功后手动触发后续流程 ★★★
                            handlePaymentSuccess(jsonObject);

                            // 如果需要推单
                            pushorders(checkoutBean);
                        }
                        // 2. 支付关闭/撤销
                        else if (trade_status.contains("TRADE_CLOSED")) {
                            if (timeCount != null) timeCount.cancel();
                            DialogUIUtils.dismiss(buildBean);
                            new DeleteShopPopupWindow(MainActivity.this, getString(R.string.order_canceled), true).show();
                        }
                        // 3. 等待支付中... (什么都不做，等待下一次轮询)
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                // ★★★ 核心修复：查询失败不弹窗报错，而是静默失败，等待下一次轮询 ★★★
                Log.e("PayDebug", "查询支付状态网络失败: " + e.getMessage());
                // 这里不要 dismiss loading，因为网络波动是暂时的，下次轮询可能就通了
            }
        });
    }




    /**
     * 微信支付轮询逻辑（移植自老代码）
     */
    /**
     * 微信支付轮询逻辑（加固版：抗网络波动）
     */
    public void fwsgetOrderInformation(CheckoutBean checkoutBean) {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");

        okhttp3.FormBody.Builder formBuilder = new okhttp3.FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();

        String url = POSApiSerview.POS_URL + POSApiSerview.fwsgetOrderInformation;
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);

        if (UserUtils.getInstance().getLoginBase() != null && UserUtils.getInstance().getLoginBase().getData() != null) {
            builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        }
        builder.post(formBody);

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        client.newCall(builder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                // ▼▼▼▼▼▼ 修改开始：网络波动时，不报错，而是延迟重试 ▼▼▼▼▼▼
                Log.w("PayDebug", "微信轮询网络失败，3秒后自动重试: " + e.getMessage());

                runOnUiThread(() -> {
                    // 只要界面还没销毁，loading 框还在显示，就继续重试
                    if (!isFinishing() && buildBean != null && buildBean.dialog != null && buildBean.dialog.isShowing()) {
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            fwsgetOrderInformation(checkoutBean);
                        }, 3000); // 3秒后重试
                    }
                });
                // ▲▲▲▲▲▲ 修改结束 ▲▲▲▲▲▲
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        JSONObject jsonObject = new JSONObject(success);

                        runOnUiThread(() -> {
                            try {
                                String trade_state_desc = "";
                                // 增加判空保护，防止JSON解析炸裂
                                if (jsonObject.has("code") && !jsonObject.isNull("code")) {
                                    JSONObject codeObj = jsonObject.optJSONObject("code");
                                    if(codeObj != null) {
                                        trade_state_desc = codeObj.optString("trade_state_desc");
                                    }
                                }

                                if (trade_state_desc.contains("密码") || trade_state_desc.contains("USERPAYING")) {
                                    // 用户正在输入密码，继续轮询
                                    // 这里也可以稍微 delay 一下，防止请求太频繁
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        fwsgetOrderInformation(checkoutBean);
                                    }, 1000);

                                } else if (trade_state_desc.contains("支付成功") || trade_state_desc.contains("SUCCESS")) {
                                    // 支付成功
                                    transaction_id = new JSONObject(jsonObject.getString("code")).getString("transaction_id");
                                    checkoutBean.setTransaction_id(transaction_id);
                                    checkoutBean.setOrder_sn(order_sn);

                                    handlePaymentSuccess(jsonObject);
                                    pushorders(checkoutBean);

                                } else if (trade_state_desc.contains("支付失败") || trade_state_desc.contains("PAYERROR")) {
                                    fwscancelanOrder(checkoutBean);

                                } else if (trade_state_desc.contains("订单已撤销") || trade_state_desc.contains("REVOKED")) {
                                    order_sn = "";
                                    out_trade_no = "";
                                    DialogUIUtils.dismiss(buildBean);
                                    new DeleteShopPopupWindow(MainActivity.this, getString(R.string.order_canceled), true).show();
                                } else {
                                    // 未知状态，继续轮询 (防止状态描述变化导致中断)
                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                        fwsgetOrderInformation(checkoutBean);
                                    }, 3000);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                // 解析失败也重试，万一服务器传回乱码
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    fwsgetOrderInformation(checkoutBean);
                                }, 3000);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("PayDebug", "Error occurred", e);
                        // 异常重试
                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                            fwsgetOrderInformation(checkoutBean);
                        }, 3000);
                    }
                } else {
                    // 服务器报错 (500/404) 也重试
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        fwsgetOrderInformation(checkoutBean);
                    }, 3000);
                }
            }
        });
    }





    /**
     * 微信支付撤销逻辑（移植自老代码，补充依赖）
     */
    public void fwscancelanOrder(CheckoutBean checkoutBean) {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");

        okhttp3.FormBody.Builder formBuilder = new okhttp3.FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();

        String url = POSApiSerview.POS_URL + POSApiSerview.fwscancelanOrder;
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);

        if (UserUtils.getInstance().getLoginBase() != null && UserUtils.getInstance().getLoginBase().getData() != null) {
            builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        }
        builder.post(formBody);

        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10000, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(10000, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        client.newCall(builder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    DialogUIUtils.dismiss(buildBean);
                    new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected), true).show();
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        runOnUiThread(() -> fwsgetOrderInformation(checkoutBean));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void getLastOder() {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        OkHttpUtil.postFormAsync(POSApiSerview.POS_URL + POSApiSerview.getLastOder, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        if (jsonObject.optInt("code") == 1) {
                            ArrayList<LastOrderBean> list = new Gson().fromJson(jsonObject.getString("data"), new TypeToken<ArrayList<LastOrderBean>>() {}.getType());
                            if (list != null && !list.isEmpty()) {
                                // ★ 核心：这里调用更新后的 asyncPrintLastOrder
                                MyPrinterHelper.getInstance().asyncPrintLastOrder(MainActivity.this, list.get(0), null);
                            }
                        }
                    } catch (JSONException e) { e.printStackTrace(); }
                });
            }
            @Override public void onFailure(IOException e) {}
        });
    }




    public void operateDetails(CheckoutBean checkoutBean) {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        OkHttpUtil.postFormAsync(POSApiSerview.POS_URL + POSApiSerview.operateDetails, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        ArrayList<PrintDataBean> list = null;
                        if (jsonObject.optInt("code") == 1) {
                            list = new Gson().fromJson(jsonObject.getString("data"), new TypeToken<ArrayList<PrintDataBean>>() {}.getType());
                        }
                        String weixin_pice = "wechat".equals(checkoutBean.getPay_type()) ? checkoutBean.getPay_fee() : "";
                        String zhifubao_pice = "alipay".equals(checkoutBean.getPay_type()) ? checkoutBean.getPay_fee() : "";
                        MyPrinterHelper.getInstance().asyncPrintCheckout(MainActivity.this, checkoutBean, list != null ? list.get(0) : null, "", weixin_pice, zhifubao_pice, order_sn);
                    } catch (JSONException e) { e.printStackTrace(); }
                });
            }
            @Override public void onFailure(IOException e) {}
        });
    }

    /**
     * 推送订单（支付成功后调用）
     */
    /**
     * 推送订单（移植老代码逻辑：使用JSON提交 + Token头）
     */
    public void pushorders(CheckoutBean checkoutBean) {
        // 使用老代码的 URL 和 JSON 逻辑
        String url = POSApiSerview.POS_URL + POSApiSerview.pushorders;
        Gson gson = new Gson();

        // 1. 构建 JSON Body
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), gson.toJson(checkoutBean));

        // 2. 构建带 Token 的请求
        okhttp3.Request.Builder builder = new okhttp3.Request.Builder().url(url);
        if (UserUtils.getInstance().getLoginBase() != null && UserUtils.getInstance().getLoginBase().getData() != null) {
            builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
        }
        builder.post(body);

        // 3. 配置 OkHttpClient (使用老代码的超时设置)
        okhttp3.OkHttpClient client = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(10000, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(10000, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(10000, java.util.concurrent.TimeUnit.SECONDS)
                .build();

        // 4. 执行请求
        client.newCall(builder.build()).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                runOnUiThread(() -> {
                    // 推单失败仅记录日志，不弹窗打断用户，因为订单实际上已经支付成功了
                    Log.e("PushOrder", "推单失败: " + e.getMessage());
                });
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        Log.i("PushOrder", "推单成功: " + success);
                        // 注意：此处移除了老代码中重复的打印、清空购物车等UI逻辑
                        // 因为新代码的 handlePaymentSuccess 已经处理了这些界面操作
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }


    /**
     * 【新增】根据关键词搜索本地商品库并实时更新 UI
     */
    private void searchLocalGoods(String keyword) {
        dbExecutor.execute(() -> {
            // 1. 如果关键词为空，安全地调用 loadLocalGoods
            if (TextUtils.isEmpty(keyword)) {
                runOnUiThread(() -> loadLocalGoods(0));
                return;
            }

            // 2. 数据库模糊查询
            List<GrouponGoodsBean.GrouponGoodsModel> results = LitePal
                    .where("title like ? or sn like ? or goods_sn like ?",
                            "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%")
                    .limit(50)
                    .find(GrouponGoodsBean.GrouponGoodsModel.class);

            runOnUiThread(() -> {
                if (isFinishing()) return;
                grouponGoodsAdapter.setNewData(results);
                grouponGoodsAdapter.hasMore = false;
                if (shop_rv != null) shop_rv.scrollToPosition(0);

//                // ★ 核心修复：搜索完立即把焦点还给“条形码框”，确保下次扫码能正常录入
//                if (et_tiaoxingma != null) {
//                    et_tiaoxingma.requestFocus();
//                }


            });




        });
    }


    private void autoProcessGifts() {
        // 1. 先把当前购物车的“普通商品”取出来存着
        List<GrouponGoodsBean.GrouponGoodsModel> normalList = new ArrayList<>();
        for (GrouponGoodsBean.GrouponGoodsModel m : selectedShopList) {
            if (!m.isIs_zengsong()) {
                normalList.add(m);
            }
        }

        // 2. 清空主列表，准备重新排序
        selectedShopList.clear();
        if (normalList.isEmpty()) return;

        // 3. 统计分类金额（基于普通商品）
        Map<String, BigDecimal> catMap = new HashMap<>();
        for (GrouponGoodsBean.GrouponGoodsModel m : normalList) {
            if (!TextUtils.isEmpty(m.getCategory_ids())) {
                catMap.put(m.getCategory_ids(), catMap.getOrDefault(m.getCategory_ids(), BigDecimal.ZERO).add(m.getHeji()));
            }
        }

        // 4. 汇总赠品数量（Map去重逻辑）
        Map<Integer, Integer> giftQtyMap = new HashMap<>();
        Map<Integer, GrouponGoodsBean.GrouponGoodsModel> giftTempMap = new HashMap<>();

        for (Map.Entry<String, BigDecimal> entry : catMap.entrySet()) {
            // 这里是你查数据库匹配赠品的逻辑
            List<GrouponGoodsBean.GrouponGoodsModel> gifts = LitePal
                    .where("category_ids like ? and subtitle != ?", "%" + entry.getKey() + "%", "")
                    .find(GrouponGoodsBean.GrouponGoodsModel.class);

            for (GrouponGoodsBean.GrouponGoodsModel g : gifts) {
                String sub = g.getSubtitle();
                if (sub != null && sub.matches("\\d+")) {
                    BigDecimal threshold = new BigDecimal(sub);
                    giftTempMap.put(g.getGgspid(), g);
                    if (threshold.compareTo(BigDecimal.ONE) == 0) {
                        giftQtyMap.put(g.getGgspid(), 1); // 袋子逻辑
                    } else {
                        int num = entry.getValue().divide(threshold, 0, RoundingMode.DOWN).intValue();
                        if (num > 0) {
                            giftQtyMap.put(g.getGgspid(), giftQtyMap.getOrDefault(g.getGgspid(), 0) + num);
                        }
                    }
                }
            }
        }

        // 5. 【关键】：先往 selectedShopList 里塞入所有计算出来的赠品
        for (Map.Entry<Integer, Integer> giftEntry : giftQtyMap.entrySet()) {
            GrouponGoodsBean.GrouponGoodsModel template = giftTempMap.get(giftEntry.getKey());
            if (template != null) {
                GrouponGoodsBean.GrouponGoodsModel gift = SerializableUtils.deepCopy(template);
                gift.setShuliang(giftEntry.getValue());
                gift.setIs_zengsong(true);
                gift.setHeji(BigDecimal.ZERO);
                gift.setDiscount("0");
                selectedShopList.add(gift); // 赠品加入，索引 0, 1, 2...
            }
        }

        // 6. 【关键】：再把刚才存的普通商品全部接在后面
        selectedShopList.addAll(normalList);
    }



    /**
     * 辅助方法：将赠品安全克隆入库
     */
    private void addGiftToCart(GrouponGoodsBean.GrouponGoodsModel template, int count) {
        GrouponGoodsBean.GrouponGoodsModel autoGift = SerializableUtils.deepCopy(template);
        autoGift.setShuliang(count);
        autoGift.setIs_zengsong(true);
        autoGift.setHeji(BigDecimal.ZERO);
        autoGift.setDiscount("0");
        selectedShopList.add(autoGift);
    }



}