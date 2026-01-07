package com.uhm.uhmcs.activity;

import com.example.scaler.AclasScaler;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import android.widget.EditText;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.LocaleList;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.uhm.uhmcs.utils.EcrProtocol;
import com.uhm.uhmcs.utils.EcrSocketManager;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;


import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.dou361.dialogui.DialogUIUtils;
import com.dou361.dialogui.bean.BuildBean;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.makeramen.roundedimageview.RoundedImageView;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.adapter.GrouponGoodsAdapter;
import com.uhm.uhmcs.adapter.SelectedShopAdapter;
import com.uhm.uhmcs.adapter.ShopTypeAdapter;
import android.os.Message;
import com.uhm.uhmcs.bean.CategoryListBean;
import com.uhm.uhmcs.bean.CheckoutBean;
import com.uhm.uhmcs.bean.GrouponGoodsBean;
import com.uhm.uhmcs.bean.LastOrderBean;
import com.uhm.uhmcs.bean.MemberBean;
import com.uhm.uhmcs.bean.PrintDataBean;
import com.uhm.uhmcs.bean.RegistrationShopBean;
import com.uhm.uhmcs.http.NetworkErrorInterceptor;
import com.uhm.uhmcs.http.NetworkLatencyMonitor;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.popupwindow.AddNoCodePopupWindow;
import com.uhm.uhmcs.popupwindow.CheckoutPopupWindow;
import com.uhm.uhmcs.popupwindow.DeleteShopPopupWindow;
import com.uhm.uhmcs.popupwindow.DiscountPopupWindow;
import com.uhm.uhmcs.popupwindow.GetRegistrationShopPopupWindow;
import com.uhm.uhmcs.popupwindow.GoodsWarehousingPopupWindow;
import com.uhm.uhmcs.popupwindow.HistoryOrderPopupWindow;
import com.uhm.uhmcs.popupwindow.MemberPopupWindow;
import com.uhm.uhmcs.popupwindow.MemberRechargePopupWindow;
import com.uhm.uhmcs.popupwindow.MoneyBoxPopupWindow;
import com.uhm.uhmcs.popupwindow.MorefunctionPopupWindow;
import com.uhm.uhmcs.popupwindow.PaymentListPopupWindow;
import com.uhm.uhmcs.popupwindow.PopupWindowOnClickListener;
import com.uhm.uhmcs.popupwindow.PrintDevicePopupWindow;
import com.uhm.uhmcs.popupwindow.PrintLabelsPopupWindow;
import com.uhm.uhmcs.popupwindow.RelieveShiftPopupWindow;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.MyUsbDeviceHelper;
import com.uhm.uhmcs.utils.NetworkUtils;
import com.uhm.uhmcs.utils.SerializableUtils;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import java.util.Map;
import com.uhm.uhmcs.popupwindow.PosSettingPopupWindow;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends Activity {

    // --- 称重平移变量 ---
    private final int MSG_Weight = 5;
    private AclasScaler m_scaler = null;
    private AclasScaler.WeightInfoNew m_weight = null;
    private TextView weight_id;      // 对应收银 UI 的 tv_real_weight
    private TextView tv_tareWeight;  // 对应收银 UI 的 tv_tare_weight

    // --- 新增下面这两行，解决你现在的报错 ---
    private TextView tv_main_shop_name;
    private TextView tv_nickname;


    // --- 新增：NETS POS 状态指示变量 ---
    private View v_pos_status_light;
    private TextView tv_pos_status_text;
    private EcrSocketManager ecrSocketManager = new EcrSocketManager(); // 实例化管理器



    // --- 购物车相关变量 (必须保留) ---
// 这个 list 存放的是顾客选中的商品，不是全部库存
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> selectedShopList = new ArrayList<>();

    // 对应的适配器也需要定义
    private SelectedShopAdapter selectedShopAdapter;



    // --- 确保添加了这两行声明 ---
    private TextView tv_real_weight;
    private TextView tv_tare_weight;
    private TextView btn_qupi;

    // 新增占位视图变量
    private TextView tv_image_placeholder;
    private LinearLayout ll_cart_empty_placeholder;

    @SuppressLint("HandlerLeak")
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_Weight:
                    showWeight(getWeightInfo());
                    break;
            }
        }
    };




    private Integer selectedShopIndex;
    private RecyclerView rv_choose_menu3, shop_rv, selected_shop_rv;
    private LinearLayoutManager selected_LinearLayoutManager;
    private ShopTypeAdapter shopTypeAdapter;
    private GrouponGoodsAdapter grouponGoodsAdapter;

    private EditText et_search_pinyin; // 提升为类成员变量

    private CustomInputTextView et_tiaoxingma;
    private Animation animation;
    private TextView tv_zongjia, tv_zongjian, qingkong_btn, qudan_btn, guadan_btn, dazhe_one_btn, dazhe_all_btn, checkout_btn, daying_btn;
    private LinearLayout huiyuan_btn;
    private TextView huiyuan_name;

    private ImageView shanchuhuiyuan_btn;

    private DeleteShopPopupWindow deleteShopPopupWindow;
    private BigDecimal zongjia = new BigDecimal("0.00");
    ;


    // --- 数据库与同步相关 (新增) ---
