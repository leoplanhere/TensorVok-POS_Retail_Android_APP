package com.uhm.uhmcs.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.gson.Gson;
import com.uhm.uhmcs.R;
import com.uhm.uhmcs.bean.LoginBase;
import com.uhm.uhmcs.bean.ShopDataBean;
import com.uhm.uhmcs.http.OkHttpUtil;
import com.uhm.uhmcs.http.POSApiSerview;
import com.uhm.uhmcs.popupwindow.DeleteShopPopupWindow;
import com.uhm.uhmcs.utils.UserUtils;
import com.uhm.uhmcs.utils.Utilis;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends Activity {
    private EditText currEditText;
    private EditText etUserName,etUserPwd;
    AppCompatButton btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();
    }
    public void initView(){
        try{
            initKey();
            btnLogin=(AppCompatButton)findViewById(R.id.btnLogin);
            currEditText = (EditText)findViewById(R.id.etUserName);
            etUserName = (EditText)findViewById(R.id.etUserName);
            etUserPwd = (EditText)findViewById(R.id.etUserPwd);
            etUserName.setText(UserUtils.getInstance().getLoginPhone());
            etUserPwd.setText(UserUtils.getInstance().getLoginPassword());
            findViewById(R.id.etUserName).setOnClickListener(v -> {
                currEditText = findViewById(R.id.etUserName);
            });
            findViewById(R.id.etUserPwd).setOnClickListener(v -> {
                currEditText = findViewById(R.id.etUserPwd);
            });
            findViewById(R.id.btnLogin).setOnClickListener(v -> {
                if(TextUtils.isEmpty(etUserName.getText().toString().trim())){
                    Toast.makeText(this,getString(R.string.usr),Toast.LENGTH_SHORT).show();
                    return;
                }
                if(TextUtils.isEmpty(etUserPwd.getText().toString().trim())){
                    Toast.makeText(this,getString(R.string.pwd),Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Utilis.isFastClick()) {
                    return;
                }
                logon();

            });


        }catch (Exception ex){
            Log.i("错误返回",ex.getMessage()+"");
            Toast.makeText(this,ex.getMessage()+"",Toast.LENGTH_SHORT).show();
        }
    }

    public void logon(){
        Map<String, String> params = new HashMap<>();
        params.put("username", etUserName.getText().toString());
        params.put("password",etUserPwd.getText().toString());
        String url = POSApiSerview.POS_URL+POSApiSerview.login;
        OkHttpUtil.postFormAsync(url, params,this, new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt","响应数据:" + response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isEmpty()) {
                            String responseData = response;
                            Gson gson = new Gson();
                            LoginBase loginBase = gson.fromJson(responseData, LoginBase.class);
                            Log.i("登录返回",responseData+"");


                            if(response != null){
                                try{
                                    if(loginBase.getCode() == 1){
                                        UserUtils.getInstance().setLoginBase(LoginActivity.this,loginBase);
                                        UserUtils.getInstance().setLoginPassword(LoginActivity.this,etUserPwd.getText().toString());
                                        UserUtils.getInstance().setLoginPhone(LoginActivity.this,etUserName.getText().toString());
                                        Map<String, String> params = new HashMap<>();
                                        params.put("token", UserUtils.getInstance().getLoginBase().getData().getUserinfo().getToken());
                                        OkHttpUtil.setGlobalHeaders(params);
                                        getShopDataBean();
                                        Toast.makeText(LoginActivity.this,loginBase.getMsg(),Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(LoginActivity.this,loginBase.getMsg(),Toast.LENGTH_SHORT).show();
                                    }
                                }catch (Exception ex){
                                    Toast.makeText(LoginActivity.this,"数据处理错误:"+ex.getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(LoginActivity.this,"请求错误，结果为空",Toast.LENGTH_SHORT).show();
                            }
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

    private void getShopDataBean() {
        Map<String, String> params = new HashMap<>();
        params.put("mobile",UserUtils.getInstance().getLoginBase().getData().getUserinfo().getMobile());
        params.put("page","1");
        params.put("strip","50");
        String url = POSApiSerview.POS_URL+POSApiSerview.shopList;
        OkHttpUtil.postFormAsync(url, params, this,new OkHttpUtil.OkHttpCallback() {
            @Override
            public void onSuccess(String response) {
                Log.i("ttt",response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!response.isEmpty()) {
                            Gson gson = new Gson();
                            ShopDataBean shopDataBean = gson.fromJson(response, ShopDataBean.class);

                            if(shopDataBean.getCode() == 1){
                                Log.i("ttt",shopDataBean.getData().get(0).getName());
                                UserUtils.getInstance().setShopDataBean(LoginActivity.this,shopDataBean);
//                                Intent intent=new Intent();
//                                intent.setClass(LoginActivity.this, LoginActivity.class);
//                                startActivity(intent);
//                                finish();
                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                finish();
                            }else{
                            }
                        }else{
//                            Toast.makeText(LoginActivity.this,"数据处理错误:"+ex.getMessage(),Toast.LENGTH_SHORT).show();
                            Toast.makeText(LoginActivity.this,"请求错误，结果为空",Toast.LENGTH_SHORT).show();
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
    public void onBackPressed() {
        finishAffinity(); // 结束所有关联 Activity
        System.exit(0);  // 完全退出进程（按需使用）
    }

    @SuppressLint("SetTextI18n")
    private void initKey(){
        try{
            LinearLayout llkeyArea = (LinearLayout) findViewById(R.id.llkeyArea);
            for(int i = 0;i<llkeyArea.getChildCount();i++){
                if(i < 3){
                    LinearLayout llNum01_09 = (LinearLayout)llkeyArea.getChildAt(i);
                    for(int j = 0;j<llNum01_09.getChildCount();j++){
                        llNum01_09.getChildAt(j).setOnClickListener(v -> {
                            currEditText.setText(currEditText.getText().toString()+v.getTag().toString());
                        });
                    }
                }else{
                    LinearLayout llNum00_C_D = (LinearLayout)llkeyArea.getChildAt(i);
                    for(int j = 0;j<llNum00_C_D.getChildCount();j++){
                        if(j == 0) {
                            llNum00_C_D.getChildAt(j).setOnClickListener(v -> {
                                currEditText.setText(currEditText.getText().toString()+v.getTag().toString());
                            });
                        }else{
                            llNum00_C_D.getChildAt(j).setOnClickListener(v -> {
                                if(!TextUtils.isEmpty(currEditText.getText().toString())) {
                                    if(v.getTag().toString().equals("c")){
                                        currEditText.setText("");
                                    }else if(v.getTag().toString().equals("D")){
                                        currEditText.setText(currEditText.getText().toString().substring(0,currEditText.getText().toString().length()-1));
                                    }
                                }
                            });
                        }
                    }
                }
            }

        }catch (Exception ex){
            Log.i("错误返回",ex.getMessage()+"");
        }
    }
}
