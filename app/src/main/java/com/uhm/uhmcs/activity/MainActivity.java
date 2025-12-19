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
import android.media.MediaRouter;
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
    private Animation animation;
    private TextView tv_zongjia, tv_zongjian, qingkong_btn, qudan_btn, guadan_btn, dazhe_one_btn, dazhe_all_btn, checkout_btn, daying_btn;
    private LinearLayout huiyuan_btn;
    private TextView huiyuan_name, pingText, httpText, bendin_view, caozuo_view, tv_nickname, tv_phone;
    private LinearLayout have_paid_view, wangluo_view;
    private View shop_mocheng;

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

        qingkong_btn = findViewById(R.id.qingkong_btn);
        guadan_btn = findViewById(R.id.guadan_btn);
        qudan_btn = findViewById(R.id.qudan_btn);
        dazhe_one_btn = findViewById(R.id.dazhe_one_btn);
        dazhe_all_btn = findViewById(R.id.dazhe_all_btn);
        checkout_btn = findViewById(R.id.checkout_btn);
        huiyuan_btn = findViewById(R.id.huiyuan_btn);
        daying_btn = findViewById(R.id.daying_btn);

        if (UserUtils.getInstance().getLoginBase() != null && UserUtils.getInstance().getLoginBase().getData() != null) {
            tv_nickname.setText(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname());
            tv_phone.setText(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getMobile());
        }

        Glide.with(this).load(R.drawable.have_paid_img).into((ImageView) findViewById(R.id.image));
        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_click);
        buildBean = DialogUIUtils.showLoading(this, getString(R.string.paying), true, false, false, false);
        timeCount = new TimeCount(30000, 5000);
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
        loadingPopup = (SyncLoadingPopup) new XPopup.Builder(this)
                .dismissOnTouchOutside(false)
                .dismissOnBackPressed(false)
                .asCustom(new SyncLoadingPopup(this))
                .show();
        downloadGoodsPage(1);
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

                        if (list != null && !list.isEmpty()) {
                            // 准备待插入列表
                            List<GrouponGoodsBean.GrouponGoodsModel> toInsertList = new ArrayList<>();

                            LitePal.beginTransaction();
                            try {
                                for (GrouponGoodsBean.GrouponGoodsModel newItem : list) {

                                    // ★★★ 优化1：删除了 shop_id 的比对过滤，防止因字段为空导致数据全被跳过 ★★★

                                    // ★★★ 优化2：极速写入策略 ★★★
                                    // 尝试按 goods_id 更新。LitePal的updateAll返回受影响行数。
                                    // 如果返回 0，说明数据库里没这条数据，那么就加到 insert 列表里。
                                    // 这样省去了 9000 次 findFirst 的读取耗时。
                                    int rowsAffected = newItem.updateAll("goods_id = ?", String.valueOf(newItem.getGoods_id()));

                                    if (rowsAffected == 0) {
                                        toInsertList.add(newItem);
                                    }
                                }

                                // ★★★ 优化3：批量插入，速度比循环 save 快几十倍 ★★★
                                if (!toInsertList.isEmpty()) {
                                    LitePal.saveAll(toInsertList);
                                    Log.i("SyncDebug", "本页批量新增数据条数: " + toInsertList.size());
                                }

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
                                    downloadGoodsPage(requestPage + 1);
                                    return;
                                }
                            }
                        }
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
            new DeleteShopPopupWindow(MainActivity.this, getString(R.string.Sync_completed), true).show();
        });
    }

    /**
     * 【优化】异步加载本地商品数据，修复分类查询逻辑
     */
    private void loadLocalGoods(int page) {
        if (page == 0) {
            ui_current_page = 0;
            grouponGoodsAdapter.hasMore = true;
            if (shop_rv != null) shop_rv.scrollToPosition(0);
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
                    GrouponGoodsBean.GrouponGoodsModel finalGoods = results.get(0);
                    Log.i("Scan", "查询成功: " + finalGoods.getTitle());
                    runOnUiThread(() -> addGoodsToCart(finalGoods));
                } else {
                    // 2. 没查到，尝试模糊
                    List<GrouponGoodsBean.GrouponGoodsModel> fuzzyList = LitePal
                            .where("sn like ? or goods_sn like ?", "%" + code + "%", "%" + code + "%")
                            .limit(20)
                            .find(GrouponGoodsBean.GrouponGoodsModel.class);

                    runOnUiThread(() -> {
                        if (fuzzyList.isEmpty()) {
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
                            addGoodsToCart(fuzzyList.get(0));
                        } else {
                            grouponGoodsAdapter.setNewData(fuzzyList);
                            grouponGoodsAdapter.hasMore = false;
                        }
                    });
                }
            });
        } else {
            processPayment(code, textType);
        }
    }




    // ----------------- 购物车逻辑 -----------------

    private void addGoodsToCart(GrouponGoodsBean.GrouponGoodsModel goods) {
        allNum++;
        for (int i = 0; i < selectedShopList.size(); i++) {
            GrouponGoodsBean.GrouponGoodsModel model = selectedShopList.get(i);
            if (model.getId() == goods.getId() && model.getGgspid() == goods.getGgspid()) {
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
        zongjia = BigDecimal.ZERO;
        for (GrouponGoodsBean.GrouponGoodsModel model : selectedShopList) {
            zongjia = zongjia.add(model.getHeji());
        }
        zongjia = zongjia.setScale(2, RoundingMode.UP);
        tv_zongjia.setText(zongjia.toString());
        tv_zongjian.setText(String.valueOf(allNum));
        MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
        MyPresentation.setZongjia(zongjia.toString());

        // ▼▼▼▼▼▼ 3. 新增：控制“暂无商品”显示的逻辑 ▼▼▼▼▼▼
        if (selectedShopList == null || selectedShopList.isEmpty()) {
            // 购物车空了：显示提示字，隐藏列表
            if (tv_empty_cart != null) tv_empty_cart.setVisibility(View.VISIBLE);
            if (selected_shop_rv != null) selected_shop_rv.setVisibility(View.GONE);
        } else {
            // 购物车有东西：隐藏提示字，显示列表
            if (tv_empty_cart != null) tv_empty_cart.setVisibility(View.GONE);
            if (selected_shop_rv != null) selected_shop_rv.setVisibility(View.VISIBLE);
        }
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲
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
        // 1. 防重复弹窗检查 (修复弹窗关不掉的问题)
        if (currentCheckoutPopup != null && currentCheckoutPopup.isShowing()) {
            currentCheckoutPopup.dismiss();
        }

        checkoutBean = new CheckoutBean();
        checkoutBean.setAllNum(allNum);
        if (UserUtils.getInstance().getLoginBase() != null && UserUtils.getInstance().getLoginBase().getData() != null) {
            checkoutBean.setUser_id(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getUserId());
        }
        checkoutBean.setMachineNumber("001");
        checkoutBean.setTotal_fee(zongjia.toString());
        checkoutBean.setPay_type((is_kuangjie || !NetworkUtils.getInstance().isNetworkConnected(this)) ? "cash" : "");
        checkoutBean.setShop_id(UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        is_kuangjie = false;

        // 2. 【关键】定义 discount_fee 变量 (解决找不到符号报错)
        BigDecimal discount_fee = BigDecimal.ZERO;
        ArrayList<CheckoutBean.GoodsJsonBean> goodsJsonList = new ArrayList<>();

        // 3. 循环构建商品数据 (包含防空指针和默认值逻辑)
        for (GrouponGoodsBean.GrouponGoodsModel model : selectedShopList) {
            if (model.getDiscounted_price() != null) {
                discount_fee = discount_fee.add(model.getDiscounted_price());
            }

            CheckoutBean.GoodsJsonBean jsonBean = new CheckoutBean.GoodsJsonBean();
            jsonBean.setGoods_id(model.getId());
            jsonBean.setTitle(model.getTitle() != null ? model.getTitle() : "未知商品");
            jsonBean.setGoods_sn(model.getGoods_sn() != null ? model.getGoods_sn() : "");
            jsonBean.setSn(model.getSn() != null ? model.getSn() : "");
            jsonBean.setDiscount(TextUtils.isEmpty(model.getDiscount()) ? "100" : model.getDiscount());
            jsonBean.setDiscounted_price(model.getDiscounted_price() == null ? "0.00" : model.getDiscounted_price().toString());
            jsonBean.setGoods_price(model.getPrice());
            jsonBean.setGoods_num(model.getShuliang());
            jsonBean.setPay_price(model.getHeji() != null ? model.getHeji().toString() : "0.00");
            jsonBean.setGoods_sku_price_id(String.valueOf(model.getGgspid()));

            // 修复 goods_sku_text 为空导致 500 错误的问题
            String safeSkuText = model.getGoods_sku_text();
            if (TextUtils.isEmpty(safeSkuText)) {
                safeSkuText = "[\"商品\"]";
            }
            jsonBean.setGoods_sku_text(safeSkuText);

            goodsJsonList.add(jsonBean);
        }

        checkoutBean.setGoodsjson(new Gson().toJson(goodsJsonList));

        // 4. 设置金额 (现在 discount_fee 已经定义了，不会报错)
        checkoutBean.setDiscount_fee(discount_fee.toString());
        checkoutBean.setTotal_amount(zongjia.add(discount_fee).toString());
        checkoutBean.setGoods_original_amount(zongjia.add(discount_fee).toString());

        // 5. 创建弹窗并赋值给全局变量
        currentCheckoutPopup = new CheckoutPopupWindow(this, checkoutBean, () -> {
            have_paid_view.setVisibility(VISIBLE);
            new Handler(Looper.getMainLooper()).postDelayed(() -> have_paid_view.setVisibility(GONE), 3000);
            is_jiezhang_qingkong = true;
            onClick(qingkong_btn);
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
        buildBean.show();
        String url = POSApiSerview.POS_URL + POSApiSerview.addOrder;
        OkHttpUtil.postJsonAsync(url, new Gson().toJson(checkoutBean), this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                runOnUiThread(() -> {
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        // ... (原有成功逻辑保持不变) ...
                        String msg = jsonObject.optString("msg");
                        if (msg.contains("成功") || msg.contains("Success")) {
                            handlePaymentSuccess(jsonObject);
                        } else if (msg.contains("密码") || msg.contains("process")) {
                            handlePaymentProcess(jsonObject);
                        } else {
                            // 其他错误
                            DialogUIUtils.dismiss(buildBean);
                            new DeleteShopPopupWindow(MainActivity.this, getString(R.string.Payment_failed) + msg, true).show();
                        }
                    } catch (JSONException e) {
                        DialogUIUtils.dismiss(buildBean);
                        Toast.makeText(MainActivity.this, "数据解析异常", LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {
                    DialogUIUtils.dismiss(buildBean);
                    // ★★★ 建议打印具体错误信息，方便排查是超时还是DNS解析失败 ★★★
                    Log.e("PayError", "支付请求失败: " + e.getMessage());
                    new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected) + "\n" + e.getMessage(), true).show();
                });
            }
        });
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



    public void fwsgetOrderInformation(CheckoutBean checkoutBean) {}
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
                            MyPrinterHelper.getInstance().asyncPrintLastOrder(MainActivity.this, list.get(0), null);
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
    public void pushorders(CheckoutBean checkoutBean) {
        if (TextUtils.isEmpty(order_sn)) return;

        Map<String, String> params = new HashMap<>();
        params.put("order_sn", order_sn);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        if (!TextUtils.isEmpty(transaction_id)) {
            params.put("transaction_id", transaction_id);
        }

        String url = POSApiSerview.POS_URL + POSApiSerview.pushorders;

        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("PushOrder", "推单成功: " + response);
            }

            @Override
            public void onFailure(IOException e) {
                Log.e("PushOrder", "推单失败: " + e.getMessage());
            }
        });
    }


}