// 使用单线程池执行数据库操作，避免卡顿主线程
    private final java.util.concurrent.ExecutorService dbExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
    private static final int SYNC_PAGE_SIZE = 1000; // 接口每页抓取数量
    private boolean is_tongbu = false; // 同步状态锁
    private com.uhm.uhmcs.popupwindow.SyncLoadingPopup loadingPopup; // 自定义同步弹窗



    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> historySelectedShopList = new ArrayList<>();

    private ArrayList<RegistrationShopBean> registrationShopBeanArrayList = new ArrayList<>();

    private View.OnClickListener onClickListener;

    private GetRegistrationShopPopupWindow getRegistrationShopPopupWindow;
    private boolean is_kedian = true;
    private BigDecimal zong_youhui = new BigDecimal("0.00");
    private String memben_discount;

    BuildBean buildBean;
    private MyPresentation presentation;
    private LinearLayout have_paid_view, wangluo_view;
    private TextView bendin_view;

    private TextView caozuo_view;

    private View shop_mocheng;

    private RoundedImageView shop_image;


    private TextView yingfu_tv,shifu_tv,youhui_tv,daijinquan_tv,xianjin_tv,huiyuanka_tv,weixin_tv,zhifubao_tv,zhaolin_tv;
    private LinearLayout zhifuxinxi_view;


    // --- 新增：称重盲扫控制变量 ---
    private int currentScanIndex = 0;
    private boolean isScaleConnected = false;
    private String lastAttemptPort = ""; // 用于记录最后一次尝试的端口
    private final String[] SCAN_PORTS = {
            "/dev/ttyS0", "/dev/ttyS1", "/dev/ttyS2", "/dev/ttyS3",
            "/dev/ttyS4", "/dev/ttyS5", "/dev/ttyS6", "/dev/ttyS7",
            "/dev/ttyS8", "/dev/ttyS9", "/dev/ttyS10", "/dev/ttyS11",
            "/dev/ttyS12", "/dev/ttyS13", "/dev/ttyS14", "/dev/ttyS15",
            "/dev/ttyAMA0", "/dev/ttyAMA1", "/dev/ttyAMA2", "/dev/ttyAMA3"
    };






    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. 基础 UI 初始化
        initView();

        // 2. 称重设备初始化（已将耗时提权逻辑移至异步，防止启动 ANR）
        InitDevice(0);


        Log.i("ttt", ">>>>onCreate 启动成功>>>>");
        MyUsbDeviceHelper.getInstance().inti(this);

        // 3. 网络状态与本地订单 UI 显示
        boolean isConnected = NetworkUtils.getInstance().isNetworkConnected(this);
        wangluo_view.setVisibility(isConnected ? GONE : VISIBLE);

        if (TextUtils.isEmpty(UserUtils.getInstance().getOrderListJson())) {
            bendin_view.setVisibility(GONE);
        } else {
            bendin_view.setVisibility(VISIBLE);
        }

        // 4. 加载分类列表（分类数据量小，保留缓存逻辑，但增加判空保护）
        loadCategoryData(isConnected);

        // 5. 【核心修改】加载商品数据：改用 LitePal 数据库加载，不再解析大 JSON
        loadGoodsFromDb(isConnected);

        // 6. 注册网络监听
        if (networkChangeReceiver == null) {
            networkChangeReceiver = registerNetworkReceiver(this);
        }

        // 启动时自动检查一次 POS 连接
        checkPosConnection();


    }

    /**
     * 封装：分类加载逻辑
     */
    private void loadCategoryData(boolean isConnected) {
        String categoryJson = UserUtils.getInstance().getCategoryListBeanJson();
        if (!TextUtils.isEmpty(categoryJson)) {
            try {
                Gson gson = new Gson();
                CategoryListBean categoryListBean = gson.fromJson(categoryJson, CategoryListBean.class);
                if (categoryListBean != null && categoryListBean.getData() != null) {
                    shopTypeAdapter.setNewData(categoryListBean.getData());
                }
            } catch (Exception e) {
                Log.e("ttt", "分类缓存解析失败: " + e.getMessage());
                if (isConnected) OverviewList();
            }
        } else {
            if (isConnected) OverviewList();
        }

    }

    /**
     * 封装：商品加载逻辑（LitePal 化方案）
     * 彻底解决 BEGIN_ARRAY 导致的崩溃，因为不再读取旧缓存 JSON
     */
    private void loadGoodsFromDb(boolean isConnected) {
        dbExecutor.execute(() -> {
            // A. 先检查本地数据库里有没有商品
            int count = org.litepal.LitePal.count(GrouponGoodsBean.GrouponGoodsModel.class);

            if (count > 0) {
                // B. 数据库有数据，加载前 20 条展示
                List<GrouponGoodsBean.GrouponGoodsModel> localList = org.litepal.LitePal
                        .limit(20)
                        .find(GrouponGoodsBean.GrouponGoodsModel.class);

                runOnUiThread(() -> {
                    grouponGoods_page = 1;
                    grouponGoodsAdapter.setNewData(localList);
                    grouponGoodsAdapter.hasMore = true;
                    Log.i("ttt", "从本地数据库加载了 " + count + " 条商品数据");
                });
            } else {
                // C. 数据库是空的
                runOnUiThread(() -> {
                    if (isConnected) {
                        // 联网状态下，自动触发我们新写的分页同步逻辑
                        syncGoodsData();
                    } else {
                        // 没网也没数据，只能显示空列表或报错
                        Toast.makeText(this, "本地无数据，请检查网络并同步", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }






    private NetworkChangeReceiver networkChangeReceiver;
    // 声明一个延迟任务，防止网络切换瞬间闪烁红字
    private Runnable networkErrorRunnable;

    // 网络变化广播接收器
    private class NetworkChangeReceiver extends BroadcastReceiver {
        private final Activity activity;

        public NetworkChangeReceiver(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            // 使用你的现有的 handler 来处理延迟
            if (NetworkUtils.getInstance().isNetworkConnected(MainActivity.this)) {
                // 1. 如果当前连通了，立即取消掉那个“准备报错”的任务
                if (networkErrorRunnable != null) {
                    handler.removeCallbacks(networkErrorRunnable);
                }
                // 2. 隐藏红字
                wangluo_view.setVisibility(GONE);

                if (TextUtils.isEmpty(UserUtils.getInstance().getOrderListJson())) {
                    bendin_view.setVisibility(GONE);
                } else {
                    bendin_view.setVisibility(VISIBLE);
                }
            } else {
                // 3. 如果检测到断网，不要立刻显示，先排队一个 2 秒后的任务
                if (networkErrorRunnable == null) {
                    networkErrorRunnable = () -> {
                        bendin_view.setVisibility(GONE);
                        wangluo_view.setVisibility(VISIBLE);
                    };
                }
                // 先移除之前的，确保不重复排队
                handler.removeCallbacks(networkErrorRunnable);
                // 2000毫秒（2秒）后执行报错显示
                handler.postDelayed(networkErrorRunnable, 2000);
            }
        }
    }




    // 注册网络状态监听
    private NetworkChangeReceiver registerNetworkReceiver(Activity context) {
        NetworkChangeReceiver receiver;
        receiver = new NetworkChangeReceiver(context);
        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);
        return receiver;
    }


    // 强制隐藏软键盘
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN
            );
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
        }
    }

    TextView pingText;
    TextView httpText;
    NetworkLatencyMonitor monitor;

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // --- 必须加入下面这一段 ---
        if (m_scaler != null) {
            m_scaler.AclasDisconnect();
        }

        if (networkChangeReceiver != null) {
            unregisterReceiver(networkChangeReceiver);
        }
        monitor.stopMonitoring();
        MyUsbDeviceHelper.getInstance().unregisterReceiver();
    }

    boolean is_kuangjie = false;
    int allNum = 0;
    public MemberBean memberBean1;
    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged", "SimpleDateFormat"})


    private void initView() {

        // 绑定新添加的占位视图
        tv_image_placeholder = findViewById(R.id.tv_image_placeholder);
        ll_cart_empty_placeholder = findViewById(R.id.ll_cart_empty_placeholder);


        // 1. 【核心修复】必须把这一行提到最前面，防止空指针崩溃
        et_search_pinyin = findViewById(R.id.et_search_pinyin);

        MediaRouter mediaRouter = (MediaRouter) getSystemService(Context.MEDIA_ROUTER_SERVICE);
        MediaRouter.RouteInfo route = mediaRouter.getSelectedRoute(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        if (route != null) {
            Display presentationDisplay = route.getPresentationDisplay();
            if (presentationDisplay != null) {
                presentation = new MyPresentation(this, presentationDisplay);
                presentation.show();
            }
        }

        yingfu_tv=findViewById(R.id.yingfu_tv);
        shifu_tv=findViewById(R.id.shifu_tv);
        youhui_tv=findViewById(R.id.youhui_tv);
        daijinquan_tv=findViewById(R.id.daijinquan_tv);
        xianjin_tv=findViewById(R.id.xianjin_tv);
        huiyuanka_tv=findViewById(R.id.huiyuanka_tv);
        weixin_tv=findViewById(R.id.weixin_tv);
        zhifubao_tv=findViewById(R.id.zhifubao_tv);
        zhaolin_tv=findViewById(R.id.zhaolin_tv);
        zhifuxinxi_view=findViewById(R.id.zhifuxinxi_view);

        tv_real_weight = findViewById(R.id.tv_real_weight);
        tv_tare_weight = findViewById(R.id.tv_tare_weight);
        weight_id = tv_real_weight;
        tv_tareWeight = tv_tare_weight;

        findViewById(R.id.btn_qupi).setOnClickListener(v -> {
            if (m_scaler != null && m_weight != null && m_weight.isStable) {
                m_scaler.AclasTare();
            }
        });





        onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                 /*
                  刷新商品
                 */
                // 在 onClickListener 的 switch(id) 中找到 R.id.shuaxin_btn
                if (id == R.id.shuaxin_btn) {
                    // 检查网络，有网才同步
                    if (NetworkUtils.getInstance().isNetworkConnected(MainActivity.this)) {
                        syncGoodsData(); // 触发新写的 LitePal 同步逻辑
                        OverviewList();  // 同时刷新分类
                    } else {
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected), true).show();
                    }
                }



                /*
                  删除商品
                 */
                if (id == R.id.delete_shop) {
                    if (selectedShopIndex == null) {
                        if (selectedShopAdapter.getItemCount() > 0) {
                            new DeleteShopPopupWindow(MainActivity.this, getString(R.string.please_select_product)).show();
                        }
                        return;
                    }
                    deleteShopPopupWindow = new DeleteShopPopupWindow(MainActivity.this, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(String string) {
                            // ... 原有的删除逻辑 ...
                            if (!selectedShopList.get(selectedShopIndex).isIs_zengsong()) {
                                zongjia = zongjia.subtract(selectedShopList.get(selectedShopIndex).getHeji()).setScale(2, RoundingMode.DOWN);
                                tv_zongjia.setText(zongjia + "");
                                MyPresentation.setZongjia(zongjia.toString());
                            }
                            allNum = allNum - selectedShopList.get(selectedShopIndex).getShuliang();
                            selectedShopList.remove(selectedShopIndex.intValue());
                            selectedShopAdapter.setNewData(selectedShopList);

                            MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                            tv_zongjian.setText(allNum + "");
                            selectedShopIndex = null;
                            selected_LinearLayoutManager.scrollToPosition(0);

                            // ★★★ 核心修复：在这里调用！当用户点击“确定删除”后，立即刷新占位符状态 ★★★
                            updatePlaceholderVisibility();
                        }
                    });
                    deleteShopPopupWindow.show();

                    // 【删除这里】不要在 show() 后面调用
                    // updatePlaceholderVisibility();
                }
                /*
                  清空商品
                 */
                if (id == R.id.qingkong_btn) {
                    if (selectedShopList.isEmpty()) {
                        return;
                    }
                    selectedShopList.clear();
                    selectedShopAdapter.notifyDataSetChanged();
                    zongjia = new BigDecimal("0.00");
                    tv_zongjia.setText(zongjia + "");
                    tv_zongjian.setText("0");
                    allNum = 0;
                    MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                    MyPresentation.setZongjia(zongjia.toString());

                    updatePlaceholderVisibility();
                }
                /*
                  挂单
                 */
                if (id == R.id.guadan_btn) {
                    if (selectedShopAdapter.getItemCount() <= 0) {
                        return;
                    }
                    // 定义日期格式模板
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    // 获取当前时间（基于系统时区）
                    String formattedTime = sdf.format(System.currentTimeMillis());
                    RegistrationShopBean registrationShopBean = new RegistrationShopBean();
                    registrationShopBean.setTime(formattedTime);
                    registrationShopBean.setTotal_price(zongjia);
                    registrationShopBean.setAllNum(allNum);
                    ArrayList<GrouponGoodsBean.GrouponGoodsModel> registrationShopList = SerializableUtils.deepCopyList(selectedShopList);
                    registrationShopBean.setRegistrationShopList(registrationShopList);
                    registrationShopBeanArrayList.add(registrationShopBean);

                    // 获取当前的挂单数量
                    int count = registrationShopBeanArrayList.size();
// 使用 HTML 标签来自定义数字的颜色和大小
// <font color='#FF0000'> 设置颜色，<big> 或 <small> 或指定样式设置大小
                    String styledText = "取单(<font color='#FF0000'><big>" + count + "</big></font>)";

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        qudan_btn.setText(android.text.Html.fromHtml(styledText, android.text.Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        qudan_btn.setText(android.text.Html.fromHtml(styledText));
                    }

                    selectedShopList.clear();
                    selectedShopAdapter.setNewData(selectedShopList);
                    zongjia = new BigDecimal("0.00");
                    tv_zongjia.setText(zongjia + "");
                    tv_zongjian.setText("0");
                    allNum = 0;
                    MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                    MyPresentation.setZongjia(zongjia.toString());

                    updatePlaceholderVisibility();

                }

                /*
                   取单
                 */
                if (id == R.id.qudan_btn) {
                    if (!registrationShopBeanArrayList.isEmpty()) {
                        getRegistrationShopPopupWindow = new GetRegistrationShopPopupWindow(MainActivity.this, registrationShopBeanArrayList, new PopupWindowOnClickListener.GetRegistrationShopOnClickListener() {
                            @Override
                            public void onClick(int index, int type) {
                                RegistrationShopBean registrationShopBean = registrationShopBeanArrayList.get(index);

                                if (type == 1) {//取单
                                    Log.i("ttt", ">>>>>>>>>>>>取单");
                                    selectedShopAdapter.setNewData(new ArrayList<>());
                                    zongjia = registrationShopBean.getTotal_price();
                                    tv_zongjia.setText(zongjia + "");
                                    selectedShopList = new ArrayList<>();
                                    selectedShopList = registrationShopBean.getRegistrationShopList();
                                    tv_zongjian.setText(registrationShopBean.getAllNum() + "");
                                    allNum = registrationShopBean.getAllNum();
                                    selectedShopAdapter.setNewData(selectedShopList);
                                    MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                                    MyPresentation.setZongjia(zongjia.toString());
                                    availableAmount();

                                    // ★★★ 核心修复：在这里调用，数据恢复后立刻隐藏占位符 ★★★
                                    updatePlaceholderVisibility();

                                } else if (type == 2) {//删除
                                    if (!registrationShopBeanArrayList.isEmpty()) {
                                        getRegistrationShopPopupWindow.setDataDelect();
                                    }

                                    // 如果删完了，也刷新一下（防止误操作导致的逻辑不符）
                                    updatePlaceholderVisibility();

                                }
                                registrationShopBeanArrayList.remove(index);
                                if (!registrationShopBeanArrayList.isEmpty()) {
                                    qudan_btn.setText(getString(R.string.qudan_num, registrationShopBeanArrayList.size() + ""));
                                } else {
                                    qudan_btn.setText(getString(R.string.qudan_null));
                                }

                            }
                        });
                        getRegistrationShopPopupWindow.show();
                    }



                }

                /*
                  改价/打折
                 */
                if (id == R.id.dazhe_one_btn) {
                    if (selectedShopIndex == null) {
                        if (selectedShopAdapter.getItemCount() > 0) {
                            new DeleteShopPopupWindow(MainActivity.this, getString(R.string.please_select_product)).show();
                        }
                        return;
                    }
                    new DiscountPopupWindow(MainActivity.this, getString(R.string.item_discount), new PopupWindowOnClickListener.DiscountOnClickListener() {
                        @Override
                        public void onClick(String discount) {
                            GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = selectedShopAdapter.getData().get(selectedShopIndex);
                            grouponGoodsModel.setDiscount(discount);

                            zongjia = zongjia.subtract(grouponGoodsModel.getHeji());
                            Log.i("ttt", ">>>>>>" + zongjia);
                            if (grouponGoodsModel.getOnline_type().equals("weight")){


                                BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                                BigDecimal zhehoujia = price.multiply(new BigDecimal(discount)).divide(new BigDecimal("100"));


                                BigDecimal heji = zhehoujia.divide(new BigDecimal(500)).multiply(new BigDecimal(grouponGoodsModel.getGoods_weight())).setScale(2, RoundingMode.DOWN);

                                grouponGoodsModel.setHeji(heji);
                                grouponGoodsModel.setDiscounted_price(price.subtract(zhehoujia).divide(new BigDecimal(500)).multiply(new BigDecimal(grouponGoodsModel.getGoods_weight())).setScale(2, RoundingMode.DOWN));
                                zongjia = zongjia.add(heji);

                            }else {
                                BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));

                                BigDecimal zhehoujia = yuanjia.multiply(new BigDecimal(discount)).divide(new BigDecimal("100"));

                                grouponGoodsModel.setDiscounted_price(yuanjia.subtract(zhehoujia));

                                grouponGoodsModel.setHeji(zhehoujia);

                                zongjia = zongjia.add(zhehoujia);

                            }
                            Log.i("ttt", ">>sss>>>>" + zongjia);
                            tv_zongjia.setText(zongjia + "");
                            selectedShopAdapter.notifyItemChanged(selectedShopIndex);
                            MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                            MyPresentation.setZongjia(zongjia.toString());
                            availableAmount();

                        }
                    }).show();
                }
                /*
                  整单打折
                 */
                if (id == R.id.dazhe_all_btn) {
                    if (selectedShopAdapter.getItemCount() <= 0) {
                        return;
                    }
                    new DiscountPopupWindow(MainActivity.this, getString(R.string.order_discount), new PopupWindowOnClickListener.DiscountOnClickListener() {
                        @Override
                        public void onClick(String discount) {
                            for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopAdapter.getData()) {

                                grouponGoodsModel.setDiscount(discount);

                                zongjia = zongjia.subtract(grouponGoodsModel.getHeji());
                                Log.i("ttt", ">>>>>>" + zongjia);



                                if (grouponGoodsModel.getOnline_type().equals("weight")){


                                    BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                                    BigDecimal zhehoujia = price.multiply(new BigDecimal(discount)).divide(new BigDecimal("100"));


                                    BigDecimal heji = zhehoujia.divide(new BigDecimal(500)).multiply(new BigDecimal(grouponGoodsModel.getGoods_weight())).setScale(2, RoundingMode.DOWN);

                                    grouponGoodsModel.setHeji(heji);
                                    grouponGoodsModel.setDiscounted_price(price.subtract(zhehoujia).divide(new BigDecimal(500)).multiply(new BigDecimal(grouponGoodsModel.getGoods_weight())).setScale(2, RoundingMode.DOWN));
                                    zongjia = zongjia.add(heji);

                                }else {
                                    BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));

                                    BigDecimal zhehoujia = yuanjia.multiply(new BigDecimal(discount)).divide(new BigDecimal("100"));

                                    grouponGoodsModel.setDiscounted_price(yuanjia.subtract(zhehoujia));

                                    grouponGoodsModel.setHeji(zhehoujia);

                                    zongjia = zongjia.add(zhehoujia);


                                }


                            }
                            Log.i("ttt", ">>sss>>>>" + zongjia);
                            tv_zongjia.setText(zongjia + "");
                            selectedShopAdapter.notifyDataSetChanged();
                            MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                            MyPresentation.setZongjia(zongjia.toString());
                            availableAmount();

                        }
                    }).show();
                }

                /*
                  结账
                 */
                if (id == R.id.checkout_btn) {
                    if (selectedShopAdapter.getItemCount() <= 0) {
                        return;
                    }
                    if (Utilis.isFastClick()) {
                        return;
                    }
                    CheckoutBean checkoutBean = new CheckoutBean();
                    checkoutBean.setAllNum(allNum);
                    checkoutBean.setUser_id(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getUserId());

                    checkoutBean.setTotal_fee(zongjia.toString());
                    if (is_kuangjie || !NetworkUtils.getInstance().isNetworkConnected(MainActivity.this)) {
                        checkoutBean.setPay_type("cash");
                    } else {
                        checkoutBean.setPay_type("");
                    }
                    is_kuangjie = false;
//                    checkoutBean.setPay_fee();
                    checkoutBean.setShop_id(UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
                    BigDecimal discount_fee = new BigDecimal("0.00");
                    ArrayList<CheckoutBean.GoodsJsonBean> goodsJsonBeanArrayList = new ArrayList<>();


                    for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopList) {
                        if (grouponGoodsModel.getDiscounted_price() != null) {
                            discount_fee = discount_fee.add(grouponGoodsModel.getDiscounted_price());
                        }
                        CheckoutBean.GoodsJsonBean goodsJsonBean = new CheckoutBean.GoodsJsonBean();
                        goodsJsonBean.setGoods_id(grouponGoodsModel.getIds());
                        goodsJsonBean.setTitle(grouponGoodsModel.getTitle());
                        goodsJsonBean.setGoods_sn(grouponGoodsModel.getGoods_sn());
                        goodsJsonBean.setSn(grouponGoodsModel.getSn());
                        goodsJsonBean.setDiscount(TextUtils.isEmpty(grouponGoodsModel.getDiscount()) ? "100" : grouponGoodsModel.getDiscount());
                        goodsJsonBean.setDiscounted_price(grouponGoodsModel.getDiscounted_price() == null ? "0.00" : grouponGoodsModel.getDiscounted_price().toString());
                        goodsJsonBean.setGoods_price(grouponGoodsModel.getPrice());
                        goodsJsonBean.setGoods_num(grouponGoodsModel.getShuliang());
                        goodsJsonBean.setGoods_weight(grouponGoodsModel.getGoods_weight());
                        goodsJsonBean.setOnline_type(grouponGoodsModel.getOnline_type());
                        goodsJsonBean.setPay_price(grouponGoodsModel.getHeji().toString());
                        goodsJsonBean.setGoods_sku_price_id(grouponGoodsModel.getGgspid() + "");
//                        goodsJsonBean.setGoods_sku_text(TextUtils.isEmpty(grouponGoodsModel.getGoods_sku_text()) ? "" : grouponGoodsModel.getGoods_sku_text());
                        goodsJsonBeanArrayList.add(goodsJsonBean);
                    }
                    Gson gson = new Gson();

                    checkoutBean.setGoodsjson(gson.toJson(goodsJsonBeanArrayList));
                    checkoutBean.setDiscount_fee(discount_fee.toString());
                    checkoutBean.setTotal_amount(zongjia.add(discount_fee).toString());
                    checkoutBean.setGoods_original_amount(zongjia.add(discount_fee).toString());
//                    checkoutBean.setTotal_amount((int) 0.01);
//                    checkoutBean.setTotal_amount((int) 0.01);
                    if (memberBean1 != null) {
                        checkoutBean.setMember_name(memberBean1.getNickname());
                        checkoutBean.setMember_phone(memberBean1.getMobile());
//                        checkoutBean.setCardnumber(memberBean1.getMobile());
                        checkoutBean.setCoupon_fee(Coupon_fee);
                    }

                    CheckoutPopupWindow checkoutPopupWindow = new CheckoutPopupWindow(MainActivity.this, checkoutBean, new PopupWindowOnClickListener.CheckoutOnClickListener() {
                        @Override
                        public void onClick(CheckoutBean checkoutBean, String xinjin_pice, String weixin_pice, String zhifubao_pice, String huiyuanka_pice) {
                            have_paid_view.setVisibility(VISIBLE);
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    have_paid_view.setVisibility(GONE);
                                }
                            }, 3000);
                            onClickListener.onClick(qingkong_btn);
                            onClickListener.onClick(shanchuhuiyuan_btn);


                            BigDecimal shifujine_pice = new BigDecimal("0.00");
                            if (!TextUtils.isEmpty(xinjin_pice)) {
                                shifujine_pice = shifujine_pice.add(new BigDecimal(xinjin_pice));
                            }
                            if (!TextUtils.isEmpty(weixin_pice)) {
                                shifujine_pice = shifujine_pice.add(new BigDecimal(weixin_pice));
                            }
                            if (!TextUtils.isEmpty(zhifubao_pice)) {
                                shifujine_pice = shifujine_pice.add(new BigDecimal(zhifubao_pice));
                            }
                            if (!TextUtils.isEmpty(huiyuanka_pice)) {
                                shifujine_pice = shifujine_pice.add(new BigDecimal(huiyuanka_pice));
                            }

                            yingfu_tv.setText(checkoutBean.getTotal_amount());
                            youhui_tv.setText("-"+checkoutBean.getDiscount_fee());
                            daijinquan_tv.setText("-"+(TextUtils.isEmpty(checkoutBean.getCoupon_fee()) ? "0.00" : checkoutBean.getCoupon_fee()));
                            shifu_tv.setText(shifujine_pice.toString());
                            xianjin_tv.setText(TextUtils.isEmpty(xinjin_pice) ? "0.00" : xinjin_pice);
                            huiyuanka_tv.setText(TextUtils.isEmpty(huiyuanka_pice) ? "0.00" : huiyuanka_pice);
                            weixin_tv.setText(TextUtils.isEmpty(weixin_pice) ? "0.00" : weixin_pice);
                            zhifubao_tv.setText(TextUtils.isEmpty(zhifubao_pice) ? "0.00" : zhifubao_pice);
                            zhaolin_tv.setText(TextUtils.isEmpty(checkoutBean.getCash_change()) ? "0.00" : checkoutBean.getCash_change());
                            zhifuxinxi_view.setVisibility(VISIBLE);

                        }
                    });
                    Log.i("ttt", ">>>>>>>sss>SS>>" + checkoutPopupWindow.isShow());
                    if (checkoutPopupWindow.isShow()) {
                        checkoutPopupWindow.dismiss();
                    } else {
                        checkoutPopupWindow.show();
                    }

                }

                /*
                  会员查询
                 */
                if (id == R.id.huiyuan_btn) {
                    new MemberPopupWindow(MainActivity.this, memberBean1, new PopupWindowOnClickListener.MemberOnClickListener() {
                        @Override
                        public void onClick(MemberBean memberBean) {
                            memberBean1 = memberBean;
                            huiyuan_name.setText(memberBean.getNickname());
                            availableAmount();
//                            if (memberBean.getVip() == 0) {
//                                memben_discount = "";
//                            }
//                            if (memberBean.getVip() == 17) {//青铜
//                                memben_discount = "98";
//                            }
//                            if (memberBean.getVip() == 18) {//白银
//                                memben_discount = "95";
//                            }
//                            if (memberBean.getVip() == 19) {//黄金
//                                memben_discount = "90";
//                            }
//                            if (selectedShopAdapter.getItemCount() <= 0) {
//                                return;
//                            }
//                            for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopAdapter.getData()) {
//
//                                grouponGoodsModel.setDiscount(memben_discount);
//
//                                zongjia = zongjia.subtract(grouponGoodsModel.getHeji());
//                                Log.i("ttt", ">>>>>>" + zongjia);
//
//                                BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));
//
//                                BigDecimal zhehoujia = yuanjia.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal("100"));
//
//                                grouponGoodsModel.setDiscounted_price(yuanjia.subtract(zhehoujia));
//
//                                grouponGoodsModel.setHeji(zhehoujia);
//
//                                zongjia = zongjia.add(zhehoujia);
//                                tv_zongjia.setText(zongjia + "");
//                                selectedShopAdapter.notifyDataSetChanged();
//                                MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
//                                MyPresentation.setZongjia(zongjia.toString());
//
//                            }
                        }
                    }).show();
                }
                /*
                  打印尾单
                 */
                if (id == R.id.daying_btn) {
                    getLastOder();

                }
                /*
                 * 删除会员
                 */
                if (R.id.shanchuhuiyuan_btn == id) {
                    memberBean1 = null;
                    huiyuan_name.setText(getString(R.string.member_nickname));
                    Coupon_fee = "";
//                    memben_discount = "100";
//
//                    if (selectedShopAdapter.getItemCount() <= 0) {
//                        return;
//                    }
//                    for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopAdapter.getData()) {
//
//                        grouponGoodsModel.setDiscount(memben_discount);
//
//                        zongjia = zongjia.subtract(grouponGoodsModel.getHeji());
//                        Log.i("ttt", ">>>>>>" + zongjia);
//
//                        BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));
//
//                        BigDecimal zhehoujia = yuanjia.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal("100"));
//
//                        grouponGoodsModel.setDiscounted_price(yuanjia.subtract(zhehoujia));
//
//                        grouponGoodsModel.setHeji(zhehoujia);
//
//                        zongjia = zongjia.add(zhehoujia);
//                        tv_zongjia.setText(zongjia + "");
//                        selectedShopAdapter.notifyDataSetChanged();
//                        MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
//                        MyPresentation.setZongjia(zongjia.toString());
//
//                    }
                }
                /*
                 * 更多功能
                 */
                if (R.id.more_function_btn == id) {

//                    MyLabeksPrinterHelper.getInstance().asyncPrintCheckout(MainActivity.this,new ArrayList<>());

                    new MorefunctionPopupWindow(MainActivity.this, new PopupWindowOnClickListener.MorefunctionOnClickListener() {
                        @Override
                        public void onClick(int btnType) {
                            switch (btnType) {
                                /*
                                 * 同步数据
                                 */
                                case 1:
                                    syncGoodsData();
                                    break;
                                /*
                                 * 标签打印
                                 */
                                case 2:
                                    new PrintLabelsPopupWindow(MainActivity.this).show();

                                    break;
                                /*
                                 * 商品入库
                                 */
                                case 3:
                                    new GoodsWarehousingPopupWindow(MainActivity.this, allGrouponGoodsModelList, new PopupWindowOnClickListener.GoodsWarehousingOnClickListener() {
                                        @Override
                                        public void onClick(int code, String msg) {
                                            Log.i("ttt", ">>>>>>wwww>>>" + msg);
                                            if (code == 1) {
                                                new DeleteShopPopupWindow(MainActivity.this, getString(R.string.product_stocked_successfully), true).show();
                                            } else {
                                                new DeleteShopPopupWindow(MainActivity.this, msg, true).show();
                                            }
                                        }
                                    }).show();
                                    break;
                                /*
                                 * 历史账单
                                 */
                                case 4:
                                    new HistoryOrderPopupWindow(MainActivity.this).show();
                                    break;
                                /*
                                 * 退出登录
                                 */
                                case 5:
                                    UserUtils.getInstance().setCategoryListBeanJson(MainActivity.this, "");
                                    UserUtils.getInstance().setGrouponGoodsBeanJson(MainActivity.this, "");
                                    UserUtils.getInstance().setLoginBase(MainActivity.this, null);
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    break;
                                /*
                                 * 钱箱设置
                                 */
                                case 6:
                                    new MoneyBoxPopupWindow(MainActivity.this).show();
                                    break;
                                /*
                                 * 交接班
                                 */
                                case 7:
                                    new RelieveShiftPopupWindow(MainActivity.this).show();
                                    break;
                                /*
                                 * 账单打印机设置
                                 */
                                case 8:
                                    new PrintDevicePopupWindow(MainActivity.this, 1, new PopupWindowOnClickListener.PrintDeviceOnClickListener() {
                                        @Override
                                        public void onClick(UsbDevice usbDevice) {
                                            UserUtils.getInstance().setVENDOR_ID(MainActivity.this, usbDevice.getVendorId());
                                            UserUtils.getInstance().setPRODUCT_ID(MainActivity.this, usbDevice.getProductId());
                                            if (usbDevice.getVendorId() == UserUtils.getInstance().getLABEKS_VENDOR_ID() && usbDevice.getProductId() == UserUtils.getInstance().getLABEKS_PRODUCT_ID()) { // 替换为实际 VID/PID
                                                UserUtils.getInstance().setLABEKS_VENDOR_ID(MainActivity.this, 0);
                                                UserUtils.getInstance().setLABEKS_PRODUCT_ID(MainActivity.this, 0);
                                            }
                                            MyUsbDeviceHelper.getInstance().requestUsbPermission(usbDevice);
                                            new DeleteShopPopupWindow(MainActivity.this, getString(R.string.setup_completed), true).show();
                                        }
                                    }).show();
                                    break;
                                /*
                                 * 标签打印机设置
                                 */
                                case 9:
                                    new PrintDevicePopupWindow(MainActivity.this, 2, new PopupWindowOnClickListener.PrintDeviceOnClickListener() {
                                        @Override
                                        public void onClick(UsbDevice usbDevice) {
                                            UserUtils.getInstance().setLABEKS_VENDOR_ID(MainActivity.this, usbDevice.getVendorId());
                                            UserUtils.getInstance().setLABEKS_PRODUCT_ID(MainActivity.this, usbDevice.getProductId());
                                            if (usbDevice.getVendorId() == UserUtils.getInstance().getVENDOR_ID() && usbDevice.getProductId() == UserUtils.getInstance().getPRODUCT_ID()) { // 替换为实际 VID/PID
                                                UserUtils.getInstance().setVENDOR_ID(MainActivity.this, 0);
                                                UserUtils.getInstance().setPRODUCT_ID(MainActivity.this, 0);
                                            }
                                            MyUsbDeviceHelper.getInstance().requestUsbPermission(usbDevice);
                                            new DeleteShopPopupWindow(MainActivity.this, getString(R.string.setup_completed), true).show();
                                        }
                                    }).show();
                                    break;
                                /*
                                 * 打折开关
                                 */
                                case 10:
                                    new DeleteShopPopupWindow( MainActivity.this, UserUtils.getInstance().isDazhe() ? getString(R.string.discounts_hint, getString(R.string.off)) : getString(R.string.discounts_hint, getString(R.string.on)), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                                        @Override
                                        public void onClick(String text) {
                                            UserUtils.getInstance().setDazhe(MainActivity.this, !UserUtils.getInstance().isDazhe());
                                            if (UserUtils.getInstance().isDazhe()) {
                                                dazhe_all_btn.setVisibility(VISIBLE);
                                                dazhe_one_btn.setVisibility(VISIBLE);
                                                caozuo_view.setVisibility(VISIBLE);
                                            } else {
                                                caozuo_view.setVisibility(GONE);
                                                dazhe_all_btn.setVisibility(GONE);
                                                dazhe_one_btn.setVisibility(GONE);
                                                if (selectedShopAdapter.getItemCount() > 0) {
                                                    for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopAdapter.getData()) {

                                                        grouponGoodsModel.setDiscount("100");

                                                        zongjia = zongjia.subtract(grouponGoodsModel.getHeji());
                                                        Log.i("ttt", ">>>>>>" + zongjia);

                                                        BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));

                                                        BigDecimal zhehoujia = yuanjia.multiply(new BigDecimal("100")).divide(new BigDecimal("100"));

                                                        grouponGoodsModel.setDiscounted_price(yuanjia.subtract(zhehoujia));

                                                        grouponGoodsModel.setHeji(zhehoujia);

                                                        zongjia = zongjia.add(zhehoujia);
                                                        tv_zongjia.setText(zongjia + "");
                                                        selectedShopAdapter.notifyDataSetChanged();
                                                        MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                                                        MyPresentation.setZongjia(zongjia.toString());

                                                    }
                                                }

                                            }
                                            selectedShopAdapter.notifyDataSetChanged();
                                        }
                                    }).show();
                                    break;
                                /*
                                 * 会员充值
                                 */
                                case 11:
                                    new MemberRechargePopupWindow(MainActivity.this, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                                        @Override
                                        public void onClick(String text) {

                                        }
                                    }).show();
                                    break;

                                /**
                                 * 商品点击设置
                                 */
                                case 12:
                                    new DeleteShopPopupWindow(true, MainActivity.this, UserUtils.getInstance().isDianji() ? getString(R.string.shangpinkaiguan_hint, getString(R.string.off)) : getString(R.string.shangpinkaiguan_hint, getString(R.string.on)), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                                        @Override
                                        public void onClick(String text) {
                                            UserUtils.getInstance().setDianji(MainActivity.this, !UserUtils.getInstance().isDianji());
                                            shop_mocheng.setVisibility(!UserUtils.getInstance().isDianji() ? VISIBLE : GONE);

                                        }
                                    }).show();
                                    break;
                                /*
                                 * 语言设置
                                 */
                                case 13:
                                    new XPopup.Builder(MainActivity.this)
                                            .asCenterList((getString(R.string.Language)+":"+(UserUtils.getInstance().getLanguage().equals("en")?"English":"中文")), new String[]{"中文", "English",},
                                                    new OnSelectListener() {
                                                        @Override
                                                        public void onSelect(int position, String text) {

                                                            if (position == 0) {
                                                                UserUtils.getInstance().setLanguage(MainActivity.this,"zh");
                                                            }
                                                            if (position == 1) {
                                                                UserUtils.getInstance().setLanguage(MainActivity.this,"en");
                                                            }

                                                            Locale locale=new Locale(UserUtils.getInstance().getLanguage());
                                                            Resources res = getResources();
                                                            Configuration config = res.getConfiguration();
                                                            if (Build.VERSION.SDK_INT >= 24) {
                                                                config.setLocale(locale);
                                                                config.setLocales(new LocaleList(locale));
                                                            } else {
                                                                config.locale = locale;
                                                            }
                                                            res.updateConfiguration(config, res.getDisplayMetrics());
                                                            recreate();

                                                        }
                                                    })
                                            .show();
                                    break;


                                /*
                                 * 新增：POS 通信设置 (case 14)
                                 */
                                case 14:
                                    new com.lxj.xpopup.XPopup.Builder(MainActivity.this)
                                            .asCustom(new com.uhm.uhmcs.popupwindow.PosSettingPopupWindow(MainActivity.this,
                                                    () -> checkPosConnection())) // 简化为 Lambda 表达式，更清爽
                                            .show();
                                    break;



                                // ==========================================
                                // 【新增】 小票样式 DIY 设置入口
                                // ==========================================
                                case 15:
                                    new com.uhm.uhmcs.popupwindow.ReceiptDiyPopupWindow(MainActivity.this).show();
                                    break;




                                default:
                                    throw new IllegalStateException("Unexpected value: " + btnType);
                            }
                        }
                    }).show();
                }
                /*
                 * 无码收银
                 */

                if (id == R.id.add_no_code_btn) {
                    new AddNoCodePopupWindow(MainActivity.this, new PopupWindowOnClickListener.AddNoCodeOnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(GrouponGoodsBean.GrouponGoodsModel grouponGoodsMode) {
                            BigDecimal price = new BigDecimal(grouponGoodsMode.getPrice());
                            if (!TextUtils.isEmpty(memben_discount)) {
                                price = price.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal(100));
                                grouponGoodsMode.setDiscounted_price(new BigDecimal(grouponGoodsMode.getPrice()).subtract(price));
                                grouponGoodsMode.setDiscount(memben_discount);
                            }

                            BigDecimal heji = price.setScale(2, RoundingMode.DOWN);
                            grouponGoodsMode.setHeji(heji);
                            grouponGoodsMode.setShuliang(1);
                            selectedShopList.add(0, SerializableUtils.deepCopy(grouponGoodsMode));

                            have_paid_view.setVisibility(GONE);
                            selectedShopAdapter.setNewData(selectedShopList);
                            allNum++;
                            MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);

                            // 滚动到位置 0（第一条）
                            selected_LinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
                            tv_zongjian.setText(allNum + "");
                            zongjia = zongjia.add(price).setScale(2, RoundingMode.DOWN);
                            tv_zongjia.setText(zongjia + "");
                            MyPresentation.setZongjia(zongjia.toString());
                            availableAmount();
                        }
                    }).show();
                    updatePlaceholderVisibility();

                }


            }
        };
        shop_image = findViewById(R.id.shop_image);
        caozuo_view = findViewById(R.id.caozuo_view);
        shop_mocheng = findViewById(R.id.shop_mocheng);
        shop_mocheng.setOnClickListener(v -> {

        });

        TextView tv_main_shop_name = findViewById(R.id.tv_main_shop_name);
        TextView tv_nickname = findViewById(R.id.tv_nickname);
        tv_main_shop_name.setText(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname());
        tv_nickname.setText(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getMobile());

        pingText = findViewById(R.id.pingValue);
        httpText = findViewById(R.id.httpValue);

        // 绑定 POS 状态 UI
        v_pos_status_light = findViewById(R.id.v_pos_status_light);
        tv_pos_status_text = findViewById(R.id.tv_pos_status_text);

        // 默认设为POS灰色状态
        if (v_pos_status_light != null) {
            v_pos_status_light.setBackgroundResource(R.drawable.shape_circle_gray);
        }

        monitor = new NetworkLatencyMonitor();
        monitor.startMonitoring((pingMs, httpMs) -> {
            runOnUiThread(() -> {
                pingText.setText(String.valueOf(pingMs));
                httpText.setText(String.valueOf(httpMs));
                updateColor(pingText, pingMs);
                updateColor(httpText, httpMs);
            });
        });


        // --- 称重 UI 绑定平移 ---
        weight_id = findViewById(R.id.tv_real_weight); // 实时重量
        tv_tareWeight = findViewById(R.id.tv_tare_weight); // 皮重
        View btn_qupi = findViewById(R.id.btn_qupi); // 去皮按钮

        btn_qupi.setOnClickListener(v -> {
            if (isScaleConnected) {
                // 情况 A：已连接，执行去皮
                if (m_scaler != null && m_weight != null && m_weight.isStable) {
                    m_scaler.AclasTare();
                } else if (m_weight != null && !m_weight.isStable) {
                    Toast.makeText(MainActivity.this, "重量不稳定", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 情况 B：未连接，启动扫描
                Log.i("ScaleScan", "用户手动触发秤盘重连...");
                Toast.makeText(MainActivity.this, "正在搜索秤盘，可能需要20秒，请稍候...", Toast.LENGTH_SHORT).show();
                OpenScale(); // 调用扫描方法
            }
        });



        shop_mocheng.setVisibility(!UserUtils.getInstance().isDianji() ? VISIBLE : GONE);
        bendin_view = findViewById(R.id.bendin_view);
        wangluo_view = findViewById(R.id.wangluo_view);
        ImageView imageView = findViewById(R.id.image);
        // 加载本地资源
        Glide.with(this).load(R.drawable.have_paid_img).into(imageView);
        have_paid_view = findViewById(R.id.have_paid_view);
        findViewById(R.id.delete_shop).setOnClickListener(onClickListener);
        findViewById(R.id.shuaxin_btn).setOnClickListener(onClickListener);
        guadan_btn = findViewById(R.id.guadan_btn);
        guadan_btn.setOnClickListener(onClickListener);
        qingkong_btn = findViewById(R.id.qingkong_btn);
        qingkong_btn.setOnClickListener(onClickListener);
        qudan_btn = findViewById(R.id.qudan_btn);
        qudan_btn.setOnClickListener(onClickListener);
        dazhe_one_btn = findViewById(R.id.dazhe_one_btn);
        dazhe_one_btn.setOnClickListener(onClickListener);
        dazhe_all_btn = findViewById(R.id.dazhe_all_btn);
        dazhe_all_btn.setOnClickListener(onClickListener);
        checkout_btn = findViewById(R.id.checkout_btn);
        checkout_btn.setOnClickListener(onClickListener);

        huiyuan_btn = findViewById(R.id.huiyuan_btn);
        huiyuan_btn.setOnClickListener(onClickListener);
        daying_btn = findViewById(R.id.daying_btn);
        daying_btn.setOnClickListener(onClickListener);
        huiyuan_name = findViewById(R.id.huiyuan_name);
        shanchuhuiyuan_btn = findViewById(R.id.shanchuhuiyuan_btn);
        shanchuhuiyuan_btn.setOnClickListener(onClickListener);
        findViewById(R.id.more_function_btn).setOnClickListener(onClickListener);
        findViewById(R.id.add_no_code_btn).setOnClickListener(onClickListener);

        if (UserUtils.getInstance().isDazhe()) {
            dazhe_all_btn.setVisibility(VISIBLE);
            dazhe_one_btn.setVisibility(VISIBLE);
            caozuo_view.setVisibility(VISIBLE);
        } else {
            caozuo_view.setVisibility(GONE);
            dazhe_all_btn.setVisibility(GONE);
            dazhe_one_btn.setVisibility(GONE);
        }


        tv_zongjia = findViewById(R.id.tv_zongjia);
        tv_zongjian = findViewById(R.id.tv_zongjian);

        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_click);


        et_tiaoxingma = findViewById(R.id.et_tiaoxingma);
        et_tiaoxingma.setOnClickListener(v -> et_tiaoxingma.postDelayed(() -> et_tiaoxingma.requestFocus(), 100));
        buildBean = DialogUIUtils.showLoading(this, getString(R.string.paying), true, true, false, false);
        // 设置输入完成监听




// 在 initView() 里的 et_tiaoxingma 初始化部分
        et_tiaoxingma.setOnInputCompleteListener(text -> {
            Log.i("ScanDebug", "收到扫码内容: " + text);
            et_tiaoxingma.setText("");
            if (TextUtils.isEmpty(text)) return;

            zhifuxinxi_view.setVisibility(GONE);

            // 1. 判断是否为支付码
            String textType = detectPaymentType(text);
            if (!textType.equals("unknown")) {
                // ... (这里保留你原本的支付处理逻辑) ...
                return;
            }

            // 2. 数据库查询逻辑
            dbExecutor.execute(() -> {
                // 在 dbExecutor 线程里的那一行修改为：
                List<GrouponGoodsBean.GrouponGoodsModel> dbResults = org.litepal.LitePal
                        .where("sn = ? or serverId = ? or goods_sn = ? or ggspid = ?", text, text, text, text)
                        .find(GrouponGoodsBean.GrouponGoodsModel.class);

                if (!dbResults.isEmpty()) {
                    runOnUiThread(() -> handleScanSuccess(dbResults.get(0), null, null));
                } else {
                    // 匹配复合码正则
                    String PATTERN = "^\\d{5}\\d{3}.+$";
                    if (text.matches(PATTERN)) {
                        try {
                            String weightStr = Integer.parseInt(text.substring(0, 5)) + "";
                            String discountStr = Integer.parseInt(text.substring(5, 8)) + "";
                            String productId = text.substring(8);

                            List<GrouponGoodsBean.GrouponGoodsModel> specialResults = org.litepal.LitePal
                                    .where("serverId = ? or ggspid = ?", productId, productId)
                                    .find(GrouponGoodsBean.GrouponGoodsModel.class);

                            if (!specialResults.isEmpty()) {
                                runOnUiThread(() -> handleScanSuccess(specialResults.get(0), weightStr, discountStr));
                            } else {
                                runOnUiThread(() -> handleScanError());
                            }
                        } catch (Exception e) {
                            runOnUiThread(() -> handleScanError());
                        }
                    } else {
                        runOnUiThread(() -> handleScanError());
                    }
                }
            });
        }); // <--- 注意：在这里就结束了，后面不准再写方法定义





        // 自动获取焦点
        et_tiaoxingma.postDelayed(() -> et_tiaoxingma.requestFocus(), 100);


        //=================================选中购买的商品start=========================================//
        selected_shop_rv = findViewById(R.id.selected_shop_rv);
        selected_LinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        selected_shop_rv.setLayoutManager(selected_LinearLayoutManager);
        selectedShopAdapter = new SelectedShopAdapter(this, R.layout.item_selected_shop);
        selected_shop_rv.setAdapter(selectedShopAdapter);
        ((SimpleItemAnimator) selected_shop_rv.getItemAnimator()).setSupportsChangeAnimations(false);

        selectedShopAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopAdapter.getData()) {
                    grouponGoodsModel.setSelected(false);
                }
                selectedShopAdapter.getData().get(position).setSelected(true);
                selectedShopIndex = position;
                selectedShopAdapter.notifyDataSetChanged();
                Glide.with(MainActivity.this).clear(shop_image);  // 先清空ImageView
                Glide.with(MainActivity.this).load(selectedShopAdapter.getData().get(position).getImage()).into(shop_image);  // 再加载新图片

            }
        });


        selectedShopAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopAdapter.getData()) {
                    grouponGoodsModel.setSelected(false);
                }
                selectedShopAdapter.getData().get(position).setSelected(true);
                selectedShopIndex = position;
                selectedShopAdapter.notifyDataSetChanged();
                Glide.with(MainActivity.this).clear(shop_image);  // 先清空ImageView
                Glide.with(MainActivity.this).load(selectedShopAdapter.getData().get(position).getImage()).into(shop_image);  // 再加载新图片
                int id = view.getId();
                GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = selectedShopAdapter.getData().get(position);
                if (id == R.id.shuliang_jian) {
                    allNum--;
                    if (grouponGoodsModel.getShuliang() > 1) {
                        grouponGoodsModel.setShuliang(grouponGoodsModel.getShuliang() - 1);

                        BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                        if (!TextUtils.isEmpty(grouponGoodsModel.getDiscount())) {
                            price = price.multiply(new BigDecimal(grouponGoodsModel.getDiscount())).divide(new BigDecimal("100"));
                            grouponGoodsModel.setDiscounted_price(grouponGoodsModel.getDiscounted_price().subtract(new BigDecimal(grouponGoodsModel.getPrice()).subtract(price)));
                        }


                        BigDecimal heji = grouponGoodsModel.getHeji().subtract(price).setScale(2, RoundingMode.DOWN);
                        grouponGoodsModel.setHeji(heji);
                        if (!grouponGoodsModel.isIs_zengsong()) {
                            zongjia = zongjia.subtract(price).setScale(2, RoundingMode.DOWN);
                        }

                    } else {
                        deleteShopPopupWindow = new DeleteShopPopupWindow(MainActivity.this, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onClick(String text) {
                                if (!grouponGoodsModel.isIs_zengsong()) {
                                    BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                                    if (!TextUtils.isEmpty(grouponGoodsModel.getDiscount())) {
                                        price = price.multiply(new BigDecimal(grouponGoodsModel.getDiscount())).divide(new BigDecimal("100"));
                                        grouponGoodsModel.setDiscounted_price(grouponGoodsModel.getDiscounted_price().subtract(new BigDecimal(grouponGoodsModel.getPrice()).subtract(price)));
                                    }

                                    zongjia = zongjia.subtract(price).setScale(2, RoundingMode.DOWN);
                                    tv_zongjia.setText(zongjia + "");
                                    MyPresentation.setZongjia(zongjia.toString());
                                }
                                selectedShopList.remove(position);
                                selectedShopAdapter.setNewData(selectedShopList);
                                tv_zongjian.setText(allNum + "");
                                MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                                if (selectedShopAdapter.getItemCount() > 0) {
                                    availableAmount();
                                }
                                selectedShopIndex = null;

                                // ★★★ 核心修复：在这里也要调用！ ★★★
                                updatePlaceholderVisibility();

                            }
                        });
                        deleteShopPopupWindow.show();
                        return;
                    }

                } else if (id == R.id.shuliang_jia) {
                    allNum++;
                    grouponGoodsModel.setShuliang(grouponGoodsModel.getShuliang() + 1);

                    BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                    if (!TextUtils.isEmpty(grouponGoodsModel.getDiscount())) {
                        price = price.multiply(new BigDecimal(grouponGoodsModel.getDiscount())).divide(new BigDecimal("100"));
                        grouponGoodsModel.setDiscounted_price(grouponGoodsModel.getDiscounted_price().add(new BigDecimal(grouponGoodsModel.getPrice()).subtract(price)));
                    }

                    BigDecimal heji = grouponGoodsModel.getHeji().add(price).setScale(2, RoundingMode.DOWN);
                    grouponGoodsModel.setHeji(heji);
                    if (!grouponGoodsModel.isIs_zengsong()) {
                        zongjia = zongjia.add(price).setScale(2, RoundingMode.DOWN);
                    }

                } else if (id == R.id.zengsong) {
                    BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));
                    if (!grouponGoodsModel.isIs_zengsong()) {
                        grouponGoodsModel.setIs_zengsong(true);
                        grouponGoodsModel.setDiscount("0");
                        grouponGoodsModel.setDiscounted_price(yuanjia);
                        zongjia = zongjia.subtract(grouponGoodsModel.getHeji()).setScale(2, RoundingMode.DOWN);
                        grouponGoodsModel.setHeji(new BigDecimal("0.00"));

                    } else {
                        grouponGoodsModel.setDiscount("100");
                        grouponGoodsModel.setIs_zengsong(false);
                        grouponGoodsModel.setDiscounted_price(new BigDecimal("0.00"));
                        grouponGoodsModel.setHeji(yuanjia);
                        zongjia = zongjia.add(yuanjia).setScale(2, RoundingMode.DOWN);
                    }

                }
                tv_zongjia.setText(zongjia + "");
                tv_zongjian.setText(allNum + "");
                MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                MyPresentation.setZongjia(zongjia.toString());
                selectedShopAdapter.notifyItemChanged(position);
                if (selectedShopAdapter.getItemCount() > 0) {
                    availableAmount();
                }
            }
        });


        //=================================选中购买的商品end=========================================//


        //=================================商品类型start=========================================//
        rv_choose_menu3 = findViewById(R.id.rv_choose_menu3);
        rv_choose_menu3.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        shopTypeAdapter = new ShopTypeAdapter(this, R.layout.item_shop_type);
        rv_choose_menu3.setAdapter(shopTypeAdapter);


        shopTypeAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                // 1. 设置选中变色
                shopTypeAdapter.setIndex(position);

                // 2. 获取点击的分类 ID
                CategoryListBean.CategoryListModel item = shopTypeAdapter.getData().get(position);
                category_ids = (item == null || TextUtils.isEmpty(item.getId())) ? "" : item.getId();

                Log.i("ttt", "切换分类: " + (item != null ? item.getName() : "全部") + " ID: " + category_ids);

                // 3. 【关键】不再使用 stream().filter()，而是直接从数据库重新加载
                loadLocalGoods(0);
            }
        });


        //=================================商品类型endt=========================================//

        //=================================商品列表start=========================================//
        shop_rv = findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new GridLayoutManager(this, 2)); // 设置3列，横向布局，不反转方向（false）
        grouponGoodsAdapter = new GrouponGoodsAdapter(this, R.layout.item_groupon_goods);
        shop_rv.setAdapter(grouponGoodsAdapter);
