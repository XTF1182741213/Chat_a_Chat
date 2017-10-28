package com.example.pc.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;

public class MainActivity extends Activity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //等待一定时间后界面跳转
        new Handler().postDelayed(r, 2000);
    }
    Runnable r = new Runnable() {
            @Override
        public void run() {
            Intent intent = new Intent();
            //界面跳转
            intent.setClass(MainActivity.this,SelectModel.class);
            startActivity(intent);
            //关闭窗口
            finish();
        }
    };
}
