package com.uhm.uhmcs.activity;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRouter;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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


import androidx.annotation.NonNull;
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
import com.makeramen.roundedimageview.RoundedImageView;
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
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private Integer selectedShopIndex;
    private RecyclerView rv_choose_menu3, shop_rv, selected_shop_rv;
    private LinearLayoutManager selected_LinearLayoutManager;
    private ShopTypeAdapter shopTypeAdapter;
    private GrouponGoodsAdapter grouponGoodsAdapter;

    private SelectedShopAdapter selectedShopAdapter;
    private CustomInputTextView et_tiaoxingma;
    private Animation animation;
    private TextView tv_zongjia, tv_zongjian, qingkong_btn, qudan_btn, guadan_btn, dazhe_one_btn, dazhe_all_btn, checkout_btn, daying_btn;
    private LinearLayout huiyuan_btn;
    private TextView huiyuan_name;

    private ImageView shanchuhuiyuan_btn;

    private DeleteShopPopupWindow deleteShopPopupWindow;
    private BigDecimal zongjia = new BigDecimal("0.00");
    ;

    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> selectedShopList = new ArrayList<>();

    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> historySelectedShopList = new ArrayList<>();

    private ArrayList<RegistrationShopBean> registrationShopBeanArrayList = new ArrayList<>();

    private View.OnClickListener onClickListener;

    private GetRegistrationShopPopupWindow getRegistrationShopPopupWindow;
    private boolean is_kedian = true;
    private BigDecimal zong_youhui = new BigDecimal("0.00");
    private String memben_discount;
    private boolean is_tongbu = false;
    BuildBean buildBean;
    private MyPresentation presentation;
    private LinearLayout have_paid_view, wangluo_view;
    private TextView bendin_view;

    private TextView caozuo_view;

    private View shop_mocheng;

    private RoundedImageView shop_image;


    private TextView yingfu_tv,shifu_tv,youhui_tv,daijinquan_tv,xianjin_tv,huiyuanka_tv,weixin_tv,zhifubao_tv,zhaolin_tv;
    private LinearLayout zhifuxinxi_view;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        Log.i("ttt", ">>>>onCreate>>>>");
        MyUsbDeviceHelper.getInstance().inti(this);

        if (NetworkUtils.getInstance().isNetworkConnected(this)) {
            OverviewList();
            getGrouponGoods();
            wangluo_view.setVisibility(GONE);
            if (TextUtils.isEmpty(UserUtils.getInstance().getOrderListJson())) {
                bendin_view.setVisibility(GONE);
            } else {
                bendin_view.setVisibility(VISIBLE);
            }

        } else {
            wangluo_view.setVisibility(VISIBLE);
            bendin_view.setVisibility(GONE);
            if (!TextUtils.isEmpty(UserUtils.getInstance().getCategoryListBeanJson())) {
                Gson gson = new Gson();
                CategoryListBean categoryListBean = gson.fromJson(UserUtils.getInstance().getCategoryListBeanJson(), CategoryListBean.class);
                shopTypeAdapter.setNewData(categoryListBean.getData());
            } else {
                OverviewList();
            }
            if (!TextUtils.isEmpty(UserUtils.getInstance().getGrouponGoodsBeanJson())) {
                Gson gson = new Gson();
                GrouponGoodsBean grouponGoodsBean = gson.fromJson(UserUtils.getInstance().getGrouponGoodsBeanJson(), GrouponGoodsBean.class);
                allGrouponGoodsModelList = grouponGoodsBean.getData();
                indexGrouponGoodsModelList = allGrouponGoodsModelList;
                grouponGoodsAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
            } else {
                getGrouponGoods();
            }
        }
        if (networkChangeReceiver == null) {
            networkChangeReceiver = registerNetworkReceiver(this);
        }


    }

    private NetworkChangeReceiver networkChangeReceiver;

    // 网络变化广播接收器
    private class NetworkChangeReceiver extends BroadcastReceiver {
        private final Activity activity;

        public NetworkChangeReceiver(Activity activity) {
            this.activity = activity;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            if (NetworkUtils.getInstance().isNetworkConnected(MainActivity.this)) {
                wangluo_view.setVisibility(GONE);
                if (TextUtils.isEmpty(UserUtils.getInstance().getOrderListJson())) {
                    bendin_view.setVisibility(GONE);
                } else {
                    bendin_view.setVisibility(VISIBLE);
                }
            } else {
                bendin_view.setVisibility(GONE);
                wangluo_view.setVisibility(VISIBLE);
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


        onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                int id = v.getId();
                 /*
                  刷新商品
                 */
                if (id == R.id.shuaxin_btn) {
                    is_tongbu = true;
                    grouponGoods_page = 1;
                    getGrouponGoods();
                    OverviewList();
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

                            // 滚动到位置 0（第一条）
                            selected_LinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
                            if (selectedShopAdapter.getItemCount() > 0) {
                                availableAmount();
                            }

                        }
                    });
                    deleteShopPopupWindow.show();
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

                    qudan_btn.setText(getString(R.string.qudan_num, registrationShopBeanArrayList.size() + ""));

                    selectedShopList.clear();
                    selectedShopAdapter.setNewData(selectedShopList);
                    zongjia = new BigDecimal("0.00");
                    tv_zongjia.setText(zongjia + "");
                    tv_zongjian.setText("0");
                    allNum = 0;
                    MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                    MyPresentation.setZongjia(zongjia.toString());

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

                                } else if (type == 2) {//删除
                                    if (!registrationShopBeanArrayList.isEmpty()) {
                                        getRegistrationShopPopupWindow.setDataDelect();
                                    }

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

                            BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));

                            BigDecimal zhehoujia = yuanjia.multiply(new BigDecimal(discount)).divide(new BigDecimal("100"));

                            grouponGoodsModel.setDiscounted_price(yuanjia.subtract(zhehoujia));

                            grouponGoodsModel.setHeji(zhehoujia);

                            zongjia = zongjia.add(zhehoujia);
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

                                BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));

                                BigDecimal zhehoujia = yuanjia.multiply(new BigDecimal(discount)).divide(new BigDecimal("100"));

                                grouponGoodsModel.setDiscounted_price(yuanjia.subtract(zhehoujia));

                                grouponGoodsModel.setHeji(zhehoujia);

                                zongjia = zongjia.add(zhehoujia);
                                tv_zongjia.setText(zongjia + "");
                                selectedShopAdapter.notifyDataSetChanged();
                                MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                                MyPresentation.setZongjia(zongjia.toString());
                                availableAmount();
                            }

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
                    checkoutBean.setMachineNumber("001");
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
                                    is_tongbu = true;
                                    grouponGoods_page = 1;
                                    getGrouponGoods2();
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
                                    new DeleteShopPopupWindow(true, MainActivity.this, UserUtils.getInstance().isDazhe() ? getString(R.string.discounts_hint, getString(R.string.off)) : getString(R.string.discounts_hint, getString(R.string.on)), new PopupWindowOnClickListener.DeleteShopOnClickListener() {
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
//                                /**
//                                 * 支付设置
//                                 */
//                                case 12:
//                                    new PaymentListPopupWindow(MainActivity.this).show();
//                                    break;
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

        monitor = new NetworkLatencyMonitor();
        monitor.startMonitoring((pingMs, httpMs) -> {
            runOnUiThread(() -> {
                pingText.setText(String.valueOf(pingMs));
                httpText.setText(String.valueOf(httpMs));
                updateColor(pingText, pingMs);
                updateColor(httpText, httpMs);
            });
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
        et_tiaoxingma.setOnInputCompleteListener(text -> {
            Log.i("ttt", ">>>>>>>>>>>>>>" + text);
            et_tiaoxingma.setText("");
            if (TextUtils.isEmpty(text)) {
                return;
            }
            zhifuxinxi_view.setVisibility(GONE);

            String textType = detectPaymentType(text);
            if (textType.equals("unknown")) {
                //
//                Log.i("ttt", ">>>>>>>>>>>>>>" + allGrouponGoodsModelList.size()+"sssssssss");
//                for (int i=0;i<allGrouponGoodsModelList.size();i++){
//                    if (!TextUtils.isEmpty(allGrouponGoodsModelList.get(i).getSn())&& allGrouponGoodsModelList.get(i).getSn().equals(text)){
//                        Log.i("ttt","这个的是空》》》》"+allGrouponGoodsModelList.get(i).getPrice()+">>>>>"+allGrouponGoodsModelList.get(i).getGgprice());
//                    }
//                }


                ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList = allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> !TextUtils.isEmpty(grouponGoodsModel.getSn()) && grouponGoodsModel.getSn().equals(text))
                        .collect(Collectors.toCollection(ArrayList::new));
                if (grouponGoodsModelArrayList.isEmpty()) {
                    String PATTERN = "^\\d{5}\\d{3}.+$";
//
                    if (!text.matches(PATTERN)) {
                        // 初始化MediaPlayer
                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.cuowu);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setOnCompletionListener(mp -> mp.release());
//                    mediaPlayer.pause();  // 暂停
//                    mediaPlayer.stop();   // 停止(需重新prepare)

                        // 播放控制
                        mediaPlayer.start();  // 开始播放
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.product_not_found_in_inventory), true).show();
                        return;
                    }
                    String weight = Integer.parseInt(text.substring(0, 5)) + "";
                    String discount = Integer.parseInt(text.substring(5, 8)) + "";
                    String productId = text.substring(8);

                    ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelList = allGrouponGoodsModelList.stream()
                            .filter(grouponGoodsModel -> grouponGoodsModel.getId().equals(productId))
                            .collect(Collectors.toCollection(ArrayList::new));
                    if (grouponGoodsModelList.isEmpty()) {
                        // 初始化MediaPlayer
                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.cuowu);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setOnCompletionListener(mp -> mp.release());
//                    mediaPlayer.pause();  // 暂停
//                    mediaPlayer.stop();   // 停止(需重新prepare)

                        // 播放控制
                        mediaPlayer.start();  // 开始播放
                        new DeleteShopPopupWindow(MainActivity.this, getString(R.string.product_not_found_in_inventory), true).show();
                        return;
                    } else {
                        // 初始化MediaPlayer
                        MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.shaoma);
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        mediaPlayer.setOnCompletionListener(mp -> mp.release());
//                    mediaPlayer.pause();  // 暂停
//                    mediaPlayer.stop();   // 停止(需重新prepare)

                        // 播放控制
                        mediaPlayer.start();  // 开始播放
                        GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = SerializableUtils.deepCopy(grouponGoodsModelList.get(0));
                        Glide.with(MainActivity.this).clear(shop_image);  // 先清空ImageView
                        Glide.with(MainActivity.this).load(grouponGoodsModel.getImage()).into(shop_image);  // 再加载新图片
                        if (grouponGoodsModel.getOnline_type().equals("weight")){
                            allNum++;
                            grouponGoodsModel.setDiscount(discount);
                            grouponGoodsModel.setGoods_weight(weight);
                            BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                            BigDecimal zhehoujia = price.multiply(new BigDecimal(discount)).divide(new BigDecimal("100"));


                            BigDecimal heji = zhehoujia.divide(new BigDecimal(500)).multiply(new BigDecimal(weight)).setScale(2, RoundingMode.DOWN);

                            grouponGoodsModel.setHeji(heji);
                            grouponGoodsModel.setShuliang(1);
                            grouponGoodsModel.setDiscounted_price(price.subtract(zhehoujia).divide(new BigDecimal(500)).multiply(new BigDecimal(weight)).setScale(2, RoundingMode.DOWN));
                            selectedShopList.add(0, grouponGoodsModel);
                            have_paid_view.setVisibility(GONE);
                            selectedShopAdapter.setNewData(selectedShopList);
                            MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);

                            // 滚动到位置 0（第一条）
                            selected_LinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
                            tv_zongjian.setText(allNum + "");
                            zongjia = zongjia.add(heji);
                            tv_zongjia.setText(zongjia + "");
                            MyPresentation.setZongjia(zongjia.toString());
                            availableAmount();
                        }else {
                            allNum+=Integer.parseInt(weight);
                            grouponGoodsModel.setDiscount(discount);
                            grouponGoodsModel.setGoods_weight("0");
                            BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                            BigDecimal zhehoujia = price.multiply(new BigDecimal(discount)).divide(new BigDecimal("100"));


                            BigDecimal heji = zhehoujia.multiply(new BigDecimal(weight)).setScale(2, RoundingMode.DOWN);

                            grouponGoodsModel.setHeji(heji);
                            grouponGoodsModel.setShuliang(Integer.parseInt(weight));
                            grouponGoodsModel.setDiscounted_price(price.subtract(zhehoujia).multiply(new BigDecimal(weight)).setScale(2, RoundingMode.DOWN));
                            selectedShopList.add(0, grouponGoodsModel);
                            have_paid_view.setVisibility(GONE);
                            selectedShopAdapter.setNewData(selectedShopList);
                            MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);

                            // 滚动到位置 0（第一条）
                            selected_LinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
                            tv_zongjian.setText(allNum + "");
                            zongjia = zongjia.add(heji);
                            tv_zongjia.setText(zongjia + "");
                            MyPresentation.setZongjia(zongjia.toString());
                            availableAmount();
                        }

                        return;
                    }

                }
                // 初始化MediaPlayer
                MediaPlayer mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.shaoma);
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                mediaPlayer.setOnCompletionListener(mp -> mp.release());
//                    mediaPlayer.pause();  // 暂停
//                    mediaPlayer.stop();   // 停止(需重新prepare)

                // 播放控制
                mediaPlayer.start();  // 开始播放
                allNum++;
                GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = grouponGoodsModelArrayList.get(0);
                Glide.with(MainActivity.this).clear(shop_image);  // 先清空ImageView
                Glide.with(MainActivity.this).load(grouponGoodsModel.getImage()).into(shop_image);  // 再加载新图片
                grouponGoodsModel.setGoods_weight("0");
                if (selectedShopList != null && !selectedShopList.isEmpty()) {

                    for (int i = 0; i < selectedShopList.size(); i++) {
                        GrouponGoodsBean.GrouponGoodsModel model = selectedShopList.get(i);
//                        Log.i("ttt", ">>>>>>>>>>>>>>" + model.getId() + "<<<<<" + grouponGoodsModel.getId());
                        Log.i("ttt", ">>>>>>>>Ggspid>>>>>>" + model.getGgspid() + "<<<<<" + grouponGoodsModel.getGgspid());
                        if (model.getId().equals(grouponGoodsModel.getId()) && model.getGgspid().equals(grouponGoodsModel.getGgspid())) {
//                            if (model.getGgspid()==0||model.getGgspid()==grouponGoodsModel.getGgspid())
                            model.setShuliang(model.getShuliang() + 1);
                            BigDecimal price = new BigDecimal(model.getPrice());
                            if (!TextUtils.isEmpty(model.getDiscount())) {
                                price = price.multiply(new BigDecimal(model.getDiscount())).divide(new BigDecimal("100"));
                                model.setDiscounted_price(model.getDiscounted_price().add(new BigDecimal(model.getPrice()).subtract(price)));
                            }
                            BigDecimal heji = price.add(model.getHeji()).setScale(2, RoundingMode.DOWN);
                            model.setHeji(heji);
                            if (!model.isIs_zengsong()) {
                                zongjia = zongjia.add(price).setScale(2, RoundingMode.DOWN);
                            }

                            tv_zongjia.setText(zongjia + "");
                            selectedShopAdapter.notifyItemChanged(i);
                            MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                            tv_zongjian.setText(allNum + "");
                            MyPresentation.setZongjia(zongjia.toString());
                            availableAmount();
                            return;
                        }
                    }
                }
                BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                if (!TextUtils.isEmpty(memben_discount)) {
                    price = price.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal(100));
                    grouponGoodsModel.setDiscounted_price(new BigDecimal(grouponGoodsModel.getPrice()).subtract(price));
                    grouponGoodsModel.setDiscount(memben_discount);
                }

                BigDecimal heji = price.setScale(2, RoundingMode.DOWN);
                grouponGoodsModel.setHeji(heji);
                grouponGoodsModel.setShuliang(1);
                selectedShopList.add(0, SerializableUtils.deepCopy(grouponGoodsModel));

                have_paid_view.setVisibility(GONE);
                selectedShopAdapter.setNewData(selectedShopList);
                MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);

                // 滚动到位置 0（第一条）
                selected_LinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
                tv_zongjian.setText(allNum + "");
                zongjia = zongjia.add(price).setScale(2, RoundingMode.DOWN);
                tv_zongjia.setText(zongjia + "");
                MyPresentation.setZongjia(zongjia.toString());
                availableAmount();
            } else {
                if (selectedShopAdapter.getItemCount() <= 0) {
                    return;
                }
                checkoutBean = new CheckoutBean();
                checkoutBean.setAllNum(allNum);

                checkoutBean.setUser_id(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getUserId());
                checkoutBean.setMachineNumber("001");

                checkoutBean.setTotal_fee(zongjia.toString());

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
//                        goodsJsonBean.setPay_price("0.01");
//                        goodsJsonBean.setGoods_price("0.01");
                    goodsJsonBean.setGoods_sku_price_id(grouponGoodsModel.getGgspid() + "");
//                    goodsJsonBean.setGoods_sku_text(TextUtils.isEmpty(grouponGoodsModel.getGoods_sku_text()) ? "" : grouponGoodsModel.getGoods_sku_text());


                    goodsJsonBeanArrayList.add(goodsJsonBean);
                }
                Gson gson = new Gson();

                checkoutBean.setGoodsjson(gson.toJson(goodsJsonBeanArrayList));
                checkoutBean.setDiscount_fee(discount_fee.toString());
                checkoutBean.setTotal_amount(zongjia.add(discount_fee).toString());
                checkoutBean.setGoods_original_amount(zongjia.add(discount_fee).toString());
                checkoutBean.setPay_type(textType);
                checkoutBean.setShop_id(UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
                checkoutBean.setAuthCode(text);
                checkoutBean.setPay_fee(zongjia.toString());
                checkoutBean.setCash_price(zongjia.toString());
                checkoutBean.setCash_change("0.00");
                checkoutBean.setType(1);
                checkoutBean.setXf_type("1");
                checkoutBean.setOrder_status(2);
                if (memberBean1 != null) {
                    checkoutBean.setMember_name(memberBean1.getNickname());
                    checkoutBean.setMember_phone(memberBean1.getMobile());
//                    checkoutBean.setCardnumber(memberBean1.getMobile());
                    checkoutBean.setCoupon_fee(Coupon_fee);
                    checkoutBean.setPay_fee(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
                    checkoutBean.setTotal_fee(new BigDecimal(checkoutBean.getTotal_fee()).subtract(new BigDecimal(checkoutBean.getCoupon_fee())).toString());
                }

                SubmitCheckout(checkoutBean);

            }


        });

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
            }
        });


        selectedShopAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
            @Override
            public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
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
                shopTypeAdapter.setIndex(position);
                category_ids = TextUtils.isEmpty(shopTypeAdapter.getData().get(position).getId()) ? "" : shopTypeAdapter.getData().get(position).getId();
