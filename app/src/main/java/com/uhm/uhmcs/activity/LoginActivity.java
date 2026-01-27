package com.uhm.uhmcs.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.LoginBase;
import com.uhm.uhmcs.bean.ShopDataBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.utils.Utilis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * POS 登录页面
 * 优化点：修复了由于布局重构导致的空指针异常，优化了数字键盘输入逻辑。
 */
public class LoginActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LoginActivity";
    private EditText currEditText; // 当前获得焦点的输入框
    private EditText etUserName, etUserPwd;
    private View btnLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 设置新的重构后的布局文件
        setContentView(R.layout.activity_login);

        initView();
        initKey();
    }

    public void initView() {
        try {
            // 1. 初始化视图控件
            etUserName = findViewById(R.id.etUserName);
            etUserPwd = findViewById(R.id.etUserPwd);

            etUserName.setShowSoftInputOnFocus(false);
            etUserPwd.setShowSoftInputOnFocus(false);


            btnLogin = findViewById(R.id.btnLogin);

            // 2. 初始化数据（回显上次登录的信息）
            etUserName.setText(UserUtils.getInstance().getLoginPhone());
            etUserPwd.setText(UserUtils.getInstance().getLoginPassword());

            // 3. 设置默认焦点
            currEditText = etUserName;
            etUserName.requestFocus();

            // 4. 输入框焦点监听：点击或切换输入框时，更新当前操作对象
            etUserName.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) currEditText = etUserName;
            });
            etUserPwd.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) currEditText = etUserPwd;
            });

            // 额外兼容：如果用户直接点击输入框也要切换
            etUserName.setOnClickListener(v -> currEditText = etUserName);
            etUserPwd.setOnClickListener(v -> currEditText = etUserPwd);

            // 5. 登录按钮点击事件
            btnLogin.setOnClickListener(v -> {
                if (TextUtils.isEmpty(etUserName.getText().toString().trim())) {
                    Toast.makeText(this, "请输入工号", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(etUserPwd.getText().toString().trim())) {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Utilis.isFastClick()) {
                    return;
                }
                logon();
            });

        } catch (Exception ex) {
            Log.e(TAG, "initView Error: " + ex.getMessage());
            Toast.makeText(this, "初始化界面失败", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 初始化数字键盘
     * 采用 ID 绑定模式，彻底解决原先循环遍历布局导致的 NullPointerException
     */
    private void initKey() {
        // 数字键 0-9
        int[] numIds = {
                R.id.key0, R.id.key1, R.id.key2, R.id.key3, R.id.key4,
                R.id.key5, R.id.key6, R.id.key7, R.id.key8, R.id.key9
        };

        for (int id : numIds) {
            View v = findViewById(id);
            if (v != null) v.setOnClickListener(this);
        }

        // 功能键
        View keyClear = findViewById(R.id.keyClear);
        View keyDelete = findViewById(R.id.keyDelete);

        if (keyClear != null) keyClear.setOnClickListener(this);
        if (keyDelete != null) keyDelete.setOnClickListener(this);
    }

    /**
     * 统一处理键盘点击事件
     */
    @Override
    public void onClick(View v) {
        if (currEditText == null) return;

        currEditText.requestFocus(); // 确保点击按键时，输入框依然持有焦点，光标才会持续闪烁
        int id = v.getId();
        int selection = currEditText.getSelectionStart(); // 获取当前光标所在的索引位置

        if (id == R.id.keyClear) {
            // 清除全部内容
            currEditText.setText("");
        } else if (id == R.id.keyDelete) {
            // 在光标位置向前删除一个字符
            if (selection > 0) {
                currEditText.getText().delete(selection - 1, selection);
            }
        } else {
            // 处理数字键：在当前光标位置插入数字
            if (v instanceof Button) {
                String input = ((Button) v).getText().toString();
                currEditText.getText().insert(selection, input);
            }
        }
    }



    /**
     * 执行登录逻辑
     */
    public void logon() {
        Map<String, String> params = new HashMap<>();
        params.put("username", etUserName.getText().toString());
        params.put("password", etUserPwd.getText().toString());
        String url = POSApiSerview.POS_URL + POSApiSerview.login;

        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "响应数据:" + response);
                runOnUiThread(() -> {
                    if (!response.isEmpty()) {
                        Gson gson = new Gson();
                        try {
                            LoginBase loginBase = gson.fromJson(response, LoginBase.class);
                            if (loginBase != null && loginBase.getCode() == 1) {
                                // 保存登录信息
                                UserUtils.getInstance().setLoginBase(LoginActivity.this, loginBase);
                                UserUtils.getInstance().setLoginPassword(LoginActivity.this, etUserPwd.getText().toString());
                                UserUtils.getInstance().setLoginPhone(LoginActivity.this, etUserName.getText().toString());

                                // 设置全局 Token 头部
                                Map<String, String> headers = new HashMap<>();
                                headers.put("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
                                OkHttpUtil.setGlobalHeaders(headers);

                                // 获取门店信息
                                getShopDataBean();
                                Toast.makeText(LoginActivity.this, loginBase.getMsg(), Toast.LENGTH_SHORT).show();
                            } else {
                                String msg = (loginBase != null) ? loginBase.getMsg() : "登录失败";
                                Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            Toast.makeText(LoginActivity.this, "数据解析错误:" + ex.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "网络请求失败", Toast.LENGTH_SHORT).show());
                Log.e(TAG, "请求失败: " + e.getMessage());
            }
        });
    }

    /**
     * 获取门店数据
     */
    private void getShopDataBean() {
        Map<String, String> params = new HashMap<>();
        params.put("mobile", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getMobile());
        params.put("page", "1");
        params.put("strip", "50");
        String url = POSApiSerview.POS_URL + POSApiSerview.shopList;

        OkHttpUtil.postFormAsync(url, params, this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i(TAG, "门店数据:" + response);
                runOnUiThread(() -> {
                    if (!response.isEmpty()) {
                        Gson gson = new Gson();
                        try {
                            ShopDataBean shopDataBean = gson.fromJson(response, ShopDataBean.class);
                            if (shopDataBean.getCode() == 1 && shopDataBean.getData() != null && !shopDataBean.getData().isEmpty()) {
                                UserUtils.getInstance().setShopDataBean(LoginActivity.this, shopDataBean);
                                // 跳转主页
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(LoginActivity.this, "未获取到有效的门店信息", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception ex) {
                            Log.e(TAG, "解析门店数据失败");
                        }
                    } else {
                        Toast.makeText(LoginActivity.this, "门店数据为空", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFailure(IOException e) {
                Log.e(TAG, "获取门店失败: " + e.getMessage());
            }
        });
    }

    @Override
    public void onBackPressed() {
        // POS 应用通常通过返回键直接退出程序
        finishAffinity();
        System.exit(0);
    }
}