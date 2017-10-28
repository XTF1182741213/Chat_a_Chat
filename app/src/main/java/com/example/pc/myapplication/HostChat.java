package com.example.pc.myapplication;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.net.wifi.WifiManager;
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
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class HostChat extends Activity {

    private String ip;//对方ip（房间名）
    private TextView textView4;//内容显示框
    private TextView textView5;//ip显示框
    private Button button7;//发送按钮
    private Button button8;//返回按钮
    private EditText editText3;//输入框
    private ScrollView scrollView1;//滚动条
    private LinearLayout linearLayout1;//布局
    private final Handler mHandler = new Handler();//用于滚动条的自动定位
    private BluetoothAdapter bluetoothAdapter;//获取蓝牙服务
    private ServiceConnection coon;
    private UDPService sBinder;//service对象
    private boolean isBind;//是否绑定服务的标记位
    private String myselfIp ;//保存自己的ip
    private ActivityReceiver activityReceiver;//广播对象
    private WifiManager wifiManager;//获取wifi服务
    private String newText;//保存编辑框的消息
    private int port = 14747;//接收端端口号

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host_chat);

        Log.w("act","wwwww进入创建");

        //成功连接服务后的操作
        coon = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                sBinder = ((UDPService.SimpleBinder)service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(HostChat.this, "连接服务端失败", Toast.LENGTH_SHORT).show();
            }

        };

        //绑定服务
        bindService(new Intent(HostChat.this, UDPService.class), coon, Context.BIND_AUTO_CREATE);
        isBind = true;

        Log.w("act","wwwww绑定成功");

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //注册广播接收者，用于接收新消息
        activityReceiver = new ActivityReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("HostChat.receiver");
        registerReceiver(activityReceiver, intentFilter);

        textView4 = (TextView) findViewById(R.id.textView4);
        textView5 = (TextView) findViewById(R.id.textView5);
        editText3 = (EditText) findViewById(R.id.editText3);
        scrollView1 = (ScrollView) findViewById(R.id.scrollview1);
        linearLayout1 = (LinearLayout)findViewById(R.id.linearLayout1);
        button7 = (Button) findViewById(R.id.button7);
        button8 = (Button) findViewById(R.id.button8);

        // 启动activity时不自动弹出软键盘
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        //加滚动条
        textView4.setMovementMethod(ScrollingMovementMethod.getInstance());

        Log.w("act","wwwww更新内容111");
        myselfIp = getLocalIPAddress();

        Log.w("act","wwwww更新内容2222");

        button7.setText(R.string.button7);
        button8.setText(R.string.button8);

        //获取Intent实例，注意不是new
        Intent intent = getIntent();
        ip = intent.getStringExtra("ip");

        Log.w("act","wwwwwipipip"+ip);

        //textView4.setText("");
        //自动点击发送按钮，用于获取历史数据
        button7.post(new Runnable(){
            @Override
            public void run() {
                button7.performClick();
            }
        });

        textView5.setText(ip);

        //删除未读
        Intent sendIntent = new Intent();
        sendIntent.putExtra("uiReIp", ip);
        sendIntent.setAction("UDPService.receiver");
        sendBroadcast(sendIntent);
        Log.w("act","wwwww删除了吗");

        //发送消息
        button7.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editText3.getText().toString().length() != 0) {
                    //newText = bluetoothAdapter.getName()+"\n"+myselfIp+"\n"+editText3.getText().toString()+"\n\n";
                    newText = bluetoothAdapter.getName()+"\n"+editText3.getText().toString()+"\n\n";
                    sBinder.setServiceSeText(newText);//发送本次显示的内容
                    sBinder.setServiceSeIp(ip);//设置对方ip
                    sBinder.setServiceSePort(port);//设置端口号
                    sBinder.useSend(14949);//指明发送端端口号，并发送消息
                    textView4.setText(textView4.getText().toString()+newText);
                    editText3.setText("");//清空输入框
                    mHandler.post(mScrollToBottom); //滚动到底部
                    Log.w("act","wwwww我点了发送");
                }else {
                    //编辑框为空就更新数据
                    textView4.setText( sBinder.GetSaveData(ip));
                    //滚动到底部
                    mHandler.post(mScrollToBottom);

                    //把已读ip发送回去,用于service更新未读列表
                    Intent sendIntent = new Intent();
                    sendIntent.putExtra("uiReIp", ip);
                    sendIntent.setAction("UDPService.receiver");
                    sendBroadcast(sendIntent);
                    Log.w("act","wwwww我点了发送");
                }
            }
        });

        //返回检测界面，解除绑定并关闭窗口
        button8.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(HostChat.this, ChatRecord.class);
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

            button8.post(new Runnable(){
                @Override
                public void run() {
                    button8.performClick();
                }
            });

            //返回false,则再次点击仍可触发该事件
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    //广播接收类
    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String uiReIp ;//用于保存新信息的ip
            String uiReText ;//用于接收新信息
            //接收ip
            uiReIp = intent.getStringExtra("serviceReIp");
            Log.w("相同吗",ip+"he"+uiReIp);
            if(uiReIp.equals(ip)){
                Log.w("act","wwwww广播");
                //与房间名相同则把textView内容更新
                uiReText = intent.getStringExtra("serviceReText");
                textView4.setText(textView4.getText().toString()+uiReText);
                Log.w("act","wwwww更新内容");
                mHandler.post(mScrollToBottom); //滚动到底部

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

    //滚动条定位到底部的方法
    private Runnable mScrollToBottom = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            int off = linearLayout1.getMeasuredHeight() - scrollView1.getHeight();
            if (off > 0) {
                scrollView1.scrollTo(0, off);
            }
        }
    };

    //获取wifi热点ip

    //此方法只可用于客户端获取服务端，服务端不可使用改方法获取本机ip否则服务端ip不一致
/*   private String getWlanIp() {
        int i;
        DhcpInfo info = wifiManager.getDhcpInfo();
        //取得ip（int）
        i = info.serverAddress;
        //转为字符串
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
   }
*/
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

}


