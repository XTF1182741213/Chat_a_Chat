package com.example.pc.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class SelectWlan extends Activity {
    private Button button9;//单人聊天跳转按钮
    private Button button14;//多人聊天跳转按钮
    private Button enter_back;//返回按钮
    private String ip ;//热点Ip
    private WifiManager wifiManager;//获取wifi服务
    private TextView ip_name;//建房者ip
    private TextView phone_name;//建房者机名
    private BluetoothAdapter bluetoothAdapter;//获取蓝牙服务（获取手机名）
    private int servicesign;//服务器接收端的开启标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_wlan);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        button9 = (Button)findViewById(R.id.button9);
        button14 = (Button)findViewById(R.id.button14);
        enter_back = (Button)findViewById(R.id.enter_back);
        ip_name = (TextView)findViewById(R.id.ip_name);
        phone_name = (TextView)findViewById(R.id.phone_name);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        button9.setText(R.string.button9);
        button14.setText(R.string.button14);
        ip_name.setText(getLocalIPAddress());
        phone_name.setText("设备名\n"+bluetoothAdapter.getName());

        //单人聊天跳转
        class button9click implements View.OnClickListener {
            public void onClick(View v) {
                //是否连接了wifi
                if(isWifi(SelectWlan.this)) {
                    //获取热点ip
                    Log.w("qsqsq","进来啦");
                    ip = getWlanIp();
                    Intent intent = new Intent();
                    intent.setClass(SelectWlan.this, JoinChat.class);
                    intent.putExtra("wlanip",ip);
                    Log.w("redianip","xxxxx"+ip);

                    startActivity(intent);
                    finish();
                }else{//未连接则跳到wifi界面
                    Log.w("qsqsq","跳不了");
                    Toast.makeText(SelectWlan.this, "请先连接wifi", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS );
                    startActivity(intent);
                }
            }
        }
        //将监听事件与对象进行绑定
        button9.setOnClickListener(new button9click());

        //群聊跳转
        class button14click implements View.OnClickListener {
            public void onClick(View v) {
                //是否连接了wifi
                if(isWifi(SelectWlan.this)) {
                    //获取热点ip
                    Log.w("qsqsq","进来啦");
                    ip = getWlanIp();
                    Intent intent = new Intent();
                    intent.setClass(SelectWlan.this, TogJoinChat.class);
                    intent.putExtra("wlanip",ip);
                    Log.w("redianip","xxxxx"+ip);

                    startActivity(intent);
                    finish();
                }else{//未连接则跳到wifi界面
                    Log.w("qsqsq","跳不了");
                    Toast.makeText(SelectWlan.this, "请先连接wifi", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS );
                    startActivity(intent);
                }
            }
        }
        //将监听事件与对象进行绑定
        button14.setOnClickListener(new button14click());

        //返回按钮点击事件
        class enter_backclick implements View.OnClickListener {
            //返回选择界面
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SelectWlan.this, SelectModel.class);
                startActivity(intent);
                finish();
            }
        }
        enter_back.setOnClickListener(new enter_backclick());
    }

    //点击返回键的事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            enter_back.post(new Runnable(){
                @Override
                public void run() {
                    enter_back.performClick();
                }
            });

            //返回false,则再次点击仍可触发该事件
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    //获取wifi热点ip
    private String getWlanIp() {
        int i;
        DhcpInfo info = wifiManager.getDhcpInfo();
        //取得ip（int）
        i = info.serverAddress;
        //转为字符串
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }
    //  判断wifi是否连接
    private static boolean isWifi(Context mContext) {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            Log.w("qsqsq","zhen");
            return true;
        }
        Log.w("qsqsq","jia");
        return false;
    }

    //获取本机ip
    public static String getLocalIPAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface ni = en.nextElement();
                Enumeration<InetAddress> enIp = ni.getInetAddresses();
                while (enIp.hasMoreElements()) {
                    InetAddress inet = enIp.nextElement();
                    if (!inet.isLoopbackAddress()
                            && (inet instanceof Inet4Address)) {
                        Log.w("Get","wwwww获取本机IP---"+inet.getHostAddress().toString());
                        return inet.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "0.0.0.0";
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
                Intent intent = new Intent(SelectWlan.this, UDPService.class);
                intent.putExtra("servicesign",servicesign);
                startService(intent);
                this.finish();
                break;
        }
        return true;
    }

    //消息框
    private void showMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectWlan.this);
        builder.setTitle("关于");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击“确定”后，关闭消息框
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectWlan.this);
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

