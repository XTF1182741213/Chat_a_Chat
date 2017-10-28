package com.example.pc.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class JoinChat extends Activity {

    private String ip;//热点ip
    private TextView textView6;//内容显示框
    private TextView textView7;//ip显示框
    private Button button10;//发送按钮
    private Button button11;//返回按钮
    private EditText editText4;//输入框
    private ScrollView scrollView2;//滚动条
    private LinearLayout linearLayout2;//布局
    private final Handler mHandler = new Handler();//用于滚动条的自动定位
    private BluetoothAdapter bluetoothAdapter;//获取蓝牙服务
    private ServiceConnection coon;
    private UDPService sBinder;//service对象
    private boolean isBind;//是否绑定服务的标记位
    private String myselfIp ;//保存自己的ip
    private ActivityReceiver activityReceiver;//广播对象
    private String newText;//保存编辑框的消息
    private int port = 14747;//接收端端口号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_chat);
            Log.w("act","wwwww进入创建");

            //成功连接服务后的操作
            coon = new ServiceConnection() {

                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    sBinder = ((UDPService.SimpleBinder)service).getService();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Toast.makeText(JoinChat.this, "连接服务端失败", Toast.LENGTH_SHORT).show();
                }

            };

            //绑定服务
            bindService(new Intent(JoinChat.this, UDPService.class), coon, Context.BIND_AUTO_CREATE);
            isBind = true;

            Log.w("act","wwwww绑定成功");

            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            //注册广播接收者，用于接收新消息
            activityReceiver = new ActivityReceiver();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("JoinChat.receiver");
            registerReceiver(activityReceiver, intentFilter);

            textView6 = (TextView) findViewById(R.id.textView6);
            textView7 = (TextView) findViewById(R.id.textView7);
            editText4 = (EditText) findViewById(R.id.editText4);
            scrollView2 = (ScrollView) findViewById(R.id.scrollview2);
            linearLayout2 = (LinearLayout)findViewById(R.id.linearLayout2);
            button10 = (Button) findViewById(R.id.button10);
            button11 = (Button) findViewById(R.id.button11);

            // 启动activity时不自动弹出软键盘
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //加滚动条
            textView6.setMovementMethod(ScrollingMovementMethod.getInstance());

            Log.w("act","wwwww更新内容111");

            Log.w("act","wwwww更新内容2222");

            button10.setText(R.string.button10);
            button11.setText(R.string.button11);

            //获取本机ip（客户端）
            myselfIp = getLocalIPAddress();



            //获取Intent实例，注意不是new
            Intent intent = getIntent();
            ip = intent.getStringExtra("wlanip");

            Log.w("duifangip","wwww"+ip);

            //textView6.setText("");
            //自动点击发送按钮，用于获取历史数据
            button10.post(new Runnable(){
                @Override
                public void run() {
                    button10.performClick();
                }
            });
            textView7.setText(ip);

            //删除未读
            Intent sendIntent = new Intent();
            sendIntent.putExtra("uiReIp", ip);
            sendIntent.setAction("UDPService.receiver");
            sendBroadcast(sendIntent);
            Log.w("act","wwwww删除了吗");

            //发送消息
            button10.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(editText4.getText().toString().length() != 0) {
                        //newText = bluetoothAdapter.getName()+"\n"+myselfIp+"\n"+editText4.getText().toString()+"\n\n";
                        newText = bluetoothAdapter.getName()+"\n"+editText4.getText().toString()+"\n\n";
                        sBinder.setServiceSeText(newText);//发送本次显示的内容
                        sBinder.setServiceSeIp(ip);//设置对方ip
                        sBinder.setServiceSePort(port);//设置端口号
                        sBinder.useSend(14949);//指明发送端端口号，并发送消息
                        textView6.setText(textView6.getText().toString()+newText);
                        mHandler.post(mScrollToBottom); //滚动到底部
                        editText4.setText("");//清空输入框
                    }else {
                        //编辑框为空就更新数据
                        Log.w("act","点了一次");
                        textView6.setText( sBinder.GetSaveData(ip));
                        mHandler.post(mScrollToBottom); //滚动到底部
                    }
                }
            });

            //返回检测界面，解除绑定并关闭窗口
            button11.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent();
                            intent.setClass(JoinChat.this, SelectWlan.class);
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

            button11.post(new Runnable(){
                @Override
                public void run() {
                    button11.performClick();
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
            int off = linearLayout2.getMeasuredHeight() - scrollView2.getHeight();
            if (off > 0) {
                scrollView2.scrollTo(0, off);
            }
        }
    };

    //获取本机(内网)ip,仅非热点端可调用
    //方法一：红米机测试，取得乱码，中兴机正常
  /*  public String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> mEnumeration = NetworkInterface.getNetworkInterfaces(); mEnumeration.hasMoreElements();) {
                NetworkInterface intf = mEnumeration.nextElement();
                for (Enumeration<InetAddress> enumIPAddr = intf.getInetAddresses(); enumIPAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIPAddr.nextElement();
                    // 如果不是回环地址
                    if (!inetAddress.isLoopbackAddress()) {
                        // 直接返回本地IP地址
                        Log.w("Get","wwwww获取本机IP1"+inetAddress.getHostAddress().toString());
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.w("Get","wwwww获取本机IP-报错"+ex);
        }
        Log.w("Get","wwwww获取本机IPkong");
        return null;
    } */
    //方式二：红米、中兴均正常
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

    public class ActivityReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                String uiReIp ;//用于接收新信息的ip
                String uiReText ;//用于接收新信息
                //接收ip
                uiReIp = intent.getStringExtra("serviceReIp");
                if(uiReIp.equals(ip)){
                    Log.w("act","wwwww广播");
                    //与房间名相同则把textView内容更新
                    uiReText = intent.getStringExtra("serviceReText");
                    textView6.setText(textView6.getText().toString()+uiReText);
                    Log.w("act","wwwww更新内容");

                    //滚动到底部
                    mHandler.post(mScrollToBottom);

                    //把已读ip发送回去,用于service更新未读列表
                    Intent sendIntent = new Intent();
                    sendIntent.putExtra("uiReIp", uiReIp);
                    sendIntent.setAction("UDPService.receiver");
                    sendBroadcast(sendIntent);
                    Log.w("act","wwwww广播成功");
                }
            }
        }

        protected void onDestroy() {
            //解除serivce绑定
            if(isBind) {
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

    }




