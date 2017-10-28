package com.example.pc.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class TogHostChat extends Activity {

    private String roomname = "多人聊天室";//群聊房间名
    private TextView textView11;//内容显示框
    private TextView textView8;//ip显示框
    private Button button16;//发送按钮
    private Button button15;//返回按钮
    private EditText editText5;//输入框
    private ScrollView scrollView3;//滚动条
    private LinearLayout linearLayout3;//布局
    private final Handler mHandler = new Handler();//用于滚动条的自动定位
    private BluetoothAdapter bluetoothAdapter;//获取蓝牙服务
    private ServiceConnection coon;
    private UDPService sBinder;//service对象
    private boolean isBind;//是否绑定服务的标记位
    private String myselfIp;//保存自己的ip
    private ActivityReceiver activityReceiver;//广播对象
    private WifiManager wifiManager;//获取wifi服务
    private String newText;//保存编辑框的消息
    private int port = 15757;//接收端端口号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tog_host_chat);

        //成功连接服务后的操作
        coon = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                sBinder = ((UDPService.SimpleBinder) service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(TogHostChat.this, "连接服务端失败", Toast.LENGTH_SHORT).show();
            }

        };

        //绑定服务
        bindService(new Intent(TogHostChat.this, UDPService.class), coon, Context.BIND_AUTO_CREATE);
        isBind = true;

        Log.w("act", "wwwww绑定成功");

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //注册广播接收者，用于接收新消息
        activityReceiver = new ActivityReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("TogHostChat.receiver");
        registerReceiver(activityReceiver, intentFilter);

        textView11 = (TextView) findViewById(R.id.textView11);
        textView8 = (TextView) findViewById(R.id.textView8);
        editText5 = (EditText) findViewById(R.id.editText5);
        scrollView3 = (ScrollView) findViewById(R.id.scrollview3);
        linearLayout3 = (LinearLayout)findViewById(R.id.linearLayout3);
        button16 = (Button) findViewById(R.id.button16);
        button15 = (Button) findViewById(R.id.button15);

        // 启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //加滚动条
        textView11.setMovementMethod(ScrollingMovementMethod.getInstance());

        Log.w("act", "wwwww更新内容111");
        myselfIp = getLocalIPAddress();

        Log.w("act", "wwwww更新内容2222");

        button16.setText(R.string.button16);
        button15.setText(R.string.button15);

        //textView11.setText("");
        //自动点击发送按钮，用于获取历史数据
        button16.post(new Runnable() {
            @Override
            public void run() {
                button16.performClick();
            }
        });

        textView8.setText(roomname);

        //删除未读
        Intent sendIntent = new Intent();
        sendIntent.putExtra("uiReIp", roomname);
        sendIntent.setAction("UDPService.receiver");
        sendBroadcast(sendIntent);
        Log.w("act", "wwwww删除了吗");

        //发送消息
        button16.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText5.getText().toString().length() != 0) {
                    //newText = bluetoothAdapter.getName() + "\n" + myselfIp + "\n" + editText5.getText().toString() + "\n\n";
                    newText = bluetoothAdapter.getName() + "\n" + editText5.getText().toString() + "\n\n";
                    sBinder.setServiceSeText(newText);//发送本次显示的内容
                    sBinder.setServiceSeIp(myselfIp);//设置服务器ip
                    sBinder.setServiceSePort(port);//设置端口号
                    sBinder.useSend(15959);//发送消息
                    editText5.setText("");//清空输入框
                } else {
                    //编辑框为空就更新数据
                    textView11.setText(sBinder.GetSaveData(roomname));
                    mHandler.post(mScrollToBottom); //滚动到底部

                    //把已读ip发送回去,用于service更新未读列表
                    Intent sendIntent = new Intent();
                    sendIntent.putExtra("uiReIp", roomname);
                    sendIntent.setAction("UDPService.receiver");
                    sendBroadcast(sendIntent);
                }
            }
        });

        //返回聊天模式选择界面，解除绑定并关闭窗口
        button15.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(TogHostChat.this,ChatRecord.class);
                        startActivity(intent);
                        onDestroy();
                        finish();
                    }
                };
                r.run();
            }
        });
    }

    //点击返回键的事件
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            button15.post(new Runnable(){
                @Override
                public void run() {
                    button15.performClick();
                }
            });

            //返回false,则再次点击仍可触发该事件
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    //滚动条定位到底部的方法
    private Runnable mScrollToBottom = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            int off = linearLayout3.getMeasuredHeight() - scrollView3.getHeight();
            if (off > 0) {
                scrollView3.scrollTo(0, off);
            }
        }
    };

    //广播接收类
    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String uiReIp;//用于保存新信息的ip
            String uiReText;//用于接收新信息
            //接收ip
            uiReIp = intent.getStringExtra("serviceReIp");
            Log.w("相同吗", roomname + "he" + uiReIp);
            if (uiReIp.equals(roomname)) {
                Log.w("act", "wwwww广播");
                //与房间名相同则把textView内容更新
                uiReText = intent.getStringExtra("serviceReText");
                textView11.setText(textView11.getText().toString() + uiReText);
                Log.w("act", "wwwww更新内容");

                //滚动到底部
                mHandler.post(mScrollToBottom);

                //把已读ip发送回去,用于service更新未读列表
                Intent sendIntent = new Intent();
                sendIntent.putExtra("uiReIp", uiReIp);
                sendIntent.setAction("UDPService.receiver");
                sendBroadcast(sendIntent);
                Log.w("act", "wwwww广播成功");
            }
        }
    }

    protected void onDestroy() {
        //解除serivce绑定
        if (isBind) {
            unbindService(coon);
            isBind = false;
        }
        //注销广播
        //unregisterReceiver(activityReceiver);
        try {
            unregisterReceiver(activityReceiver);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("Receiver not registered")) {
                // Ignore this exception. This is exactly what is desired
            } else {
                // unexpected, re-throw
                throw e;
            }
        }
        super.onDestroy();
    }

    //通用的获取本机ip方法（客户端/服务端）
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
                        Log.w("Get", "wwwww获取本机IP---" + inet.getHostAddress().toString());
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
}
