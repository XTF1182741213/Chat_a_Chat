package com.example.pc.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class SelectChatModel extends Activity {

    private Button button5;//单人
    private Button button6;//群聊
    private Button back;//返回按钮
    private TextView ipname;//建房者ip
    private TextView phonename;//建房者机名
    private BluetoothAdapter bluetoothAdapter;//获取蓝牙服务（获取手机名）
    private int servicesign;//服务器接收端的开启标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_chat_model);

        button5 = (Button)findViewById(R.id.button5);
        button6 = (Button)findViewById(R.id.button6);
        back = (Button)findViewById(R.id.back);
        ipname = (TextView)findViewById(R.id.ipname);
        phonename = (TextView)findViewById(R.id.phonename);


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        button5.setText(R.string.button5);
        button6.setText(R.string.button6);
        ipname.setText(getLocalIPAddress());
        phonename.setText("设备名\n"+bluetoothAdapter.getName());

        //单人聊天按钮的监听事件
        class button5click implements View.OnClickListener {
            public void onClick(View v) {
                //跳转到检测连接页面
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(SelectChatModel.this, WlanCheak.class);
                        startActivity(intent);
                        finish();
                    }
                };
                r.run();
            }
        }
        //将监听事件与对象进行绑定
        button5.setOnClickListener(new button5click());

        //多人聊天按钮的监听事件
        class button6click implements View.OnClickListener {
            public void onClick(View v) {
                //跳转到多人聊天页面
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        servicesign = 2;//将标志位置为2
                        Intent intent = new Intent();
                        intent.setClass(SelectChatModel.this, UDPService.class);
                        intent.putExtra("servicesign",servicesign);
                        startService(intent);

                        intent.setClass(SelectChatModel.this,TogHostChat.class);
                        startActivity(intent);
                        finish();
                    }
                };
                r.run();
            }
        }
        //将监听事件与对象进行绑定
        button6.setOnClickListener(new button6click());

        //返回按钮点击事件
        class backclick implements View.OnClickListener {
            //返回登录界面
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SelectChatModel.this, SignIn.class);
                startActivity(intent);
                finish();
            }
        }
        back.setOnClickListener(new backclick());
    }

    //点击返回键的事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            back.post(new Runnable(){
                @Override
                public void run() {
                    back.performClick();
                }
            });

            //返回false,则再次点击仍可触发该事件
            return false;
        }
        return super.onKeyDown(keyCode, event);
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
                Intent intent = new Intent(SelectChatModel.this, UDPService.class);
                intent.putExtra("servicesign",servicesign);
                startService(intent);
                this.finish();
                break;
        }
        return true;
    }

    //消息框
    private void showMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectChatModel.this);
        builder.setTitle("关于");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击“确定”后，关闭消息框
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectChatModel.this);
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
