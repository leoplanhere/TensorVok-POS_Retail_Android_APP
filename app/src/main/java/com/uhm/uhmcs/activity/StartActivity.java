package com.uhm.uhmcs.activity;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.uhm.uhmcs.R;

public class StartActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
    }
}
