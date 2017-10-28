package com.example.pc.myapplication;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

public class WlanCheak extends Activity {
    private ListView listView;//显示已连接的客户端ip
    private ArrayList<String> connectedIP;//保存已连接的客户端ip
    private Button button12;//刷新连接ip列表的按钮
    private ImageView imageView3;//跳转至聊天列表的图片
    private int servicesign;//服务器接收端的开启标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wlan_cheak);

        listView = (ListView) findViewById(R.id.listView);
        button12 = (Button) findViewById(R.id.button12);
        imageView3 = (ImageView) findViewById(R.id.imageView3);

        button12.setText(R.string.button12);

        getNewConnectedHotIP();

        //添加点击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String ip = connectedIP.get(position);//保存点击的ip
                //String ip = WlanCheak.this.simple_list_item_1.getItem(position);
                //点击的不是ip栏时，跳转
                if (!ip.equalsIgnoreCase("ip")) {
                    Intent intent = new Intent();
                    intent.putExtra("ip", ip);//传递点击的ip
                    intent.setClass(WlanCheak.this, HostChat.class);
                    startActivity(intent);
                }
            }
        });

        button12.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.w("wwww","wwwwww我点击了按钮12");
                getNewConnectedHotIP();
            }
        });

        //气泡图片点击事件，跳转至聊天列表
        imageView3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(WlanCheak.this, ChatRecord.class);
                        startActivity(intent);
                        onDestroy();
                        finish();
                    }
                };
                r.run();
            }
        });

    }

    private void getNewConnectedHotIP() {
        //存储连入热点的ip
        connectedIP = getConnectedHotIP();
        //将接入者IP显示在listview上
        listView.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, connectedIP));
    }

    //获取接入者的ip地址
    private ArrayList<String> getConnectedHotIP() {
        ArrayList<String> connectedIp = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIp.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIp;
    }

    //点击返回键的事件,跳转至聊天模式选择界面
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            Runnable r = new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent();
                    intent.setClass(WlanCheak.this, SelectChatModel.class);
                    startActivity(intent);
                    onDestroy();
                    finish();
                }
            };
            r.run();

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
                Intent intent = new Intent(WlanCheak.this, UDPService.class);
                intent.putExtra("servicesign",servicesign);
                startService(intent);
                this.finish();
                break;
        }
        return true;
    }

    //消息框
    private void showMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(WlanCheak.this);
        builder.setTitle("关于");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击“确定”后，关闭消息框
                AlertDialog.Builder builder = new AlertDialog.Builder(WlanCheak.this);
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