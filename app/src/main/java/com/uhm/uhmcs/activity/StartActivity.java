package com.uhm.uhmcs.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.Nullable;

import com.uhm.uhmcs.R;
import com.uhm.uhmcs.utils.UserUtils;

public class StartActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        delaymillinon();
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
        }, 3000);
    }
}
