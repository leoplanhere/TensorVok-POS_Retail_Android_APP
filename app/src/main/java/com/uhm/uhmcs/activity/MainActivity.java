package com.uhm.uhmcs.activity;

import static android.widget.Toast.LENGTH_SHORT;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
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
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.popupwindow.CheckoutPopupWindow;
import com.uhm.uhmcs.popupwindow.DeleteShopPopupWindow;
import com.uhm.uhmcs.popupwindow.DiscountPopupWindow;
import com.uhm.uhmcs.popupwindow.GetRegistrationShopPopupWindow;
import com.uhm.uhmcs.popupwindow.GoodsWarehousingPopupWindow;
import com.uhm.uhmcs.popupwindow.MemberPopupWindow;
import com.uhm.uhmcs.popupwindow.MorefunctionPopupWindow;
import com.uhm.uhmcs.popupwindow.PopupWindowOnClickListener;
import com.uhm.uhmcs.utils.MyPrinterHelper;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.view.CustomInputTextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    private boolean is_tongbu=false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

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
            grouponGoodsAdapter.setNewData(grouponGoodsBean.getData());
        } else {
            getGrouponGoods();
        }

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

    boolean is_kuangjie = false;

    private void initView() {


        onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();

                /**
                 * 删除商品
                 */
                if (id == R.id.delete_shop) {
                    if (selectedShopIndex == null) {
                        if (selectedShopAdapter.getItemCount() > 0) {
                            new DeleteShopPopupWindow(MainActivity.this, "请选择商品").show();
                        }
                        return;
                    }
                    deleteShopPopupWindow = new DeleteShopPopupWindow(MainActivity.this, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onClick(String string) {
                            if (!selectedShopList.get(selectedShopIndex).isIs_zengsong()) {
                                zongjia = zongjia.subtract(selectedShopList.get(selectedShopIndex).getHeji()).setScale(2, RoundingMode.UP);
                                tv_zongjia.setText(zongjia + "");
                            }
                            selectedShopList.remove(selectedShopIndex.intValue());
                            selectedShopAdapter.setNewData(selectedShopList);
                            tv_zongjian.setText(selectedShopAdapter.getItemCount() + "");
                            selectedShopIndex = null;

                            // 滚动到位置 0（第一条）
                            selected_LinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果

                        }
                    });
                    deleteShopPopupWindow.show();
                }
                /**
                 * 清空商品
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
                }
                /**
                 * 挂单
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
                    ArrayList<GrouponGoodsBean.GrouponGoodsModel> registrationShopList = new ArrayList<>();
                    registrationShopList.addAll(selectedShopList);
                    registrationShopBean.setRegistrationShopList(registrationShopList);
                    registrationShopBeanArrayList.add(registrationShopBean);

                    qudan_btn.setText(getString(R.string.qudan_num, registrationShopBeanArrayList.size() + ""));

                    selectedShopList.clear();
                    selectedShopAdapter.notifyDataSetChanged();
                    zongjia = new BigDecimal("0.00");
                    tv_zongjia.setText(zongjia + "");
                    tv_zongjian.setText("0");

                }

                /**
                 *  取单
                 */
                if (id == R.id.qudan_btn) {
                    if (!registrationShopBeanArrayList.isEmpty()) {
                        getRegistrationShopPopupWindow = new GetRegistrationShopPopupWindow(MainActivity.this, registrationShopBeanArrayList, new PopupWindowOnClickListener.GetRegistrationShopOnClickListener() {
                            @Override
                            public void onClick(int index, int type) {
                                RegistrationShopBean registrationShopBean = registrationShopBeanArrayList.get(index);
                                registrationShopBeanArrayList.remove(index);
                                if (type == 1) {//取单
                                    zongjia = registrationShopBean.getTotal_price();
                                    tv_zongjia.setText(zongjia + "");
                                    selectedShopList = registrationShopBean.getRegistrationShopList();
                                    tv_zongjian.setText(selectedShopList.size() + "");
                                    selectedShopAdapter.setNewData(selectedShopList);

                                } else if (type == 2) {//删除
                                    if (!registrationShopBeanArrayList.isEmpty()) {
                                        getRegistrationShopPopupWindow.setDataDelect();
                                    }

                                }
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

                /**
                 * 改价/打折
                 */
                if (id == R.id.dazhe_one_btn) {
                    if (selectedShopIndex == null) {
                        if (selectedShopAdapter.getItemCount() > 0) {
                            new DeleteShopPopupWindow(MainActivity.this, "请选择商品").show();
                        }
                        return;
                    }
                    new DiscountPopupWindow(MainActivity.this, "改价/打折", new PopupWindowOnClickListener.DiscountOnClickListener() {
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


                        }
                    }).show();
                }
                /**
                 * 整单打折
                 */
                if (id == R.id.dazhe_all_btn) {
                    if (selectedShopAdapter.getItemCount() <= 0) {
                        return;
                    }
                    new DiscountPopupWindow(MainActivity.this, "整单打折", new PopupWindowOnClickListener.DiscountOnClickListener() {
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

                            }

                        }
                    }).show();
                }

                /**
                 * 结账
                 */
                if (id == R.id.checkout_btn) {
                    if (selectedShopAdapter.getItemCount() <= 0) {
                        return;
                    }
                    CheckoutBean checkoutBean = new CheckoutBean();


                    checkoutBean.setUser_id(UserUtils.getInstance().getLoginBase().getData().getUserinfo().getUserId());
                    checkoutBean.setMachineNumber("001");

                    checkoutBean.setTotal_fee(zongjia.intValue());

                    if (is_kuangjie) {
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
                        goodsJsonBean.setGoods_id(grouponGoodsModel.getId());
                        goodsJsonBean.setTitle(grouponGoodsModel.getTitle());
                        goodsJsonBean.setGoods_sn(grouponGoodsModel.getGoods_sn());
                        goodsJsonBean.setSn(grouponGoodsModel.getSn());
                        goodsJsonBean.setDiscount(TextUtils.isEmpty(grouponGoodsModel.getDiscount()) ? "100" : grouponGoodsModel.getDiscount());
                        goodsJsonBean.setDiscounted_price(grouponGoodsModel.getDiscounted_price() == null ? "0.00" : grouponGoodsModel.getDiscounted_price().toString());
                        goodsJsonBean.setGoods_price(grouponGoodsModel.getPrice());
                        goodsJsonBean.setGoods_num(grouponGoodsModel.getShuliang());
                        goodsJsonBean.setPay_price(grouponGoodsModel.getHeji().toString());
                        goodsJsonBean.setGoods_sku_price_id(grouponGoodsModel.getGoods_sku_ids());
                        goodsJsonBean.setGoods_sku_text(grouponGoodsModel.getGoods_sku_text());


                        goodsJsonBeanArrayList.add(goodsJsonBean);
                    }
                    Gson gson = new Gson();

                    checkoutBean.setGoodsjson(gson.toJson(goodsJsonBeanArrayList));
                    checkoutBean.setDiscount_fee(discount_fee.intValue());
                    checkoutBean.setTotal_amount(zongjia.add(discount_fee).intValue());
                    checkoutBean.setGoods_original_amount(zongjia.add(discount_fee).intValue());

                    new CheckoutPopupWindow(MainActivity.this, checkoutBean, new PopupWindowOnClickListener.CheckoutOnClickListener() {
                        @Override
                        public void onClick() {
                            onClickListener.onClick(qingkong_btn);
                        }
                    }).show();
                }

                /**
                 * 会员查询
                 */
                if (id == R.id.huiyuan_btn) {
                    new MemberPopupWindow(MainActivity.this, new PopupWindowOnClickListener.MemberOnClickListener() {
                        @Override
                        public void onClick(MemberBean memberBean) {
                            huiyuan_name.setText(memberBean.getNickname());
                            if (memberBean.getVip() == 0) {
                                memben_discount = "";
                            }
                            if (memberBean.getVip() == 17) {//青铜
                                memben_discount = "98";
                            }
                            if (memberBean.getVip() == 18) {//白银
                                memben_discount = "95";
                            }
                            if (memberBean.getVip() == 19) {//黄金
                                memben_discount = "90";
                            }
                            if (selectedShopAdapter.getItemCount() <= 0) {
                                return;
                            }
                            for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopAdapter.getData()) {

                                grouponGoodsModel.setDiscount(memben_discount);

                                zongjia = zongjia.subtract(grouponGoodsModel.getHeji());
                                Log.i("ttt", ">>>>>>" + zongjia);

                                BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));

                                BigDecimal zhehoujia = yuanjia.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal("100"));

                                grouponGoodsModel.setDiscounted_price(yuanjia.subtract(zhehoujia));

                                grouponGoodsModel.setHeji(zhehoujia);

                                zongjia = zongjia.add(zhehoujia);
                                tv_zongjia.setText(zongjia + "");
                                selectedShopAdapter.notifyDataSetChanged();

                            }
                        }
                    }).show();
                }
                /**
                 * 打印尾单
                 */
                if (id == R.id.daying_btn) {
                    getLastOder();
                }
                /**
                 * 删除会员
                 */
                if (R.id.shanchuhuiyuan_btn == id) {
                    huiyuan_name.setText("会员昵称");
                    memben_discount = "100";

                    if (selectedShopAdapter.getItemCount() <= 0) {
                        return;
                    }
                    for (GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel : selectedShopAdapter.getData()) {

                        grouponGoodsModel.setDiscount(memben_discount);

                        zongjia = zongjia.subtract(grouponGoodsModel.getHeji());
                        Log.i("ttt", ">>>>>>" + zongjia);

                        BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));

                        BigDecimal zhehoujia = yuanjia.multiply(new BigDecimal(memben_discount)).divide(new BigDecimal("100"));

                        grouponGoodsModel.setDiscounted_price(yuanjia.subtract(zhehoujia));

                        grouponGoodsModel.setHeji(zhehoujia);

                        zongjia = zongjia.add(zhehoujia);
                        tv_zongjia.setText(zongjia + "");
                        selectedShopAdapter.notifyDataSetChanged();

                    }
                }
                /**
                 * 更多功能
                 */
                if (R.id.more_function_btn == id) {
                    new MorefunctionPopupWindow(MainActivity.this, new PopupWindowOnClickListener.MorefunctionOnClickListener() {
                        @Override
                        public void onClick(int btnType) {
                            switch (btnType) {
                                /**
                                 * 同步数据
                                 */
                                case 1:
                                    is_tongbu=true;
                                    OverviewList();
                                    break;
                                /**
                                 * 标签打印
                                 */
                                case 2:

                                    break;
                                /**
                                 * 商品入库
                                 */
                                case 3:
                                    new GoodsWarehousingPopupWindow(MainActivity.this).show();
                                    break;
                                /**
                                 * 历史账单
                                 */
                                case 4:

                                    break;
                                /**
                                 * 退出登录
                                 */
                                case 5:
                                    UserUtils.getInstance().setCategoryListBeanJson(MainActivity.this,"");
                                    UserUtils.getInstance().setGrouponGoodsBeanJson(MainActivity.this,"");
                                    UserUtils.getInstance().setLoginBase(MainActivity.this,null);
                                    UserUtils.getInstance().setShopDataBean(MainActivity.this,null);
                                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                                    break;
                                default:
                                    throw new IllegalStateException("Unexpected value: " + btnType);
                            }
                        }
                    }).show();
                }


            }
        };
        findViewById(R.id.delete_shop).setOnClickListener(onClickListener);
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
        findViewById(R.id.shanchuhuiyuan_btn).setOnClickListener(onClickListener);
        findViewById(R.id.more_function_btn).setOnClickListener(onClickListener);


        tv_zongjia = findViewById(R.id.tv_zongjia);
        tv_zongjian = findViewById(R.id.tv_zongjian);

        animation = AnimationUtils.loadAnimation(MainActivity.this, R.anim.scale_click);

        et_tiaoxingma = findViewById(R.id.et_tiaoxingma);
        // 设置输入完成监听
        et_tiaoxingma.setOnInputCompleteListener(text -> {
            Log.i("ttt", ">>>>>>>>>>>>>>" + text);
            et_tiaoxingma.setText("");
            //
            ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList = allGrouponGoodsModelList.stream()
                    .filter(grouponGoodsModel -> grouponGoodsModel.getSn().equals(text))
                    .collect(Collectors.toCollection(ArrayList::new));
            if (grouponGoodsModelArrayList == null || grouponGoodsModelArrayList.size() <= 0) {
                new DeleteShopPopupWindow(MainActivity.this, "商品库中没有该商品", true).show();
                return;
            }
            GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = grouponGoodsModelArrayList.get(0);
            if (selectedShopList != null && !selectedShopList.isEmpty()) {

                for (int i = 0; i < selectedShopList.size(); i++) {
                    GrouponGoodsBean.GrouponGoodsModel model = selectedShopList.get(i);
                    Log.i("ttt", ">>>>>>>>>>>>>>" + model.getId() + "<<<<<" + grouponGoodsModel.getId());
                    if (model.getId() == grouponGoodsModel.getId()) {
                        model.setShuliang(model.getShuliang() + 1);
                        BigDecimal price = new BigDecimal(model.getPrice());
                        if (!TextUtils.isEmpty(model.getDiscount())) {
                            price = price.multiply(new BigDecimal(model.getDiscount())).divide(new BigDecimal(100));
                            model.setDiscounted_price(model.getDiscounted_price().add(new BigDecimal(model.getPrice()).subtract(price)));
                        }
                        BigDecimal heji = price.add(model.getHeji()).setScale(2, RoundingMode.UP);
                        model.setHeji(heji);
                        if (!model.isIs_zengsong()) {
                            zongjia = zongjia.add(price).setScale(2, RoundingMode.UP);
                        }

                        tv_zongjia.setText(zongjia + "");
                        selectedShopAdapter.notifyItemChanged(i);
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

            BigDecimal heji = price.setScale(2, RoundingMode.UP);
            grouponGoodsModel.setHeji(heji);
            grouponGoodsModel.setShuliang(1);
            selectedShopList.add(0, grouponGoodsModel);


            selectedShopAdapter.setNewData(selectedShopList);
            // 滚动到位置 0（第一条）
            selected_LinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
            tv_zongjian.setText(selectedShopAdapter.getItemCount() + "");
            zongjia = zongjia.add(price).setScale(2, RoundingMode.UP);
            tv_zongjia.setText(zongjia + "");
        });

        // 自动获取焦点
        et_tiaoxingma.postDelayed(() -> et_tiaoxingma.requestFocus(), 100);


//        hideKeyboard();
//        // 初始化时隐藏
//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//        // 禁用输入法并保留光标（兼容高版本）
//        et_tiaoxingma.setShowSoftInputOnFocus(false);
//
//        // 点击 EditText 时拦截键盘
//        et_tiaoxingma.setOnTouchListener((v, event) -> {
//            // 拦截触摸事件，避免获取焦点
//            return true;
//        });
//
//        et_tiaoxingma.setInputType(InputType.TYPE_NULL); // 禁止软键盘
//        // 1. 禁止软键盘弹出（但不影响光标）
//        et_tiaoxingma.setShowSoftInputOnFocus(false);
//
//// 2. 强制显示光标（XML 或代码均可）
//        et_tiaoxingma.setCursorVisible(true); // 代码设置
//        // 在请求焦点后调用
//        et_tiaoxingma.requestFocus();
//        // 设置光标到文本末尾
//        et_tiaoxingma.setSelection(et_tiaoxingma.getText().length());
//        // 设置焦点监听
//        et_tiaoxingma.setOnFocusChangeListener((v, hasFocus) -> {
//            hideKeyboard();
//        });
        // 反射禁用软键盘
//        try {
//            Method method = EditText.class.getMethod("setShowSoftInputOnFocus", boolean.class);
//            method.setAccessible(true);
//            method.invoke(et_tiaoxingma, false);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


        //=================================选中购买的商品start=========================================//
        selected_shop_rv = findViewById(R.id.selected_shop_rv);
        selected_LinearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        selected_shop_rv.setLayoutManager(selected_LinearLayoutManager);
        selectedShopAdapter = new SelectedShopAdapter(this, R.layout.item_selected_shop);
        selected_shop_rv.setAdapter(selectedShopAdapter);

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
                    if (grouponGoodsModel.getShuliang() > 1) {
                        grouponGoodsModel.setShuliang(grouponGoodsModel.getShuliang() - 1);

                        BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                        if (!TextUtils.isEmpty(grouponGoodsModel.getDiscount())) {
                            price = price.multiply(new BigDecimal(grouponGoodsModel.getDiscount())).divide(new BigDecimal("100"));
                            grouponGoodsModel.setDiscounted_price(grouponGoodsModel.getDiscounted_price().subtract(new BigDecimal(grouponGoodsModel.getPrice()).subtract(price)));
                        }


                        BigDecimal heji = grouponGoodsModel.getHeji().subtract(price).setScale(2, RoundingMode.UP);
                        grouponGoodsModel.setHeji(heji);
                        if (!grouponGoodsModel.isIs_zengsong()) {
                            zongjia = zongjia.subtract(price).setScale(2, RoundingMode.UP);
                        }

                    } else {
                        deleteShopPopupWindow = new DeleteShopPopupWindow(MainActivity.this, new PopupWindowOnClickListener.DeleteShopOnClickListener() {
                            @Override
                            public void onClick(String text) {
                                if (!grouponGoodsModel.isIs_zengsong()) {
                                    BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                                    if (!TextUtils.isEmpty(grouponGoodsModel.getDiscount())) {
                                        price = price.multiply(new BigDecimal(grouponGoodsModel.getDiscount())).divide(new BigDecimal("100"));
                                        grouponGoodsModel.setDiscounted_price(grouponGoodsModel.getDiscounted_price().subtract(new BigDecimal(grouponGoodsModel.getPrice()).subtract(price)));
                                    }

                                    zongjia = zongjia.subtract(price).setScale(2, RoundingMode.UP);
                                    tv_zongjia.setText(zongjia + "");
                                }
                                selectedShopList.remove(position);
                                selectedShopAdapter.setNewData(selectedShopList);
                                tv_zongjian.setText(selectedShopAdapter.getItemCount());

                            }
                        });
                        deleteShopPopupWindow.show();
                        return;
                    }

                } else if (id == R.id.shuliang_jia) {
                    grouponGoodsModel.setShuliang(grouponGoodsModel.getShuliang() + 1);

                    BigDecimal price = new BigDecimal(grouponGoodsModel.getPrice());
                    if (!TextUtils.isEmpty(grouponGoodsModel.getDiscount())) {
                        price = price.multiply(new BigDecimal(grouponGoodsModel.getDiscount())).divide(new BigDecimal("100"));
                        grouponGoodsModel.setDiscounted_price(grouponGoodsModel.getDiscounted_price().add(new BigDecimal(grouponGoodsModel.getPrice()).subtract(price)));
                    }

                    BigDecimal heji = grouponGoodsModel.getHeji().add(price).setScale(2, RoundingMode.UP);
                    grouponGoodsModel.setHeji(heji);
                    if (!grouponGoodsModel.isIs_zengsong()) {
                        zongjia = zongjia.add(price).setScale(2, RoundingMode.UP);
                    }

                } else if (id == R.id.zengsong) {
                    BigDecimal yuanjia = new BigDecimal(grouponGoodsModel.getPrice()).multiply(new BigDecimal(grouponGoodsModel.getShuliang()));
                    if (!grouponGoodsModel.isIs_zengsong()) {
                        grouponGoodsModel.setIs_zengsong(true);
                        grouponGoodsModel.setDiscount("0");
                        grouponGoodsModel.setDiscounted_price(yuanjia);
                        zongjia = zongjia.subtract(grouponGoodsModel.getHeji()).setScale(2, RoundingMode.UP);
                        grouponGoodsModel.setHeji(new BigDecimal("0.00"));

                    } else {
                        grouponGoodsModel.setDiscount("100");
                        grouponGoodsModel.setIs_zengsong(false);
                        grouponGoodsModel.setDiscounted_price(new BigDecimal("0.00"));
                        grouponGoodsModel.setHeji(yuanjia);
                        zongjia = zongjia.add(yuanjia).setScale(2, RoundingMode.UP);
                    }

                }
                tv_zongjia.setText(zongjia + "");
                selectedShopAdapter.notifyItemChanged(position);
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
                for (CategoryListBean.CategoryListModel categoryListModel : shopTypeAdapter.getData()) {
                    categoryListModel.setSelected(false);
                }
                shopTypeAdapter.getData().get(position).setSelected(true);
                shopTypeAdapter.notifyDataSetChanged();
                category_ids = TextUtils.isEmpty(shopTypeAdapter.getData().get(position).getId()) ? "" : shopTypeAdapter.getData().get(position).getId();
//                grouponGoods_page = 1;
//                grouponGoodsAdapter.hasMore = true;
//                goods_sn = "";
//                getGrouponGoods();
                if (TextUtils.isEmpty(category_ids)) {
                    grouponGoodsAdapter.setNewData(allGrouponGoodsModelList);
                    return;
                }
                ArrayList<GrouponGoodsBean.GrouponGoodsModel> grouponGoodsModelArrayList = new ArrayList<>();
                grouponGoodsModelArrayList = allGrouponGoodsModelList.stream()
                        .filter(grouponGoodsModel -> grouponGoodsModel.getCategory_ids().equals(category_ids))
                        .collect(Collectors.toCollection(ArrayList::new));

                grouponGoodsAdapter.setNewData(grouponGoodsModelArrayList);
            }
        });
        //=================================商品类型endt=========================================//

        //=================================商品列表start=========================================//
        shop_rv = findViewById(R.id.shop_rv);
        shop_rv.setLayoutManager(new GridLayoutManager(this, 4)); // 设置3列，横向布局，不反转方向（false）
        grouponGoodsAdapter = new GrouponGoodsAdapter(this, R.layout.item_groupon_goods);
        shop_rv.setAdapter(grouponGoodsAdapter);