//        grouponGoodsAdapter.setOnLoadMoreListener(() -> {
//
//        }, shop_rv);
        grouponGoodsAdapter.setPreLoadNumber(3);


        shop_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!grouponGoodsAdapter.hasMore) return;

                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null) {
                    int visibleItemCount = layoutManager.getChildCount();
                    int totalItemCount = layoutManager.getItemCount();
                    int firstVisibleItem = layoutManager.findFirstVisibleItemPosition();

                    // 如果滑动到倒数第5个条目，就开始加载下一页
                    if ((visibleItemCount + firstVisibleItem) >= totalItemCount - 5) {
                        // 计算下一页的 offset (当前列表数 / 20)
                        int nextPage = totalItemCount / 20;
                        loadLocalGoods(nextPage);
                    }
                }
            }
        });



        grouponGoodsAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                // 1. 播放动画与音效
                view.startAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_click));
                MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.shaoma);
                mp.setOnCompletionListener(MediaPlayer::release);
                mp.start();

                // 2. 获取原始数据并深拷贝
                GrouponGoodsBean.GrouponGoodsModel originalItem = (GrouponGoodsBean.GrouponGoodsModel) adapter.getItem(position);
                if (originalItem == null) return;
                GrouponGoodsBean.GrouponGoodsModel model = SerializableUtils.deepCopy(originalItem);

                // 3. UI 状态切换
                zhifuxinxi_view.setVisibility(GONE);
                have_paid_view.setVisibility(GONE);
                Glide.with(MainActivity.this).load(model.getImage()).into(shop_image);

                // ========================== 核心：重新加入称重逻辑 ==========================
                if ("weight".equals(model.getOnline_type())) {
                    // 校验秤的状态
                    if (m_weight == null || m_weight.netWeight <= 0) {
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.qfrsp), true).show();
                        return;
                    }
                    if (!m_weight.isStable) {
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.zlbwd), true).show();
                        return;
                    }

                    // 重量换算：kg -> g (因为收银逻辑是按500g/一斤算的)
                    BigDecimal weightG = new BigDecimal(String.valueOf(m_weight.netWeight))
                            .multiply(new BigDecimal("1000")).setScale(0, RoundingMode.DOWN);

                    // 金额计算：(单价 / 500) * 重量
                    BigDecimal pricePerUnit = new BigDecimal(model.getPrice());
                    BigDecimal heji = pricePerUnit.divide(new BigDecimal("500"), 4, RoundingMode.HALF_UP)
                            .multiply(weightG).setScale(2, RoundingMode.DOWN);

                    model.setGoods_weight(weightG.toString());
                    model.setShuliang(1); // 称重品计件为1

                    // 处理会员折扣
                    if (!TextUtils.isEmpty(memben_discount) && !memben_discount.equals("100")) {
                        BigDecimal zhehou = heji.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
                        model.setDiscounted_price(heji.subtract(zhehou));
                        model.setDiscount(memben_discount);
                        heji = zhehou;
                    }
                    model.setHeji(heji);

                    // 称重品直接插入购物车首位，不进行合并
                    selectedShopList.add(0, model);
                    zongjia = zongjia.add(heji);
                    allNum++;

                } else {
                    // ========================== 普通计件商品逻辑 ==========================
                    boolean isExists = false;
                    for (int i = 0; i < selectedShopList.size(); i++) {
                        GrouponGoodsBean.GrouponGoodsModel cartItem = selectedShopList.get(i);
                        // 修正：使用 getIds() 匹配服务器 ID
                        if (cartItem.getIds().equals(model.getIds()) && cartItem.getGgspid().equals(model.getGgspid())) {
                            cartItem.setShuliang(cartItem.getShuliang() + 1);
                            BigDecimal price = new BigDecimal(cartItem.getPrice());
                            if (!TextUtils.isEmpty(cartItem.getDiscount())) {
                                price = price.multiply(new BigDecimal(cartItem.getDiscount())).divide(new BigDecimal(100));
                            }
                            cartItem.setHeji(cartItem.getHeji().add(price).setScale(2, RoundingMode.DOWN));
                            zongjia = zongjia.add(price);
                            selectedShopAdapter.notifyItemChanged(i);
                            isExists = true;
                            break;
                        }
                    }

                    if (!isExists) {
                        model.setShuliang(1);
                        model.setGoods_weight("0");
                        BigDecimal price = new BigDecimal(model.getPrice());
                        if (!TextUtils.isEmpty(memben_discount)) {
                            BigDecimal zhehou = price.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal(100));
                            model.setDiscounted_price(price.subtract(zhehou));
                            model.setDiscount(memben_discount);
                            price = zhehou;
                        }
                        model.setHeji(price.setScale(2, RoundingMode.DOWN));
                        selectedShopList.add(0, model);
                        zongjia = zongjia.add(price);
                    }
                    allNum++;
                }

                // 4. 统一刷新 UI 和客显屏
                selectedShopAdapter.setNewData(selectedShopList);
                selected_LinearLayoutManager.scrollToPosition(0);
                tv_zongjia.setText(zongjia.setScale(2, RoundingMode.DOWN).toString());
                tv_zongjian.setText(String.valueOf(allNum));
                MyPresentation.setShopArrayList(selectedShopList, allNum);
                MyPresentation.setZongjia(zongjia.toString());
                availableAmount();


                updatePlaceholderVisibility();


            }



        });



        // --- 搜索框逻辑开始 ---

        if (et_search_pinyin != null) {
            // 1. 触摸监听：处理“叉叉”图标点击
            et_search_pinyin.setOnTouchListener((v, event) -> {
                android.graphics.drawable.Drawable drawableRight = et_search_pinyin.getCompoundDrawables()[2];
                if (drawableRight != null && event.getAction() == android.view.MotionEvent.ACTION_UP) {
                    // 计算点击区域是否在叉叉图标上
                    boolean isClickClear = event.getX() >= (et_search_pinyin.getWidth() - et_search_pinyin.getPaddingRight() - drawableRight.getIntrinsicWidth() - 50);
                    if (isClickClear) {
                        et_search_pinyin.setText("");
                        hideKeyboard();              // ★ 收起键盘
                        et_tiaoxingma.requestFocus(); // ★ 焦点归还扫码框
                        return true;
                    }
                }
                return false;
            });

            // 2. 软键盘右下角“搜索/完成”按钮监听
            et_search_pinyin.setOnEditorActionListener((v, actionId, event) -> {
                // 当点击软键盘上的搜索图标时
                if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH ||
                        actionId == android.view.inputmethod.EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();                  // ★ 收起键盘
                    et_tiaoxingma.requestFocus();     // ★ 焦点归还扫码框
                    return true;
                }
                return false;
            });

            // 3. 物理键盘/扫码枪回车监听
            et_search_pinyin.setOnKeyListener((v, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {
                    hideKeyboard();                  // ★ 收起键盘
                    et_tiaoxingma.requestFocus();     // ★ 焦点归还扫码框
                    return true;
                }
                return false;
            });

            // 4. 文字变化监听
            et_search_pinyin.addTextChangedListener(new android.text.TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String keyword = s.toString().trim();
                    // 动态控制叉叉图标显隐 (确保 R.drawable.guanbi 存在)
                    et_search_pinyin.setCompoundDrawablesWithIntrinsicBounds(0, 0, keyword.length() > 0 ? R.drawable.guanbi : 0, 0);
                    searchLocalGoods(keyword);
                }
                @Override public void afterTextChanged(android.text.Editable s) {}
            });
        }