//                grouponGoods_page = 1;
//                grouponGoodsAdapter.hasMore = true;
//                goods_sn = "";
//                getGrouponGoods();
                if (TextUtils.isEmpty(category_ids)) {
                    indexGrouponGoodsModelList = allGrouponGoodsModelList;
                    grouponGoods_page = 1;
                    grouponGoodsAdapter.hasMore = true;
                    grouponGoodsAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));

                    return;
                }
                ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList = new ArrayList<>();
                grouponGoodsModelArrayList = allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> !Arrays.asList(grouponGoodsModel.getCategory_ids().split(",")).stream().filter(s -> s.equals(category_ids)).collect(Collectors.toCollection(ArrayList::new)).isEmpty())
                        .collect(Collectors.toCollection(ArrayList::new));
                indexGrouponGoodsModelList = grouponGoodsModelArrayList;
                grouponGoods_page = 1;
                grouponGoodsAdapter.hasMore = true;
                grouponGoodsAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
            }
        });
        //=================================商品类型endt=========================================//

        //=================================商品列表start=========================================//
        shop_rv = findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new GridLayoutManager(this, 4)); // 设置3列，横向布局，不反转方向（false）
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
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();

                if (!grouponGoodsAdapter.hasMore) { // 如果已经在加载或者没有更多数据，则不处理滚动事件
                    return;
                }
                if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) { // 当滚动到列表底部时触发加载更多事件
                    // 异步加载下一页数据
                    loadNextPage();
//                    Log.i("ttt", ">>>>>>>>>>>>>>" + grouponGoodsAdapter.getItemCount() % 20);
//                    if (grouponGoodsAdapter.getItemCount() % 20 == 0) {
//                        // 这里调用你的加载更多方法，例如：myAdapter.loadMoreData(newData);
//                        grouponGoods_page++;
//                        getGrouponGoods();
//                    } else {
//                        grouponGoodsAdapter.hasMore = false;
//                    }


                }


            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    Glide.with(MainActivity.this).resumeRequests();
                } else {
                    Glide.with(MainActivity.this).pauseRequests();
                }
            }
        });

        grouponGoodsAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {

            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
//                if (!is_kedian){
//                    return;
//                }
//                is_kedian=false;
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                       is_kedian=true;
//                    }
//                }, 700);
                view.startAnimation(animation);
                allNum++;
                GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = grouponGoodsAdapter.getData().get(position);
                if (selectedShopList != null && !selectedShopList.isEmpty()) {

                    for (int i = 0; i < selectedShopList.size(); i++) {
                        GrouponGoodsBean.GrouponGoodsModel model = selectedShopList.get(i);
//                        Log.i("ttt", ">>>>>>>>>>>>>>" + model.getId() + "<<<<<" + grouponGoodsModel.getId());
                        Log.i("ttt", ">>>>>>>>Ggspid>>>>>>" + model.getGgspid() + "<<<<<" + grouponGoodsModel.getGgspid());
                        if (model.getId() == grouponGoodsModel.getId() && model.getGgspid() == grouponGoodsModel.getGgspid()) {
                            model.setShuliang(model.getShuliang() + 1);
                            BigDecimal price = new BigDecimal(model.getPrice());
                            if (!TextUtils.isEmpty(model.getDiscount())) {
                                price = price.multiply(new BigDecimal(model.getDiscount())).divide(new BigDecimal(100));
                                model.setDiscounted_price(model.getDiscounted_price().add(new BigDecimal(model.getPrice()).subtract(price)));
                            }
                            BigDecimal heji = price.add(model.getHeji()).setScale(2, RoundingMode.DOWN);
                            model.setHeji(heji);
                            if (!model.isIs_zengsong()) {
                                zongjia = zongjia.add(price).setScale(2, RoundingMode.DOWN);
                            }

                            tv_zongjia.setText(zongjia + "");
                            selectedShopAdapter.notifyItemChanged(i);
                            tv_zongjian.setText(allNum + "");
                            MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);
                            MyPresentation.setZongjia(zongjia.toString());
                            availableAmount();
                            return;
                        }
                    }
                }
                BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                if (!TextUtils.isEmpty(memben_discount)) {
                    price = price.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal(100));
                    grouponGoodsModel.setDiscounted_price(new BigDecimal(grouponGoodsModel.getPrice()).subtract(price));
                    grouponGoodsModel.setDiscount(memben_discount);
                }

                BigDecimal heji = price.setScale(2, RoundingMode.DOWN);
                grouponGoodsModel.setHeji(heji);
                grouponGoodsModel.setShuliang(1);
                selectedShopList.add(0, SerializableUtils.deepCopy(grouponGoodsModel));

                have_paid_view.setVisibility(GONE);
                selectedShopAdapter.setNewData(selectedShopList);
                MyPresentation.setShopArrayList(selectedShopAdapter.getData(), allNum);

                // 滚动到位置 0（第一条）
                selected_LinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
                tv_zongjian.setText(allNum + "");
                zongjia = zongjia.add(price).setScale(2, RoundingMode.DOWN);
                tv_zongjia.setText(zongjia + "");
                MyPresentation.setZongjia(zongjia.toString());
                availableAmount();
            }
        });

        time = new TimeCount(30000, 5000);//一共执行30000毫秒，每2000执行一次。
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
                                        }, 3000);
                                        onClickListener.onClick(qingkong_btn);
                                        onClickListener.onClick(shanchuhuiyuan_btn);
                                        String weixin_pice = checkoutBean.getPay_type().equals("wechat") ? checkoutBean.getPay_fee() : "";
                                        String zhifubao_pice = checkoutBean.getPay_type().equals("alipay") ? checkoutBean.getPay_fee() : "";
                                        MyPrinterHelper.getInstance().asyncPrintCheckout(MainActivity.this, checkoutBean, null, "", weixin_pice, zhifubao_pice, order_sn);
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
    public void OverviewList() {

        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
        String url = POSApiSerview.POS_URL + POSApiSerview.getGrouponCategory;
        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isEmpty()) {
                            Gson gson = new Gson();
                            CategoryListBean categoryListBean = gson.fromJson(response, CategoryListBean.class);
                            if (categoryListBean.getCode() == 1) {
                                if (is_tongbu) {
                                    getGrouponGoods();
                                }

                                ArrayList<CategoryListBean.CategoryListModel> models = categoryListBean.getData();
                                CategoryListBean.CategoryListModel categoryListModel = new CategoryListBean.CategoryListModel();
                                categoryListModel.setName(getString(R.string.all));
                                categoryListModel.setId("");


                                models.add(0, categoryListModel);
                                categoryListBean.setData(models);
                                UserUtils.getInstance().setCategoryListBeanJson(MainActivity.this, gson.toJson(categoryListBean));
//
//                            CategoryListBean.CategoryListModel categoryListModel1 = new CategoryListBean.CategoryListModel();
//                            categoryListModel1.setName(POSApplication.text[1]);
//                            categoryListModel1.setDrawableId(POSApplication.drawableIds[1]);
//                            models.add(1, categoryListModel1);
//
//
                                shopTypeAdapter.setIndex(0);
                                shopTypeAdapter.setNewData(models);
//                            Log.i("ttt", chooseFoodMenuListAdapter.getItemCount() + "sSsDD");
                            }

                        } else {
//                            Toast.makeText(LoginActivity.this,"数据处理错误:"+ex.getMessage(),Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "请求错误，结果为空", LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {
                    if (!TextUtils.isEmpty(UserUtils.getInstance().getCategoryListBeanJson())) {
                        Gson gson = new Gson();
                        CategoryListBean categoryListBean = gson.fromJson(UserUtils.getInstance().getCategoryListBeanJson(), CategoryListBean.class);
                        shopTypeAdapter.setNewData(categoryListBean.getData());
                    }
                    System.err.println("请求失败: " + e.getMessage());
                });

            }
        });


    }

    private String goods_sn = "";
    private String category_ids = "";
    private int grouponGoods_page = 1;
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> allGrouponGoodsModelList = new ArrayList<>();
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> indexGrouponGoodsModelList = new ArrayList<>();

    private void getGrouponGoods() {
        LoadingPopupView popupView = (LoadingPopupView) new XPopup.Builder(this)
                .asLoading(getString(R.string.loading_data))
                .show();
//        popupView.setTitle("");
        Map<String, String> params = new HashMap<>();
//        params.put("category_ids", TextUtils.isEmpty(category_ids) ? "" : category_ids);
        params.put("category_ids", "");
        params.put("goods_sn", "");
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
//        params.put("page", grouponGoods_page + "");
//        params.put("strip", "20");
        String url = POSApiSerview.POS_URL + POSApiSerview.getGrouponGoods2;
        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isEmpty()) {

                            Gson gson = new Gson();
                            GrouponGoodsBean grouponGoodsBean = gson.fromJson(response, GrouponGoodsBean.class);
                            if (grouponGoodsBean.getCode() == 1) {
                                popupView.dismiss();
                                if (is_tongbu) {
                                    new DeleteShopPopupWindow(MainActivity.this, getString(R.string.Sync_completed), true).show();
                                    is_tongbu = false;
                                }

                                UserUtils.getInstance().setGrouponGoodsBeanJson(MainActivity.this, response);
                                if (grouponGoods_page == 1) {
                                    if (grouponGoodsBean.getData() != null) {
                                        allGrouponGoodsModelList = grouponGoodsBean.getData();
                                        indexGrouponGoodsModelList = allGrouponGoodsModelList;
                                        grouponGoods_page = 1;
                                        grouponGoodsAdapter.hasMore = true;
                                        grouponGoodsAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
                                    } else {
                                        Toast.makeText(MainActivity.this, "未查询到商品", LENGTH_SHORT).show();
                                    }

                                } else {
                                    if (grouponGoodsBean.getData() == null) {
                                        grouponGoodsAdapter.hasMore = false;
                                    } else {
                                        // 请求成功后，更新数据并通知适配器数据已更改
                                        grouponGoodsAdapter.loadMoreData(grouponGoodsBean.getData()); // newDataList 是新加载的数据列表
                                    }


                                }
                            }


                        } else {

//                            Toast.makeText(LoginActivity.this,"数据处理错误:"+ex.getMessage(),Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "请求错误，结果为空", LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {
                    popupView.dismiss();
                    if (!TextUtils.isEmpty(UserUtils.getInstance().getGrouponGoodsBeanJson())) {
                        Gson gson = new Gson();
                        GrouponGoodsBean grouponGoodsBean = gson.fromJson(UserUtils.getInstance().getGrouponGoodsBeanJson(), GrouponGoodsBean.class);
                        allGrouponGoodsModelList = grouponGoodsBean.getData();
                        indexGrouponGoodsModelList = allGrouponGoodsModelList;
                        grouponGoods_page = 1;
                        grouponGoodsAdapter.hasMore = true;
                        grouponGoodsAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
                    }
                    System.err.println("请求失败: " + e.getMessage());
                });

            }
        });
    }


    private void getGrouponGoods2() {
        LoadingPopupView popupView = (LoadingPopupView) new XPopup.Builder(this)
                .asLoading(getString(R.string.loading_data))
                .show();
//        popupView.setTitle("");
        Map<String, String> params = new HashMap<>();
//        params.put("category_ids", TextUtils.isEmpty(category_ids) ? "" : category_ids);
        params.put("category_ids", "");
        params.put("goods_sn", "");
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid());
//        params.put("page", grouponGoods_page + "");
//        params.put("strip", "20");
        String url = POSApiSerview.POS_URL + POSApiSerview.getGrouponGoods3;
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
                                if (jsonObject.getInt("code") == 1) {
                                    popupView.dismiss();
                                    getGrouponGoods();
                                }
                            } catch (JSONException e) {
                                Log.e("ttt", "Error occurred", e);
                            }


                        } else {

//                            Toast.makeText(LoginActivity.this,"数据处理错误:"+ex.getMessage(),Toast.LENGTH_SHORT).show();
                            Toast.makeText(MainActivity.this, "请求错误，结果为空", LENGTH_SHORT).show();
                        }
                    }
                });

            }

            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> {
                    popupView.dismiss();
                    if (!TextUtils.isEmpty(UserUtils.getInstance().getGrouponGoodsBeanJson())) {
                        Gson gson = new Gson();
                        GrouponGoodsBean grouponGoodsBean = gson.fromJson(UserUtils.getInstance().getGrouponGoodsBeanJson(), GrouponGoodsBean.class);
                        allGrouponGoodsModelList = grouponGoodsBean.getData();
                        indexGrouponGoodsModelList = allGrouponGoodsModelList;
                        grouponGoods_page = 1;
                        grouponGoodsAdapter.hasMore = true;
                        grouponGoodsAdapter.setNewData(getPageData(grouponGoods_page, indexGrouponGoodsModelList));
                    }
                    System.err.println("请求失败: " + e.getMessage());
                });

            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        hideKeyboard();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
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
                                        MyPrinterHelper.getInstance().asyncPrintCheckout(MainActivity.this, checkoutBean, null, "", weixin_pice, zhifubao_pice, order_sn);
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
                            if (code == 1) {
                                ArrayList<PrintDataBean> printDataBeanArrayList = new Gson().fromJson(jsonObject.getString("data"), new TypeToken<ArrayList<PrintDataBean>>() {
                                }.getType());
                                MyPrinterHelper.getInstance().asyncPrintCheckout(MainActivity.this, checkoutBean, printDataBeanArrayList.get(0), "", weixin_pice, zhifubao_pice, order_sn);
                            } else {
                                MyPrinterHelper.getInstance().asyncPrintCheckout(MainActivity.this, checkoutBean, null, "", weixin_pice, zhifubao_pice, order_sn);
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

}