//        shop_rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
//                super.onScrolled(recyclerView, dx, dy);
//                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
//                int visibleItemCount = layoutManager.getChildCount();
//                int totalItemCount = layoutManager.getItemCount();
//                int pastVisiblesItems = layoutManager.findFirstVisibleItemPosition();
//
//                if (!grouponGoodsAdapter.isLoading && !grouponGoodsAdapter.hasMore) { // 如果已经在加载或者没有更多数据，则不处理滚动事件
//                    return;
//                }
//                if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) { // 当滚动到列表底部时触发加载更多事件
//                    Log.i("ttt", ">>>>>>>>>>>>>>" + grouponGoodsAdapter.getItemCount() % 20);
//                    if (grouponGoodsAdapter.getItemCount() % 20 == 0) {
//                        // 这里调用你的加载更多方法，例如：myAdapter.loadMoreData(newData);
//                        grouponGoods_page++;
//                        getGrouponGoods();
//                    } else {
//                        grouponGoodsAdapter.hasMore = false;
//                    }
//
//
//                }
//
//
//            }
//        });

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

                GrouponGoodsBean.GrouponGoodsModel grouponGoodsModel = grouponGoodsAdapter.getData().get(position);
                if (selectedShopList != null && !selectedShopList.isEmpty()) {

                    for (int i = 0; i < selectedShopList.size(); i++) {
                        GrouponGoodsBean.GrouponGoodsModel model = selectedShopList.get(i);
                        Log.i("ttt", ">>>>>>>>>>>>>>" + model.getId() + "<<<<<" + grouponGoodsModel.getId());
                        if (model.getId() == grouponGoodsModel.getId()) {
                            model.setShuliang(model.getShuliang() + 1);
                            BigDecimal price = new BigDecimal(model.getPrice());
                            if (!TextUtils.isEmpty(model.getDiscount())) {
                                price = price.multiply(new BigDecimal(model.getDiscount())).divide(new BigDecimal(100));
                                model.setDiscounted_price(model.getDiscounted_price().add(new BigDecimal(model.getPrice()).subtract(price)));
                            }
                            BigDecimal heji = price.add(model.getHeji()).setScale(2, RoundingMode.UP);
                            model.setHeji(heji);
                            if (!model.isIs_zengsong()) {
                                zongjia = zongjia.add(price).setScale(2, RoundingMode.UP);
                            }

                            tv_zongjia.setText(zongjia + "");
                            selectedShopAdapter.notifyItemChanged(i);
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

                BigDecimal heji = price.setScale(2, RoundingMode.UP);
                grouponGoodsModel.setHeji(heji);
                grouponGoodsModel.setShuliang(1);
                selectedShopList.add(0, grouponGoodsModel);


                selectedShopAdapter.setNewData(selectedShopList);
                // 滚动到位置 0（第一条）
                selected_LinearLayoutManager.scrollToPosition(0);  // 立即滚动，无动画效果
                tv_zongjian.setText(selectedShopAdapter.getItemCount() + "");
                zongjia = zongjia.add(price).setScale(2, RoundingMode.UP);
                tv_zongjia.setText(zongjia + "");

            }
        });


    }

    //tab货品类型list
    public void OverviewList() {

        Map<String, String> params = new HashMap<>();

        String url = POSApiSerview.POS_URL + POSApiSerview.getGrouponCategory;
        OkHttpUtil.postFormAsync(url, params,this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt", response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isEmpty()) {
                            Gson gson = new Gson();
                            CategoryListBean categoryListBean = gson.fromJson(response, CategoryListBean.class);
                            if (categoryListBean.getCode()==1){
                                if (is_tongbu){
                                    getGrouponGoods();
                                }
                                ArrayList<CategoryListBean.CategoryListModel> models = categoryListBean.getData();
                                CategoryListBean.CategoryListModel categoryListModel = new CategoryListBean.CategoryListModel();
                                categoryListModel.setName("全部");
                                categoryListModel.setSelected(true);
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
                System.err.println("请求失败: " + e.getMessage());
            }
        });


    }

    private String goods_sn = "";
    private String category_ids = "";
    private int grouponGoods_page = 1;
    private ArrayList<GrouponGoodsBean.GrouponGoodsModel> allGrouponGoodsModelList = new ArrayList<>();

    private void getGrouponGoods() {
        Map<String, String> params = new HashMap<>();
        params.put("category_ids", TextUtils.isEmpty(category_ids) ? "" : category_ids);
        params.put("goods_sn", goods_sn);
//        params.put("page", grouponGoods_page + "");
//        params.put("strip", "20");
        String url = POSApiSerview.POS_URL + POSApiSerview.getGrouponGoods2;
        OkHttpUtil.postFormAsync(url, params,this, new OkHttpUtil.OkHttpCallback() {
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
                                if (is_tongbu){
                                    new DeleteShopPopupWindow(MainActivity.this,"数据同步成功",true).show();
                                    is_tongbu=false;
                                }

                                UserUtils.getInstance().setGrouponGoodsBeanJson(MainActivity.this, response);
                                if (grouponGoods_page == 1) {
                                    if (grouponGoodsBean.getData() != null) {
                                        allGrouponGoodsModelList = grouponGoodsBean.getData();
                                        grouponGoodsAdapter.setNewData(grouponGoodsBean.getData());
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
                System.err.println("请求失败: " + e.getMessage());
            }
        });
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
//        hideKeyboard();
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            int keyCode = event.getKeyCode();
            Log.i("ttt", "外部键盘点击" + keyCode);

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
                onClickListener.onClick(dazhe_one_btn);
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F5) {
                onClickListener.onClick(qingkong_btn);
                // 监听外接键盘的返回键
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_F6) {
                onClickListener.onClick(dazhe_all_btn);
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
        }
        return super.dispatchKeyEvent(event);
    }

    //tab货品类型list
    public void getLastOder() {

        Map<String, String> params = new HashMap<>();

        String url = POSApiSerview.POS_URL + POSApiSerview.getLastOder;
        OkHttpUtil.postFormAsync(url, params,this, new OkHttpUtil.OkHttpCallback() {
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
                                    operateDetails(lastOrderBeanArrayList.get(0));
                                }
                            } catch (JSONException e) {
                                throw new RuntimeException(e);
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

    public void operateDetails(LastOrderBean lastOrderBean) {
        Map<String, String> params = new HashMap<>();
        params.put("shop_id", UserUtils.getInstance().getShopDataBean().getData().get(0).getShopuid() + "");
        String url = POSApiSerview.POS_URL + POSApiSerview.operateDetails;
        OkHttpUtil.postFormAsync(url, params,this, new OkHttpUtil.OkHttpCallback() {
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
                            }
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

}