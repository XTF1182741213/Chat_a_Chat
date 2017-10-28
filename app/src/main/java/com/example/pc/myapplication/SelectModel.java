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
import android.widget.Button;

public class SelectModel extends Activity {

    private Button openroom;//建房按钮
    private Button joinroom;//加入房间按钮
    private int servicesign;//服务器接收端的开启标志

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_model);

        //开启单人聊天服务
        servicesign = 1;//将标志位置为1
        Intent intent = new Intent(SelectModel.this, UDPService.class);
        intent.putExtra("servicesign",servicesign);
        startService(intent);

        //获取按钮对象
        openroom =(Button)findViewById(R.id.button);
        joinroom =(Button)findViewById(R.id.button2);
        //设置按钮显示名称
        openroom.setText(R.string.button);
        joinroom.setText(R.string.button2);
        //建房按钮监听事件
        class CalculateListener implements View.OnClickListener {
            public void onClick(View v){
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(SelectModel.this, SignIn.class);
                        startActivity(intent);
                        Log.w("act","wwwww进入");
                        finish();
                    }
                };
                r.run();
            }
        }
        //建房按钮与事件绑定
        openroom.setOnClickListener(new CalculateListener());

        //加入者跳转
        class joinroomclick implements View.OnClickListener {
            public void onClick(View v) {
                //跳转到检测连接页面
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        Intent intent = new Intent();
                        intent.setClass(SelectModel.this, SelectWlan.class);
                        startActivity(intent);
                        finish();
                    }
                };
                r.run();
            }
        }
        //将监听事件与对象进行绑定
        joinroom.setOnClickListener(new joinroomclick());
    }

    //点击返回键的事件,退出程序
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {

            //关闭服务
            servicesign = 3;//将标志位置为3
            Intent intent = new Intent(SelectModel.this, UDPService.class);
            intent.putExtra("servicesign",servicesign);
            startService(intent);
            //关闭窗口
            finish();

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
                Intent intent = new Intent(SelectModel.this, UDPService.class);
                intent.putExtra("servicesign",servicesign);
                startService(intent);
                this.finish();
                break;
        }
        return true;
    }

    //消息框
    private void showMenu() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SelectModel.this);
        builder.setTitle("关于");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // 点击“确定”后，关闭消息框
                AlertDialog.Builder builder = new AlertDialog.Builder(SelectModel.this);
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


