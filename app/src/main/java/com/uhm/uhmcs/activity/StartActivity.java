package com.uhm.uhmcs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.utils.UserUtils;

import constant.UiType;
import model.UiConfig;
import model.UpdateConfig;
import update.UpdateAppUtils;

public class StartActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        delaymillinon();
//        UpdateAppUtils.init(StartActivity.this);
//        UpdateConfig updateConfig=new UpdateConfig();
//        updateConfig.setAlwaysShow(true);//非强制更新时是否每次显示弹窗
//        updateConfig.setAlwaysShowDownLoadDialog(true);//飞强制更新时是否显示进度条
//        updateConfig.setForce(true);//是否强制更新
//        updateConfig.setApkSaveName("优海猫-收银");
//        updateConfig.setApkSavePath(Environment.getExternalStorageDirectory().getAbsolutePath() +"/UHM");
//        updateConfig.isShowNotification();
//        updateConfig.setShowDownloadingToast(true);
//        updateConfig.setServerVersionName("1.0.1");
//        UiConfig uiConfig = new UiConfig();
//        uiConfig.setDownloadingBtnText("下载中");
//        uiConfig.setUiType(UiType.PLENTIFUL);
//        UpdateAppUtils
//                .getInstance()
//                .apkUrl("https://img0.baidu.com/it/u=1494226515,116279625&fm=253&fmt=auto&app=138&f=JPEG?w=787&h=500")
//                .updateConfig(updateConfig)
//                .uiConfig(uiConfig)
//                .updateTitle("发现新版本"+"1.0.1")
//                .updateContent("版本更新优化")
//                .update();
    }

    public void delaymillinon(){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("ttt",">>>>>>>>>>>>>");

                if (UserUtils.getInstance().getLoginBase()!=null&&UserUtils.getInstance().getShopDataBean()!=null){
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                }else {
                    Intent intent=new Intent();
                    intent.setClass(StartActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }, 2000);
    }
}