// --- 搜索框逻辑结束 ---








        time = new TimeCount(30000, 5000);//一共执行30000毫秒，每2000执行一次。


    }





    private void checkPosConnection() {
        String ip = UserUtils.getInstance().getEcrIp();
        int port = UserUtils.getInstance().getEcrPort();

        dbExecutor.execute(() -> {
            // 发送 55 指令检查状态
            Map<String, String> result = ecrSocketManager.executeCommand(ip, port, EcrSocketManager.statusCommand());
            String respCode = result.get("ResponseCode");

            runOnUiThread(() -> {
                if ("00".equals(respCode)) {
                    // 只有返回 00 才是真正的就绪状态
                    v_pos_status_light.setBackgroundResource(R.drawable.shape_circle_green);
                    tv_pos_status_text.setText("NETS 就绪");
                    tv_pos_status_text.setTextColor(0xFF00AB1D);
                } else {
                    // 通信失败或状态非 00（如未 Logon）
                    v_pos_status_light.setBackgroundResource(R.drawable.shape_circle_red);
                    tv_pos_status_text.setText("POS 未就绪(" + (respCode != null ? respCode : "ERR") + ")");
                    tv_pos_status_text.setTextColor(0xFFEF4444);
                }
            });
        });
    }




    CheckoutBean checkoutBean;


    public void loadNextPage() {
        if (!grouponGoodsAdapter.hasMore) return;
        grouponGoods_page++;
        ArrayList<GrouponGoodsBean.GrouponGoodsModel> pageData = getPageData(grouponGoods_page, indexGrouponGoodsModelList);
        if (pageData.isEmpty()) {
            Log.i("ttt", ">>>>loadNextPage>>>>>>");

            grouponGoodsAdapter.hasMore = false;
            return;
        }

        shop_rv.post(() -> {
            // 请求成功后，更新数据并通知适配器数据已更改
            grouponGoodsAdapter.loadMoreData(pageData); // newDataList 是新加载的数据列表
            // 或执行 add/remove 操作
        });
    }

    public ArrayList<GrouponGoodsBean.GrouponGoodsModel> getPageData(int currentPage, ArrayList<GrouponGoodsBean.GrouponGoodsModel> sourceList) {
        int start = (currentPage - 1) * 20;
        int end = Math.min(start + 20, sourceList.size());
        if (start >= end) return new ArrayList<>();
        return new ArrayList<>(sourceList.subList(start, end)); // 避免直接使用 subList
    }

    private void updateColor(TextView view, int latency) {
        if (latency < 0) {
            view.setTextColor(0xFFFF0000); // 红色表示错误
        } else if (latency < 100) {
            view.setTextColor(0xFF4CAF50); // 绿色表示优秀
        } else if (latency < 300) {
            view.setTextColor(0xFFFFC107); // 黄色表示一般
        } else {
            view.setTextColor(0xFFF44336); // 红色表示较差
        }
    }


    /**
     * 判断支付类型
     *
     * @param code 扫码获取的字符串
     * @return "alipay"（支付宝）、"wechat"（微信）、"unknown"（未知）
     */
    public static String detectPaymentType(String code) {
        if (TextUtils.isEmpty(code)) return "unknown";

        // 检查是否为纯数字
        if (!code.matches("\\d+")) return "unknown";

        // 微信规则验证
        if (code.length() == 18 && code.matches("^(10|11|12|13|14|15)\\d{16}$")) {
            return "wechat";
        }

        // 支付宝规则验证
        if (code.length() >= 16 && code.length() <= 24
                && code.matches("^(25|26|27|28|29|30)\\d+")) {
            return "alipay";
        }

        return "unknown";
    }

    String Coupon_fee = "0";

    public void availableAmount() {
        if (memberBean1 == null) {
            return;
        }
        ArrayList<CheckoutBean.GoodsJsonBean> goodsJsonBeanArrayList = new ArrayList<>();
        for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopList) {
            CheckoutBean.GoodsJsonBean goodsJsonBean = new CheckoutBean.GoodsJsonBean();
            goodsJsonBean.setGoods_id(grouponGoodsModel.getIds());
            goodsJsonBean.setTitle(grouponGoodsModel.getTitle());
            goodsJsonBean.setGoods_sn(grouponGoodsModel.getGoods_sn());
            goodsJsonBean.setSn(grouponGoodsModel.getSn());
            goodsJsonBean.setDiscount(TextUtils.isEmpty(grouponGoodsModel.getDiscount()) ? "100" : grouponGoodsModel.getDiscount());
            goodsJsonBean.setDiscounted_price(grouponGoodsModel.getDiscounted_price() == null ? "0.00" : grouponGoodsModel.getDiscounted_price().toString());
            goodsJsonBean.setGoods_price(grouponGoodsModel.getPrice());
            goodsJsonBean.setGoods_num(grouponGoodsModel.getShuliang());
            goodsJsonBean.setGoods_weight(grouponGoodsModel.getGoods_weight());
            goodsJsonBean.setPay_price(grouponGoodsModel.getHeji().toString());
            goodsJsonBean.setGoods_sku_price_id(grouponGoodsModel.getGgspid() + "");
//            goodsJsonBean.setGoods_sku_text(TextUtils.isEmpty(grouponGoodsModel.getGoods_sku_text()) ? "" : grouponGoodsModel.getGoods_sku_text());


            goodsJsonBeanArrayList.add(goodsJsonBean);
        }
        Gson gson = new Gson();

        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        params.put("mobile", memberBean1.getMobile());
        params.put("order_item", gson.toJson(goodsJsonBeanArrayList));
        String url = POSApiSerview.POS_URL + POSApiSerview.availableAmount;
        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isEmpty()) {

                            JSONObject jsonObject = null;
                            try {
                                jsonObject = new JSONObject(response);
                                int code = jsonObject.getInt("code");
                                if (code == 1) {
                                    Coupon_fee = jsonObject.getString("data");
                                }

                            } catch (JSONException e) {
                                Log.e("ttt", "Error occurred", e);
                            }


                        } else {
                            Toast.makeText(MainActivity.this, "请求错误，结果为空", LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {

                    System.err.println("请求失败: " + e.getMessage());
                });

            }
        });
    }


    public void SubmitCheckout(CheckoutBean checkoutBean) {
        if (Utilis.isFastClick()) {
            return;
        }

        // ======= 【关键修复：动态抓取收银员姓名】 =======
        try {
            if (UserUtils.getInstance().getLoginBase() != null) {
                String realName = UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
                if (!TextUtils.isEmpty(realName)) {
                    // 将真实姓名塞进字段，打印机就会打印这个姓名
                    checkoutBean.setMachineNumber(realName);
                }
            }
        } catch (Exception e) {
            Log.e("CashierError", "获取收银员失败: " + e.getMessage());
        }
        // =============================================


        buildBean.show();
        String url = POSApiSerview.POS_URL + POSApiSerview.addOrder;
        Gson gson = new Gson();
        RequestBody body = RequestBody.create(gson.toJson(checkoutBean), MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(body);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected), true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        JSONObject jsonObject = new JSONObject(success);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (jsonObject.getString("msg").contains("成功") || jsonObject.getString("msg").contains("Success")) {
                                        if (checkoutBean.getPay_type().equals("cash")) {
                                            order_sn = jsonObject.getString("data");
                                        } else if (checkoutBean.getPay_type().equals("wechat")) {
                                            order_sn = new JSONObject(jsonObject.getString("code")).getString("order_sn");
                                        } else if (checkoutBean.getPay_type().equals("alipay")) {
                                            order_sn = jsonObject.getString("order_sn");
                                        }
// 初始化MediaPlayer
                                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.yidong);
                                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                        mediaPlayer.setOnCompletionListener(mp -> mp.release());
//                    mediaPlayer.pause();  // 暂停
//                    mediaPlayer.stop();   // 停止(需重新prepare)

                                        // 播放控制
                                        mediaPlayer.start();  // 开始播放
                                        DialogUIUtils.dismiss(buildBean);
                                        have_paid_view.setVisibility(VISIBLE);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                have_paid_view.setVisibility(GONE);
                                            }
                                        }, 1000);
                                        onClickListener.onClick(qingkong_btn);
                                        onClickListener.onClick(shanchuhuiyuan_btn);
                                        String weixin_pice = checkoutBean.getPay_type().equals("wechat") ? checkoutBean.getPay_fee() : "";
                                        String zhifubao_pice = checkoutBean.getPay_type().equals("alipay") ? checkoutBean.getPay_fee() : "";

