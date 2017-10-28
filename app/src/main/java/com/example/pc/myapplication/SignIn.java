package com.example.pc.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Method;

public class SignIn extends Activity {

    private Button button3;//确定按键
    private Button button4;//取消按键
    private Button button13;//直接进入按键
    private TextView textView;//SSID文本框
    private TextView textView2;//密码文本框
    private TextView textView3;//设备名文本框
    private EditText editText1;//名称编辑框
    private EditText editText2;//密码编辑框
    private CheckBox checkBox = null;//选择按钮
    private WifiManager wifiManager;//获取wifi服务
    private BluetoothAdapter bluetoothAdapter;//获取蓝牙服务
    private String wifiname;//获取SSID
    private String wifipassword;//获取密码
    private int servicesign;//服务器接收端的开启标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        Log.w("act","wwwww进入窗口2");
        //获取对象
        button3 = (Button)findViewById(R.id.button3);
        button4 = (Button)findViewById(R.id.button4);
        button13 = (Button)findViewById(R.id.button13);

        textView = (TextView)findViewById(R.id.textView);
        textView2 = (TextView)findViewById(R.id.textView2);
        textView3 = (TextView)findViewById(R.id.textView3);

        editText1 = (EditText)findViewById(R.id.editText);
        editText2 = (EditText)findViewById(R.id.editText2);

        checkBox = (CheckBox)findViewById(R.id.checkBox);
        //启动wifi服务
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        // 设置各对象文本
        button3.setText(R.string.button3);
        button4.setText(R.string.button4);
        button13.setText(R.string.button13);

        checkBox.setText(R.string.cheakBox);

        textView.setText(R.string.textView);
        textView2.setText(R.string.textView2);
        textView3.setText("用户名："+bluetoothAdapter.getName());

        //选择框监听事件
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    //显示内容
                    editText2.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                else
                    //隐藏内容
                    editText2.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
        //确定按钮的监听事件
        class button3click implements View.OnClickListener {
            public void onClick(View v) {
                wifiname = editText1.getText().toString();//获取SSID
                wifipassword = editText2.getText().toString();//获取密码
                boolean into ;//热点开启标志
                //判断SSID是否为空
                if (wifiname.length() == 0) {
                    Toast.makeText(SignIn.this, "请输入SSID", Toast.LENGTH_SHORT).show();
                    return;
                }
                //是否设置密码
                if(wifipassword.length() == 0){
                    into = setWifiApEnabled(wifiname,wifipassword,false);
                }else if(wifipassword.length() < 8){
                    Toast.makeText(SignIn.this, "密码过短", Toast.LENGTH_SHORT).show();
                    return;
                }else {
                    into = setWifiApEnabled(wifiname, wifipassword, true);
                }
                if(into) {//成功开启热点则跳转到下个页面
                    Toast.makeText(SignIn.this, "热点开启中...", Toast.LENGTH_LONG).show();
                    new Handler().postDelayed(r, 2500);
                }

            }
        }
        //将监听事件与对象进行绑定
        button3.setOnClickListener(new button3click());

        //返回按钮的监听事件
        class button4click implements View.OnClickListener {
            public void onClick(View v) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(SignIn.this, SelectModel.class);
                        startActivity(intent);
                        finish();
                    }
                };
                r.run();
            }
        }
        //将监听事件与对象进行绑定
        button4.setOnClickListener(new button4click());

        class button13click implements View.OnClickListener {
            public void onClick(View v) {
                if(isWifiApEnabled())
                    r.run();
                else
                    Toast.makeText(SignIn.this, "热点未开启", Toast.LENGTH_LONG).show();
            }
        }

        button13.setOnClickListener(new button13click());
    }

    //点击返回键的事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            button4.post(new Runnable(){
                @Override
                public void run() {
                    button4.performClick();
                }
            });

            //返回false,则再次点击仍可触发该事件
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    //开启热点
    public boolean setWifiApEnabled(String wifiname,String wifipassword,boolean sign) {
        //关闭wifi
        wifiManager.setWifiEnabled(false);
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称
            apConfig.SSID = wifiname;
            //判断是否设置密码
            if(sign) {
                //配置热点的加密方式
                apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                //配置热点的密码
                apConfig.preSharedKey = wifipassword;
            }else{
                apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            }
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);//返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig,true);
        } catch (Exception e) {
            Toast.makeText(SignIn.this,"WLAN热点开启失败",Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    Runnable r = new Runnable() {
        @Override
        public void run() {
            //若热点未开启，则二次调用，确保热点开启
            if(!isWifiApEnabled()) {
                if (wifipassword.length() == 0)
                    setWifiApEnabled(wifiname, wifipassword, false);
                else
                    setWifiApEnabled(wifiname, wifipassword, true);
            }
            Intent intent = new Intent();
            intent.setClass(SignIn.this, SelectChatModel.class);
            startActivity(intent);
            finish();
        }
    };

    //判断热点开关是否打开
    public boolean isWifiApEnabled() {
        try {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Toast.makeText(SignIn.this,"WLAN热点未打开",Toast.LENGTH_SHORT).show();
        return false;
    }

    // 创建菜单
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(0, 0, 0, "关于");
        menu.add(0, 1, 1, "退出");
        return super.onCreateOptionsMenu(menu);
    }

    //菜单响应
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 0:
                showMenu();
                break;
            case 1:
                //关闭服务
                servicesign = 3;//将标志位置为3
                Intent intent = new Intent(SignIn.this, UDPService.class);
                intent.putExtra("servicesign",servicesign);
                startService(intent);
                this.finish();
                break;
        }
        return true;
    }

    //消息框
    private void showMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);
        builder.setTitle("关于");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击“确定”后，关闭消息框
                AlertDialog.Builder builder = new AlertDialog.Builder(SignIn.this);
                dialog = builder.show();
                dialog.dismiss();

            }
        });
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setMessage("一款基于UDP协议的局域网通信工具\n中文名：聊一会\n英文名：chat for a while\n" +
                "开发团队：因缺斯厅\n团队成员：Orianna\n                    绯月\n                   " +
                " Bacon\n                    古爷\n                    Lee");
        builder.show();

    }
}
