package com.example.pc.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

public class ChatRecord extends Activity {

    private ListView listView2;//显示有聊过天的ip信息
    private ArrayList<HashMap<String,String>> list;//listview上显示的数据
    private Hashtable<String , String> msgdata = null;//聊过天的ip列表
    private HashSet<String> noRead = null;//保存未读消息的ip
    private String[] from={"ip","type"};//ListView显示内容每一列的列名
    private int[] to={R.id.chatip,R.id.chattype};//ListView显示每一列对应的list_item中控件的id
    private Button button22;//刷新按钮
    private ServiceConnection coon;
    private UDPService sBinder;//service对象
    private boolean isBind;//是否绑定服务的标记位
    private ActivityReceiver activityReceiver;//广播对象
    private ImageView imageView6;//跳转至聊天列表的图片
    private int servicesign;//服务器接收端的开启标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_record);

        coon = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                sBinder = ((UDPService.SimpleBinder)service).getService();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Toast.makeText(ChatRecord.this, "连接服务端失败", Toast.LENGTH_SHORT).show();
            }

        };

        //绑定服务
        bindService(new Intent(ChatRecord.this, UDPService.class), coon, Context.BIND_AUTO_CREATE);
        isBind = true;

        //注册广播接收者，用于接收新消息
        activityReceiver = new ActivityReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ChatRecord.receiver");
        registerReceiver(activityReceiver, intentFilter);

        listView2 = (ListView)findViewById(R.id.listView2) ;
        imageView6 = (ImageView) findViewById(R.id.imageView6);
        button22 = (Button)findViewById(R.id.button22) ;

        button22.setText(R.string.button22);

        //自动点击刷新按钮，用于获取更新列表信息
        button22.post(new Runnable(){
            @Override
            public void run() {
                button22.performClick();
            }
        });

        //刷新按钮事件
        button22.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.w("wwww","wwwwww我点击了按钮22");
                //更新所有的聊天ip
                msgdata = new Hashtable<String , String>();
                msgdata = sBinder.getMsgdata();
                //更新未读ip列表
                noRead = new HashSet<String>();
                noRead = sBinder.getNoRead();

                //在listview中展示信息
                chatlist();
            }

        });

        //listview点击事件
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ip = "";
                Class<?> chatRoom = null;

                //保存点击的ip
                ip = list.get(position).get("ip");
                Log.w("qsqsq","wwww打印"+ip);

                //点击后跳转至对应聊天室
                if(ip.equals("多人聊天室")) {
                    chatRoom = TogHostChat.class;
                }else if(ip.length() != 0){
                    chatRoom = HostChat.class;
                }

                if(chatRoom!=null) {
                    Intent intent = new Intent();
                    intent.putExtra("ip", ip);//传递点击的ip
                    intent.setClass(ChatRecord.this, chatRoom);
                    startActivity(intent);
                    onDestroy();
                    finish();
                }
            }
        });

        imageView6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(ChatRecord.this, WlanCheak.class);
                        startActivity(intent);
                        onDestroy();
                        finish();
                    }
                };
                r.run();
            }
        });

    }

    //将存放数据的容器同适配器关联
    private void chatlist(){
        ArrayList<HashMap<String,String>> list = getchatlist();
        SimpleAdapter adapter=new SimpleAdapter(this,list,R.layout.listview2,from,to);
        //调用ListActivity的setListAdapter方法，为ListView设置适配器
        listView2.setAdapter(adapter);
        Log.w("qsqsq","wwww刷新成功");
    }

    //将列表数据存放到容器中
    private ArrayList<HashMap<String,String>> getchatlist(){

        list = new ArrayList<HashMap<String,String>>();
        HashMap<String,String> map = null;

        Set<String> keys = msgdata.keySet();

        for(String key: keys){
            map = new HashMap<String,String>();
            map.put("ip",key);
            Log.w("qsqsq","wwww123"+key);

            if(noRead.contains(key)){
                map.put("type", "New");
            }else{
                map.put("type", " ");
                Log.w("qsqsq","wwww123444");
            }
            list.add(map);
        }
        return list;
    }

    //解除服务器绑定
    protected void onDestroy() {
        //解除serivce绑定
        if(isBind) {
            unbindService(coon);
            isBind = false;
            Log.w("wwww","wwwwww解绑成功");
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

    //点击返回键的事件,将程序退至后台
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            //退至后台
            moveTaskToBack(false);

            //返回false,则再次点击仍可触发该事件
            return false;
        }
        return super.onKeyDown(keyCode, event);
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
                Intent intent = new Intent(ChatRecord.this, UDPService.class);
                intent.putExtra("servicesign",servicesign);
                startService(intent);
                //关闭窗口

                //解除服务器绑定
                onDestroy();

                this.finish();
                break;
        }
        return true;
    }

    //消息框
    private void showMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ChatRecord.this);
        builder.setTitle("关于");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击“确定”后，关闭消息框
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatRecord.this);
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

    // 广播接收类
    public class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //收到消息则刷新
            button22.post(new Runnable(){
                @Override
                public void run() {
                    button22.performClick();
                }
            });
        }

    }

}