// ================== 【替换】使用 DIY 指令极速打印 ==================
                                        byte[] printCmds = com.uhm.uhmcs.utils.ReceiptCommandUtils.getReceiptCommands(MainActivity.this, checkoutBean, order_sn);
                                        MyPrinterHelper.getInstance().printCommand(MainActivity.this, printCmds);
// ================================================================
                                        order_sn = "";
                                        out_trade_no = "";
                                        return;
                                    }
                                    if (jsonObject.getString("msg").contains("失效")) {
                                        DialogUIUtils.dismiss(buildBean);
                                        new DeleteShopPopupWindow(MainActivity.this, true, jsonObject.getString("msg"), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                                            @Override
                                            public void onClick(String text) {
                                                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                                                startActivity(intent);
                                            }
                                        }).show();
                                        return;
                                    }
                                    if (jsonObject.getString("msg").contains("密码") || jsonObject.getString("msg").contains("order success pay inprocess")) {
                                        if (checkoutBean.getPay_type().equals("wechat")) {
                                            order_sn = new JSONObject(jsonObject.getString("code")).getString("order_sn");
                                            if (new JSONObject(jsonObject.getString("code")).has("out_trade_no")) {
                                                out_trade_no = new JSONObject(jsonObject.getString("code")).getString("out_trade_no");
                                            } else {
                                                DialogUIUtils.dismiss(buildBean);
                                                new DeleteShopPopupWindow(MainActivity.this, getString(R.string.No_transaction_ID_recorded), true).show();
                                                return;
                                            }


                                            fwsgetOrderInformation(checkoutBean);
                                        } else if (checkoutBean.getPay_type().equals("alipay")) {
                                            out_trade_no = jsonObject.getString("out_trade_no");
                                            order_sn = jsonObject.getString("order_sn");

                                            queryOrder(checkoutBean);
                                            time.start();
                                        }
                                        return;

                                    }
                                    DialogUIUtils.dismiss(buildBean);
                                    new DeleteShopPopupWindow(MainActivity.this, getString(R.string.Payment_failed) + jsonObject.getString("msg"), true).show();


                                } catch (JSONException e) {
                                    Log.e("ttt", "Error occurred", e);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }

    public void queryOrder(CheckoutBean checkoutBean) {
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.queryOrder;
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(formBody);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected), true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject = new JSONObject(success);

                                    String trade_status = jsonObject.getString("trade_status");
                                    if (is_chaoshi && !trade_status.contains("TRADE_FINISHED") && !trade_status.contains("TRADE_SUCCESS")) {
                                        is_chaoshi = false;
                                        revokeOrder(checkoutBean);
                                        return;
                                    }
                                    if (trade_status.contains("TRADE_FINISHED") || trade_status.contains("TRADE_SUCCESS")) {
                                        time.cancel();
                                        transaction_id = jsonObject.getString("trade_no");
                                        checkoutBean.setTransaction_id(transaction_id);
                                        checkoutBean.setOrder_sn(order_sn);
                                        pushorders(checkoutBean);

                                    }
                                    if (trade_status.contains("TRADE_CLOSED")) {
                                        order_sn = "";
                                        out_trade_no = "";
                                        DialogUIUtils.dismiss(buildBean);
                                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.order_canceled), true).show();
                                    }


                                } catch (JSONException e) {
                                    Log.e("ttt", "Error occurred", e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }

    private TimeCount time;
    private boolean is_chaoshi = false;

    class TimeCount extends CountDownTimer {


        public TimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        //时间定时器运行过程调用此方法。millisUntilFinished   为剩余时间
        @Override
        public void onTick(long millisUntilFinished) {

            queryOrder(checkoutBean);

        }

        //时间定时器结束调用此方法
        @Override
        public void onFinish() {
            is_chaoshi = true;
            queryOrder(checkoutBean);
        }
    }

    public void revokeOrder(CheckoutBean checkoutBean) {
        Map<String, String> params = new HashMap<>();
        params.put("out_trade_no", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.revokeOrder;
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(formBody);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected), true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                queryOrder(checkoutBean);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }

    //tab货品类型list
    /**
     * 获取商品分类列表
     * 1. 联网更新分类 UI
     * 2. 自动插入“全部”选项
     * 3. 移除对旧同步方法的调用，实现新旧逻辑彻底分离
     */
    public void OverviewList() {
        // 1. 准备请求参数
        Map<String, String> params = new HashMap<>();
        try {
            params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        } catch (Exception e) {
            Log.e("ttt", "OverviewList: 获取店铺ID失败");
            return;
        }

        String url = POSApiSerview.POS_URL + POSApiSerview.getGrouponCategory;

        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                // 注意：这里不再打印 response 详情，防止日志刷屏
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (isFinishing() || isDestroyed()) return;

                        if (!TextUtils.isEmpty(response)) {
                            try {
                                Gson gson = new Gson();
                                CategoryListBean categoryListBean = gson.fromJson(response, CategoryListBean.class);

                                if (categoryListBean != null && categoryListBean.getCode() == 1) {
                                    ArrayList<CategoryListBean.CategoryListModel> models = categoryListBean.getData();
                                    if (models == null) models = new ArrayList<>();

                                    // --- 核心改进：逻辑解耦 ---
                                    // 彻底删除对 getGrouponGoods() 的调用，不在这里触发商品同步

                                    // 2. 构造并插入“全部”分类选项
                                    CategoryListBean.CategoryListModel allCategory = new CategoryListBean.CategoryListModel();
                                    allCategory.setName(getString(R.string.all));
                                    allCategory.setId(""); // ID 为空代表查询所有分类

                                    models.add(0, allCategory);

                                    // 3. 更新内存中的分类数据结构并保存缓存
                                    categoryListBean.setData(models);
                                    UserUtils.getInstance().setCategoryListBeanJson(MainActivity.this, gson.toJson(categoryListBean));

                                    // 4. 刷新分类适配器 UI
                                    shopTypeAdapter.setIndex(0);
                                    shopTypeAdapter.setNewData(models);

                                    Log.i("ttt", "分类列表加载成功，共 " + models.size() + " 个分类");
                                }
                            } catch (Exception e) {
                                Log.e("ttt", "分类解析异常: " + e.getMessage());
                            }
                        } else {
                            Toast.makeText(MainActivity.this, "获取分类失败：返回结果为空", LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;

                    // 5. 联网失败时，尝试从本地缓存加载分类
                    String cacheJson = UserUtils.getInstance().getCategoryListBeanJson();
                    if (!TextUtils.isEmpty(cacheJson)) {
                        try {
                            Gson gson = new Gson();
                            CategoryListBean cachedBean = gson.fromJson(cacheJson, CategoryListBean.class);
                            if (cachedBean != null && cachedBean.getData() != null) {
                                shopTypeAdapter.setNewData(cachedBean.getData());
                                Log.i("ttt", "离线状态：已加载本地缓存分类");
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    Log.e("ttt", "分类请求失败: " + e.getMessage());
                });
            }
        });
    }




    private String goods_sn = "";
    private String category_ids = "";
    private int grouponGoods_page = 1;
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> allGrouponGoodsModelList = new ArrayList<>();
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> indexGrouponGoodsModelList = new ArrayList<>();





    /**
     * 步骤 A: 启动同步流程
     */
    private void syncGoodsData() {
        is_tongbu = true;
        dbExecutor.execute(() -> {
            // 1. 同步前清空旧数据，防止新旧数据混杂
            org.litepal.LitePal.deleteAll(GrouponGoodsBean.GrouponGoodsModel.class);

            runOnUiThread(() -> {
                // 2. 显示同步进度弹窗 (App A 的组件)
                loadingPopup = (com.uhm.uhmcs.popupwindow.SyncLoadingPopup) new com.lxj.xpopup.XPopup.Builder(this)
                        .dismissOnTouchOutside(false)
                        .dismissOnBackPressed(false)
                        .asCustom(new com.uhm.uhmcs.popupwindow.SyncLoadingPopup(this))
                        .show();

                // 3. 从第 1 页开始递归抓取
                downloadGoodsPage(1);
            });
        });
    }

    /**
     * 步骤 B: 递归分页下载并写入库
     * @param requestPage 当前请求的页码
     */


    /**
     * 递归分页下载商品数据并保存至库
     * 适配最新的 GrouponGoodsBean 嵌套结构
     *
     */
    /**
     * 递归分页下载商品数据并保存至库
     * 核心：处理 skuPrice 嵌套条码，将其“打平”存入数据库，确保扫码成功
     */
    /**
     * 递归分页下载商品数据并保存至库
     * 【V3 超级兼容版】：解决条码(sn)存入为空的问题，支持多字段容错抓取
     */



    private void downloadGoodsPage(int requestPage) {
        String shopId = UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid();
        // 建议检查此路径在旧服务器和新服务器是否完全一致
        String url = POSApiSerview.POS_URL + "Supermarket/getGrouponGoods1";

        Map<String, String> params = new HashMap<>();
        params.put("shop_id", shopId);
        params.put("page", String.valueOf(requestPage));
        params.put("strip", String.valueOf(SYNC_PAGE_SIZE));
        params.put("category_ids", "");
        params.put("goods_sn", "");

        // --- 详细日志：请求发起阶段 ---
        Log.e("SyncNetworkDebug", "==================== 同步请求发起 ====================");
        Log.e("SyncNetworkDebug", "请求URL: " + url);
        Log.e("SyncNetworkDebug", "请求页码: " + requestPage);
        Log.e("SyncNetworkDebug", "请求参数: " + params.toString());
        Log.e("SyncNetworkDebug", "Token状态: " + (UserUtils.getInstance().getLoginBase() != null ? "已携带" : "未登录"));

        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                // --- 详细日志：请求成功阶段 ---
                Log.i("SyncNetworkDebug", "收到响应 [第 " + requestPage + " 页]");
                // 只打印前500个字符，防止日志溢出，但能看清结构
                Log.d("SyncNetworkDebug", "原始JSON预览: " + (response.length() > 500 ? response.substring(0, 500) : response));

                dbExecutor.execute(() -> {
                    try {
                        org.json.JSONObject root = new org.json.JSONObject(response);
                        int code = root.optInt("code");
                        String msg = root.optString("msg");

                        if (code != 1) {
                            Log.e("SyncNetworkDebug", "业务逻辑错误 - code: " + code + ", msg: " + msg);
                            finishSync();
                            return;
                        }

                        Gson gson = new Gson();
                        List<GrouponGoodsBean.GrouponGoodsModel> rawList = new ArrayList<>();
                        Object dataObj = root.get("data");

                        // 兼容逻辑
                        if (dataObj instanceof org.json.JSONArray) {
                            java.lang.reflect.Type listType = new com.google.gson.reflect.TypeToken<List<GrouponGoodsBean.GrouponGoodsModel>>(){}.getType();
                            rawList = gson.fromJson(dataObj.toString(), listType);
                        } else if (dataObj instanceof org.json.JSONObject) {
                            GrouponGoodsBean bean = gson.fromJson(response, GrouponGoodsBean.class);
                            if (bean != null && bean.getData() != null) {
                                rawList = bean.getData().getGoodsList();
                            }
                        }

                        if (rawList != null && !rawList.isEmpty()) {
                            Log.i("SyncNetworkDebug", "本页成功解析商品数量: " + rawList.size());
                            List<GrouponGoodsBean.GrouponGoodsModel> finalSaveList = new ArrayList<>();

                            for (GrouponGoodsBean.GrouponGoodsModel item : rawList) {
                                // 拼音处理逻辑保持不变...
                                if (!TextUtils.isEmpty(item.getTitle())) {
                                    String fullPinyin = com.github.promeg.pinyinhelper.Pinyin.toPinyin(item.getTitle(), "").toLowerCase();
                                    item.setPinyin(fullPinyin);
                                    StringBuilder sbInitial = new StringBuilder();
                                    for (char c : item.getTitle().toCharArray()) {
                                        if (com.github.promeg.pinyinhelper.Pinyin.isChinese(c)) {
                                            sbInitial.append(com.github.promeg.pinyinhelper.Pinyin.toPinyin(c).charAt(0));
                                        } else {
                                            sbInitial.append(c);
                                        }
                                    }
                                    item.setPyInitial(sbInitial.toString().toLowerCase());
                                }

                                String foundSn = "";
                                if (!TextUtils.isEmpty(item.getSn())) foundSn = item.getSn();
                                else if (!TextUtils.isEmpty(item.getBarcode())) foundSn = item.getBarcode();
                                else if (!TextUtils.isEmpty(item.getGoods_sn())) foundSn = item.getGoods_sn();

                                if (item.getSkuPrice() != null && !item.getSkuPrice().isEmpty()) {
                                    for (GrouponGoodsBean.GrouponGoodsModel.SkuPriceBean sku : item.getSkuPrice()) {
                                        GrouponGoodsBean.GrouponGoodsModel skuRow = SerializableUtils.deepCopy(item);
                                        skuRow.assignBaseObjId(0);
                                        skuRow.setSn(!TextUtils.isEmpty(sku.getSn()) ? sku.getSn() : foundSn);
                                        skuRow.setGgspid(sku.getId());
                                        finalSaveList.add(skuRow);
                                    }
                                } else {
                                    item.assignBaseObjId(0);
                                    item.setSn(foundSn);
                                    finalSaveList.add(item);
                                }
                            }

                            org.litepal.LitePal.beginTransaction();
                            try {
                                org.litepal.LitePal.saveAll(finalSaveList);
                                org.litepal.LitePal.setTransactionSuccessful();
                                Log.d("SyncNetworkDebug", "数据库写入成功 [第 " + requestPage + " 页]");
                            } finally {
                                org.litepal.LitePal.endTransaction();
                            }

                            handlePagination(gson.fromJson(response, GrouponGoodsBean.class), requestPage);
                        } else {
                            Log.w("SyncNetworkDebug", "本页数据为空，同步可能已提前结束");
                            finishSync();
                        }
                    } catch (Exception e) {
                        Log.e("SyncNetworkDebug", "解析过程崩溃: " + e.getMessage());
                        e.printStackTrace();
                        finishSync();
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                // --- 详细日志：请求失败阶段 ---
                Log.e("SyncNetworkDebug", "==================== 请求彻底失败 ====================");
                Log.e("SyncNetworkDebug", "错误类型: " + e.getClass().getSimpleName());
                Log.e("SyncNetworkDebug", "错误描述: " + e.getMessage());
                if (e.getCause() != null) {
                    Log.e("SyncNetworkDebug", "根本原因: " + e.getCause().toString());
                }
                finishSync();
            }
        });
    }




    /**
     * 步骤 C: 同步结束处理
     */
    private void finishSync() {
        runOnUiThread(() -> {
            if (loadingPopup != null) loadingPopup.dismiss();
            is_tongbu = false;

            // 1. 重置分类状态为“全部”
            category_ids = "";
            if (shopTypeAdapter != null) {
                shopTypeAdapter.setIndex(0); // 让“全部”分类高亮
                shopTypeAdapter.notifyDataSetChanged();
            }

            // 2. 【核心修复】立刻从本地数据库加载数据显示在右侧列表
            loadLocalGoods(0);

            // 3. 弹出同步成功提示
            // new DeleteShopPopupWindow(MainActivity.this, getString(R.string.Sync_completed), true).show();
        });
    }


    /**
     * 核心方法：从本地 LitePal 数据库加载商品
     * @param page 第几页（从0开始）
     */
    private void loadLocalGoods(int page) {
        // 1. 如果是加载第一页，重置状态
        if (page == 0) {
            grouponGoods_page = 1; // 对应你原本的页码变量
            grouponGoodsAdapter.hasMore = true;
            // UI 回到顶部
            runOnUiThread(() -> {
                if (shop_rv != null) shop_rv.scrollToPosition(0);
            });
        }

        // 2. 异步执行数据库查询
        dbExecutor.execute(() -> {
            List<GrouponGoodsBean.GrouponGoodsModel> list;

            // --- 核心过滤逻辑 ---
            if (TextUtils.isEmpty(category_ids)) {
                // A. 如果分类ID为空（即点击了“全部”），查询所有
                list = org.litepal.LitePal.limit(20)
                        .offset(page * 20)
                        .find(GrouponGoodsBean.GrouponGoodsModel.class);
            } else {
                // B. 如果有分类ID，使用模糊查询 (category_ids 包含该ID)
                // 因为你的 category_ids 存的是 "71,320" 这种格式
                list = org.litepal.LitePal.where("category_ids like ?", "%" + category_ids + "%")
                        .limit(20)
                        .offset(page * 20)
                        .find(GrouponGoodsBean.GrouponGoodsModel.class);
            }

            // 3. 切换回主线程更新 UI
            runOnUiThread(() -> {
                if (isFinishing() || isDestroyed()) return;

                if (page == 0) {
                    // 第一页：直接覆盖数据
                    grouponGoodsAdapter.setNewData(list);
                    if (list.size() < 20) grouponGoodsAdapter.hasMore = false;
                } else {
                    // 加载更多：追加数据
                    if (list.isEmpty()) {
                        grouponGoodsAdapter.hasMore = false;
                    } else {
                        grouponGoodsAdapter.loadMoreData(list);
                        // 记录当前加载到的位置
                    }
                }
                Log.d("DB_SEARCH", "分类ID: " + category_ids + ", 本页查到: " + list.size() + " 条");
            });
        });
    }






    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

//        hideKeyboard();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();

            // ========================== 核心：扫码枪静默夺取焦点 ==========================
            // 逻辑：如果光标在拼音框，但扫码枪射出了第一个数字
            if (et_search_pinyin != null && et_search_pinyin.hasFocus()) {
                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                    // 1. 静默切换焦点（不手动调用 hideKeyboard，避免闪烁）
                    et_tiaoxingma.requestFocus();
                    // 2. 将当前按键直接传给条码框
                    et_tiaoxingma.dispatchKeyEvent(event);
                    return true;
                }
            }


            // ==============================================================================

            Log.i("ttt", "外部键盘点击" + keyCode);

            if (keyCode == KeyEvent.KEYCODE_F1) {
                MyPrinterHelper.getInstance().asyncOpenMoneyBox(MainActivity.this);
                // 监听外接键盘的返回键
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_F2) {
                onClickListener.onClick(guadan_btn);
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F3) {
                onClickListener.onClick(qudan_btn);
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F4) {

                if (UserUtils.getInstance().isDazhe()) {
                    onClickListener.onClick(dazhe_one_btn);
                }
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F5) {
                onClickListener.onClick(qingkong_btn);
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F6) {
                if (UserUtils.getInstance().isDazhe()) {
                    onClickListener.onClick(dazhe_all_btn);
                }

                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F7) {
                onClickListener.onClick(daying_btn);
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F8) {
                onClickListener.onClick(checkout_btn);
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F9) {
                is_kuangjie = true;
                onClickListener.onClick(checkout_btn);
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F10) {
                onClickListener.onClick(huiyuan_btn);
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_BACK) {
                onBackPressed();
                return true;
            }
        }
        return super.dispatchKeyEvent(event);
    }





    //tab货品类型list
    public void getLastOder() {

        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        String url = POSApiSerview.POS_URL + POSApiSerview.getLastOder;
        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isEmpty()) {
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                int code = jsonObject.getInt("code");
                                if (code == 1 && !TextUtils.isEmpty(jsonObject.getString("data"))) {
                                    ArrayList<LastOrderBean> lastOrderBeanArrayList = new Gson().fromJson(jsonObject.getString("data"), new TypeToken<ArrayList<LastOrderBean>>() {
                                    }.getType());
                                    MyPrinterHelper.getInstance().asyncPrintLastOrder(MainActivity.this, lastOrderBeanArrayList.get(0), null);
                                }
                            } catch (JSONException e) {
                                Log.e("ttt", "Error occurred", e);
                            }

                        } else {
                            Toast.makeText(MainActivity.this, "请求错误，结果为空", LENGTH_SHORT).show();
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

    public String out_trade_no = "", order_sn = "", transaction_id = "";

    public void fwsgetOrderInformation(CheckoutBean checkoutBean) {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.fwsgetOrderInformation;
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(formBody);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected), true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject = new JSONObject(success);

                                    String msg = jsonObject.getString("msg");

                                    String trade_state_desc = new JSONObject(jsonObject.getString("code")).getString("trade_state_desc");
                                    if (trade_state_desc.contains("密码")) {
                                        fwsgetOrderInformation(checkoutBean);
                                    } else if (trade_state_desc.contains("支付成功")) {
                                        transaction_id = new JSONObject(jsonObject.getString("code")).getString("transaction_id");
                                        checkoutBean.setTransaction_id(transaction_id);
                                        checkoutBean.setOrder_sn(order_sn);
                                        pushorders(checkoutBean);
                                    } else if (trade_state_desc.contains("支付失败")) {
                                        fwscancelanOrder(checkoutBean);
                                    } else if (trade_state_desc.contains("订单已撤销")) {
                                        order_sn = "";
                                        out_trade_no = "";
                                        DialogUIUtils.dismiss(buildBean);
                                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.order_canceled), true).show();
                                    }


                                } catch (JSONException e) {
                                    Log.e("ttt", "Error occurred", e);
                                }
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }

    public void fwscancelanOrder(CheckoutBean checkoutBean) {
        Map<String, String> params = new HashMap<>();
        params.put("outTradeNo", out_trade_no);
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        FormBody.Builder formBuilder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            formBuilder.add(entry.getKey(), entry.getValue());
        }
        RequestBody formBody = formBuilder.build();
        String url = POSApiSerview.POS_URL + POSApiSerview.fwscancelanOrder;
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(formBody);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected), true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fwsgetOrderInformation(checkoutBean);
                            }
                        });

                    } catch (Exception e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }


    public void pushorders(CheckoutBean checkoutBean) {


// ======= 【关键修复：动态抓取收银员姓名】 =======
        try {
            if (UserUtils.getInstance().getLoginBase() != null) {
                String realName = UserUtils.getInstance().getLoginBase().getData().getUserinfo().getNickname();
                checkoutBean.setMachineNumber(realName);
            }
        } catch (Exception e) {}
        // =============================================

        String url = POSApiSerview.POS_URL + POSApiSerview.pushorders;
        Gson gson = new Gson();
        RequestBody body = RequestBody.create(gson.toJson(checkoutBean), MediaType.parse("application/json; charset=utf-8"));
        Request.Builder builder = new Request.Builder()
                .url(url);

        builder.addHeader("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());


        builder.post(body);

        Request request = builder.build();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY); // 设置日志级别
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS) // 连接超时
                .readTimeout(10000, TimeUnit.SECONDS)    // 读取超时
                .writeTimeout(10000, TimeUnit.SECONDS)   // 写入超时
                .addInterceptor(new NetworkErrorInterceptor()) // 先添加异常拦截器
                .addInterceptor(loggingInterceptor)   // 添加日志拦截器
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        DialogUIUtils.dismiss(buildBean);
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.no_network_detected), true).show();
                    }
                });
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    try {
                        String success = response.body().string();
                        JSONObject jsonObject = new JSONObject(success);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (jsonObject.getString("msg").contains("成功") || jsonObject.getString("msg").contains("Success")) {

// 初始化MediaPlayer
                                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.yidong);
                                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                                        mediaPlayer.setOnCompletionListener(mp -> mp.release());
//                    mediaPlayer.pause();  // 暂停
//                    mediaPlayer.stop();   // 停止(需重新prepare)

                                        // 播放控制
                                        mediaPlayer.start();  // 开始播放
                                        DialogUIUtils.dismiss(buildBean);
                                        have_paid_view.setVisibility(VISIBLE);
                                        new Handler().postDelayed(new Runnable() {
                                            @Override
                                            public void run() {
                                                have_paid_view.setVisibility(GONE);
                                            }
                                        }, 3000);
                                        onClickListener.onClick(qingkong_btn);
                                        onClickListener.onClick(shanchuhuiyuan_btn);
                                        String weixin_pice = checkoutBean.getPay_type().equals("wechat") ? checkoutBean.getPay_fee() : "";
                                        String zhifubao_pice = checkoutBean.getPay_type().equals("alipay") ? checkoutBean.getPay_fee() : "";

// --- 核心修改 ---
                                        // ================== 【替换】使用 DIY 指令极速打印 ==================
                                        byte[] printCmds = com.uhm.uhmcs.utils.ReceiptCommandUtils.getReceiptCommands(MainActivity.this, checkoutBean, order_sn);
                                        MyPrinterHelper.getInstance().printCommand(MainActivity.this, printCmds);
// ================================================================
                                        order_sn = "";
                                        out_trade_no = "";
                                    }

                                } catch (JSONException e) {
                                    Log.e("ttt", "Error occurred", e);
                                }
                            }
                        });

                    } catch (JSONException e) {
                        Log.e("ttt", "Error occurred", e);
                    }
                } else {

                }
            }
        });
    }

    public void operateDetails(CheckoutBean checkoutBean) {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        String url = POSApiSerview.POS_URL + POSApiSerview.operateDetails;
        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", ">>>>>>>>>>>>>");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            String weixin_pice = checkoutBean.getPay_type().equals("wechat") ? checkoutBean.getPay_fee() : "";
                            String zhifubao_pice = checkoutBean.getPay_type().equals("alipay") ? checkoutBean.getPay_fee() : "";

                            // ================== 【替换】使用 DIY 指令极速打印 ==================
// 注意：补打接口可能没有 order_sn，如果有请传入，没有传空字符串
                            byte[] printCmds = com.uhm.uhmcs.utils.ReceiptCommandUtils.getReceiptCommands(MainActivity.this, checkoutBean, order_sn);
                            MyPrinterHelper.getInstance().printCommand(MainActivity.this, printCmds);
// ================================================================


                        } catch (JSONException e) {
                            Log.e("ttt", "Error occurred", e);
                        }
                    }




                });
            }

            @Override
            public void onFailure(IOException e) {

            }
        });
    }

    public void operateDetails(LastOrderBean lastOrderBean) {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        String url = POSApiSerview.POS_URL + POSApiSerview.operateDetails;
        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", ">>>>>>>>>>>>>");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            int code = jsonObject.getInt("code");
                            if (code == 1) {
                                ArrayList<PrintDataBean> printDataBeanArrayList = new Gson().fromJson(jsonObject.getString("data"), new TypeToken<ArrayList<PrintDataBean>>() {
                                }.getType());
                                MyPrinterHelper.getInstance().asyncPrintLastOrder(MainActivity.this, lastOrderBean, printDataBeanArrayList.get(0));
                            } else {
                                MyPrinterHelper.getInstance().asyncPrintLastOrder(MainActivity.this, lastOrderBean, null);
                            }
                        } catch (JSONException e) {
                            Log.e("ttt", "Error occurred", e);
                        }
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {

            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        String lang = UserUtils.getInstance().getLanguage();
        super.attachBaseContext(updateBaseContext(base, lang));
    }

    private Context updateBaseContext(Context context, String language) {
        Configuration config = context.getResources().getConfiguration();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        config.setLocale(locale);
        return context.createConfigurationContext(config);
    }


    // ================================= 称重 SDK 支撑方法平移 (开始) =================================

    /**
     * 初始化称重设备
     * @param iType 0:串口, 1:USB
     */
    private void InitDevice(int iType) {
        if (m_scaler != null && m_scaler.AclasIsConnect()) {
            m_scaler.AclasDisconnect();
        }
        m_scaler = new AclasScaler(iType, this, m_listener);
        m_weight = m_scaler.new WeightInfoNew();
        m_scaler.setLog(false);
        m_scaler.AclasSetMulTare(false);
    }



    /**
     * 1. 开启连接流程
     */
    private void OpenScale() {
        isScaleConnected = false;
        currentScanIndex = 0;

        // 优先读取缓存地址
        String savedPort = UserUtils.getInstance().getSerialPortName();
        if (!TextUtils.isEmpty(savedPort)) {
            Log.i("ScaleScan", ">>> 尝试直连缓存地址: " + savedPort);
            startConnectStep(savedPort);
        } else {
            Log.i("ScaleScan", ">>> 无配置，开始盲扫...");
            scanToNextAvailablePort();
        }
    }

    /**
     * 2. 执行提权并连接 (800ms 快速模式)
     */
    private void startConnectStep(String portPath) {
        this.lastAttemptPort = portPath;
        new Thread(() -> {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
            try {
                // 强力提权：chmod + setenforce 0
                java.lang.Process p = Runtime.getRuntime().exec("su");
                java.io.DataOutputStream os = new java.io.DataOutputStream(p.getOutputStream());
                os.writeBytes("chmod 666 " + portPath + "\n");
                os.writeBytes("setenforce 0\n");
                os.writeBytes("exit\n");
                os.flush();
                p.waitFor();
            } catch (Exception e) {}

            runOnUiThread(() -> {
                if (m_scaler != null) {
                    if (m_scaler.AclasIsConnect()) m_scaler.AclasDisconnect();
                    m_weight.init();
                    m_scaler.AclasConnect(portPath, 9600, 500);

                    // 800ms 后检查，没连上就切下一个
                    handler.postDelayed(() -> {
                        if (!isScaleConnected && portPath.equals(lastAttemptPort)) {
                            Log.d("ScaleScan", portPath + " 无响应，跳过...");
                            scanToNextAvailablePort();
                        }
                    }, 800);
                }
            });
        }).start();
    }

    /**
     * 3. 切换到下一个串口
     */
    private void scanToNextAvailablePort() {
        if (isScaleConnected) return;
        if (currentScanIndex < SCAN_PORTS.length) {
            String nextPort = SCAN_PORTS[currentScanIndex];
            currentScanIndex++;
            // 排除刚才试过的缓存口
            if (nextPort.equals(UserUtils.getInstance().getSerialPortName())) {
                scanToNextAvailablePort();
                return;
            }
            startConnectStep(nextPort);
        } else {
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "未检测到可用秤盘", Toast.LENGTH_LONG).show();
                if (tv_real_weight != null) tv_real_weight.setText("未连接");
            });
            currentScanIndex = 0;
        }
    }

    /**
     * 秤数据监听回调
     */
    private AclasScaler.AclasScalerListener m_listener = new AclasScaler.AclasScalerListener() {
        @Override
        public void onConnected() {
            isScaleConnected = true;
            String successPort = lastAttemptPort; // 获取最后一次尝试成功的口
            Log.i("ScaleScan", "★★ 连接成功！地址: " + successPort);

            // 锁定地址：存入本地缓存，实现下次秒连
            UserUtils.getInstance().setSerialPortName(MainActivity.this, successPort);
            currentScanIndex = 0;

            runOnUiThread(() -> Toast.makeText(MainActivity.this, "秤盘连接成功", Toast.LENGTH_SHORT).show());
        }

        @Override
        public void onError(int errornum, String str) {
            Log.e("ScaleScan", "onError [" + errornum + "]: " + str);
            // 错误 -7 代表读不到数据，触发切换下一个口
            if (errornum == -7 && !isScaleConnected) {
                runOnUiThread(() -> scanToNextAvailablePort());
            }
        }

        @Override
        public void onDisConnected() {
            isScaleConnected = false;
        }

        @Override
        public void onRcvData(AclasScaler.WeightInfoNew info) {
            if (setWeightInfo(info)) {
                handler.sendEmptyMessage(MSG_Weight);
            }
        }

        @Override public void onUpdateProcess(int iIndex, int iTotal) {}
    };


    /**
     * 实时更新主界面称重 UI
     */
    private void showWeight(final AclasScaler.WeightInfoNew info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (!info.isOverWeight) {
                    // 更新你收银界面上的两个 TextView
                    if (tv_tare_weight != null) {
                        tv_tare_weight.setText("皮重:" + String.format("%.3f", info.tareWeight));
                    }
                    if (tv_real_weight != null) {
                        tv_real_weight.setText(String.format("%.3f", info.netWeight));
                    }
                } else {
                    if (tv_real_weight != null) tv_real_weight.setText("OVER");
                }
            }
        });
    }




    // 必须加 synchronized，防止多线程冲突
    private synchronized boolean setWeightInfo(AclasScaler.WeightInfoNew info) {
        if (m_weight == null) return false;
        return m_weight.setData(info);
    }



    private synchronized AclasScaler.WeightInfoNew getWeightInfo() {
        return m_weight;
    }

    // ================================= 称重 SDK 支撑方法平移 (结束) =================================


    /**
     * 封装方法：处理扫码成功后的逻辑 (放在类级别，不要嵌套在方法里)
     */
    private void handleScanSuccess(GrouponGoodsBean.GrouponGoodsModel originalModel, String specialWeight, String specialDiscount) {
        // 播放扫码音效
        MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.shaoma);
        mp.setOnCompletionListener(MediaPlayer::release);
        mp.start();

        GrouponGoodsBean.GrouponGoodsModel model = SerializableUtils.deepCopy(originalModel);
        Glide.with(MainActivity.this).load(model.getImage()).into(shop_image);
        have_paid_view.setVisibility(View.GONE);

        if (specialWeight != null) {
            // 复合码计算逻辑
            model.setDiscount(specialDiscount);
            BigDecimal price = new BigDecimal(model.getPrice());
            BigDecimal zhehoujia = price.multiply(new BigDecimal(specialDiscount)).divide(new BigDecimal("100"));

            if ("weight".equals(model.getOnline_type())) {
                BigDecimal heji = zhehoujia.divide(new BigDecimal(500)).multiply(new BigDecimal(specialWeight)).setScale(2, RoundingMode.DOWN);
                model.setHeji(heji);
                model.setGoods_weight(specialWeight);
                model.setShuliang(1);
                model.setDiscounted_price(price.subtract(zhehoujia).divide(new BigDecimal(500)).multiply(new BigDecimal(specialWeight)).setScale(2, RoundingMode.DOWN));
                allNum++;
                zongjia = zongjia.add(heji);
            } else {
                int qty = Integer.parseInt(specialWeight);
                BigDecimal heji = zhehoujia.multiply(new BigDecimal(qty)).setScale(2, RoundingMode.DOWN);
                model.setHeji(heji);
                model.setShuliang(qty);
                model.setGoods_weight("0");
                model.setDiscounted_price(price.subtract(zhehoujia).multiply(new BigDecimal(qty)).setScale(2, RoundingMode.DOWN));
                allNum += qty;
                zongjia = zongjia.add(heji);
            }
            selectedShopList.add(0, model);
        } else {
            // 普通码计算逻辑
            boolean isExists = false;
            for (int i = 0; i < selectedShopList.size(); i++) {
                GrouponGoodsBean.GrouponGoodsModel m = selectedShopList.get(i);
                if (m.getIds().equals(model.getIds()) && m.getGgspid().equals(model.getGgspid())) {
                    m.setShuliang(m.getShuliang() + 1);
                    BigDecimal price = new BigDecimal(m.getPrice());
                    if (!TextUtils.isEmpty(m.getDiscount())) {
                        price = price.multiply(new BigDecimal(m.getDiscount())).divide(new BigDecimal(100));
                    }
                    m.setHeji(m.getHeji().add(price).setScale(2, RoundingMode.DOWN));
                    if (!m.isIs_zengsong()) zongjia = zongjia.add(price).setScale(2, RoundingMode.DOWN);
                    selectedShopAdapter.notifyItemChanged(i);
                    isExists = true;
                    break;
                }
            }
            if (!isExists) {
                model.setShuliang(1);
                model.setGoods_weight("0");
                BigDecimal price = new BigDecimal(model.getPrice());
                if (!TextUtils.isEmpty(memben_discount)) {
                    BigDecimal zhehou = price.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal(100));
                    model.setDiscounted_price(price.subtract(zhehou));
                    model.setDiscount(memben_discount);
                    price = zhehou;
                }
                model.setHeji(price.setScale(2, RoundingMode.DOWN));
                selectedShopList.add(0, model);
                zongjia = zongjia.add(price).setScale(2, RoundingMode.DOWN);
            }
            allNum++;
        }

        selectedShopAdapter.setNewData(selectedShopList);
        selected_LinearLayoutManager.scrollToPosition(0);
        tv_zongjian.setText(String.valueOf(allNum));
        tv_zongjia.setText(zongjia.toString());
        MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
        MyPresentation.setZongjia(zongjia.toString());
        availableAmount();
        updatePlaceholderVisibility();

    }

    private void handleScanError() {
        MediaPlayer mp = MediaPlayer.create(MainActivity.this, R.raw.cuowu);
        mp.setOnCompletionListener(MediaPlayer::release);
        mp.start();
        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.product_not_found_in_inventory), true).show();
    }


    /**
     * 辅助方法：处理分页同步进度及递归逻辑
     * 解决“找不到符号 handlePagination”错误
     */
    private void handlePagination(GrouponGoodsBean bean, int requestPage) {
        if (bean == null || bean.getData() == null) {
            finishSync();
            return;
        }

        GrouponGoodsBean.Pagination pagination = bean.getData().getPagination();
        if (pagination != null) {
            int totalCount = pagination.getTotal();
            int totalPage = pagination.getTotalpage();

            // 1. 更新 UI 进度条
            if (totalCount > 0) {
                int currentCount = Math.min((requestPage * SYNC_PAGE_SIZE), totalCount);
                int percent = Math.min((int) (((double) currentCount / totalCount) * 100), 100);

                runOnUiThread(() -> {
                    if (loadingPopup != null) {
                        loadingPopup.updateProgress(percent, currentCount, totalCount);
                    }
                });
            }

            // 2. 递归逻辑：如果还没到最后一页，继续下载下一页
            if (requestPage < totalPage) {
                Log.i("SyncDebug", "第 " + requestPage + " 页完成，准备请求下一页...");
                downloadGoodsPage(requestPage + 1);
            } else {
                // 3. 所有页码同步完成
                Log.i("SyncDebug", "全部数据同步完毕！");
                finishSync();
            }
        } else {
            // 如果接口没有返回分页信息，默认结束同步
            finishSync();
        }
    }


    /**
     * 【新增】根据拼音/名称/条码搜索本地数据库
     */
    private void searchLocalGoods(String keyword) {
        dbExecutor.execute(() -> {
            List<GrouponGoodsBean.GrouponGoodsModel> filterResults;

            if (android.text.TextUtils.isEmpty(keyword)) {
                // 如果关键词清空了，恢复显示当前分类的前20条
                runOnUiThread(() -> loadLocalGoods(0));
                return;
            } else {
                // 同时模糊匹配：标题、全拼、首字母、条码
                filterResults = org.litepal.LitePal
                        .where("title like ? or pinyin like ? or pyInitial like ? or sn like ?",
                                "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%", "%" + keyword + "%")
                        .limit(50)
                        .find(GrouponGoodsBean.GrouponGoodsModel.class);
            }

            runOnUiThread(() -> {
                if (grouponGoodsAdapter != null) {
                    grouponGoodsAdapter.setNewData(filterResults);
                    grouponGoodsAdapter.hasMore = false; // 搜索模式下关闭加载更多
                    if (shop_rv != null) shop_rv.scrollToPosition(0);
                }
            });
        });
    }

    /**
     * 核心方法：根据购物车数据自动显示/隐藏占位符
     */
    private void updatePlaceholderVisibility() {
        boolean isEmpty = selectedShopList.isEmpty();

        // 1. 处理右侧购物车占位符
        if (ll_cart_empty_placeholder != null && selected_shop_rv != null) {
            ll_cart_empty_placeholder.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            selected_shop_rv.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }

        // 2. 处理左侧图片占位符
        if (tv_image_placeholder != null && shop_image != null) {
            // 如果购物车为空，或者当前没有选中任何商品图片，则显示占位文字
            tv_image_placeholder.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
            shop_image.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * 对齐正确版：将历史订单 Bean 转换为 结账 Bean
     */
    private CheckoutBean convertLastOrderToCheckout(LastOrderBean lastOrder) {
        CheckoutBean bean = new CheckoutBean();
        try {
            bean.setTotal_amount(lastOrder.getTotal_amount());

            // --- 关键对齐点：映射收银员 ---
            if (!TextUtils.isEmpty(lastOrder.getCash_user_sn())) {
                bean.setMachineNumber(lastOrder.getCash_user_sn());
            }

            // --- 关键对齐点：映射会员信息 (支持 consignee 字段) ---
            if (!TextUtils.isEmpty(lastOrder.getConsignee())) {
                bean.setMember_name(lastOrder.getConsignee());
            } else {
                bean.setMember_name(lastOrder.getMember_name());
            }

            if (!TextUtils.isEmpty(lastOrder.getPhone())) {
                bean.setMember_phone(lastOrder.getPhone());
            } else {
                bean.setMember_phone(lastOrder.getMember_phone());
            }

            // 处理商品列表
            if (lastOrder.getOrder_item() != null) {
                Gson gson = new Gson();
                String jsonStr = gson.toJson(lastOrder.getOrder_item());
                bean.setGoodsjson(jsonStr);

                int totalNum = 0;
                for (LastOrderBean.GoodsJsonBean item : lastOrder.getOrder_item()) {
                    totalNum += item.getGoods_num();
                }
                bean.setAllNum(totalNum);
            } else {
                bean.setGoodsjson("[]");
                bean.setAllNum(0);
            }

            bean.setDiscount_fee(lastOrder.getDiscount_fee());
            bean.setCoupon_fee(lastOrder.getCoupon_fee());
            bean.setPay_type(lastOrder.getPay_type());
            bean.setCash_change(lastOrder.getCash_change());
            bean.setGoods_original_amount(lastOrder.getGoods_original_amount());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }



}