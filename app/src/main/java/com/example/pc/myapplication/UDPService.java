package com.example.pc.myapplication;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;

import static android.os.SystemClock.sleep;

public class UDPService extends Service {
    private Hashtable<String , String> msgdata = new Hashtable<String , String>();//保存ip和聊天记录
    private HashSet<String> noRead = new HashSet<String>();//保存未读消息的ip
    private Vibrator vibrator = null;//手机震动服务
    public SimpleBinder sBinder;//service对象
    private String serviceSeText;//记录activity发送的信息的文本
    private String serviceSeIp;//记录activity发送的信息的目的
    private int serviceSePort;////记录activity发送的信息的目的的端口号
    private int servicesign;//服务器接收端的开启标志

    public UDPService() {
    }

    /**
     * 在 Local Service 中直接继承 Binder 而不是 IBinder,
     * Binder 实现了 IBinder 接口，这样更简单。
     */
    public class SimpleBinder extends Binder {
        /**
         * 获取 Service 实例
         * @return
         */
        public UDPService getService() {
            return UDPService.this;
        }
    }

    public IBinder onBind(Intent intent) {
        // 返回 SimpleBinder 对象
        return sBinder;
    }

    public void onCreate() {
        super.onCreate();

        //注册广播接收者，用于收到新消息时通知activity
        ServiceRecevier serviceRecevier = new ServiceRecevier();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("UDPService.receiver");
        registerReceiver(serviceRecevier, intentFilter);

        //创建震动对象
        vibrator=(Vibrator)getSystemService(Service.VIBRATOR_SERVICE);

        // 创建 SimpleBinder
        sBinder = new SimpleBinder();
    }

    //启动service时运行
    public int onStartCommand(Intent intent, int flag, int startId) {
        super.onStartCommand(intent,flag, startId);

        if(intent != null) {

            DatagramSocket rece = null;
            servicesign = intent.getIntExtra("servicesign",-1);
            if(servicesign == 1) {
                try {
                    //单人聊天接收端
                    rece = new DatagramSocket(14747);
                } catch (SocketException e) {
                    e.printStackTrace();
                    Log.w("Rece", "wwwww创建出报错" + e);
                }

                //开辟新线程
                new Thread(new Rece(rece)).start();
                Log.w("fuw1","www服务1启动了");
            }else if(servicesign == 2){
                try {
                    //多人聊天接收端
                    rece = new DatagramSocket(15757);
                } catch (SocketException e) {
                    e.printStackTrace();
                    Log.w("Rece", "wwwww创建出报错" + e);
                }

                //开辟新线程
                new Thread(new TogRece(rece)).start();
                Log.w("fuw2","www服务2启动了");
            }else if(servicesign == 3){
                UDPService.this.stopSelf();
                Log.w("fuw2","www我停止了444");
            }

        }
        //服务异常中断时自动重启，并传入最后一个intent
        return START_REDELIVER_INTENT;
    }

    public void onDestroy() {
        super.onDestroy();
    }

    //聊天接收消息类
    public class Rece implements Runnable{

        private DatagramSocket ds ;

        public Rece(DatagramSocket ds) {
            this.ds = ds;
        }

        @Override
        public void run() {

            String serviceReIp;//发送者IP

            try {
                while (true) {

                    // 创建数据包。
                    byte[] buf = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);

                    // 使用接收方法将数据存储到数据包中。阻塞式的。
                    ds.receive(dp);

                    Log.w("Rece", "wwwww消息以收到");
                    //判断是否为群聊信息
                    if (dp.getPort() == 14949) {//不是群聊则获取发送者ip

                        //存储发送者IP
                        serviceReIp = dp.getAddress().getHostAddress();

                    }else{
                        serviceReIp = "多人聊天室";
                    }
                        //存储信息内容
                        String serviceReText = new String(dp.getData(), 0, dp.getLength());

                        //调用SavaData方法保存数据
                        SaveData(serviceReIp, serviceReText);

                        //将发送者的ip写入未读消息列表中
                        AddNotReadMsg(serviceReIp);

                        //发送广播通知activity
                        Intent intent = new Intent();

                        //传递发送者IP给activity
                        intent.putExtra("serviceReIp", serviceReIp);

                        //传递发送者信息内容给activity
                        intent.putExtra("serviceReText", serviceReText);

                        intent.setAction("JoinChat.receiver");
                        sendBroadcast(intent);
                        Log.w("Rece", "wwwww消息已广播1");
                        intent.setAction("HostChat.receiver");
                        sendBroadcast(intent);
                        Log.w("Rece", "wwwww消息已广播2");
                        intent.setAction("TogJoinChat.receiver");
                        sendBroadcast(intent);
                        Log.w("Rece", "wwwww消息已广播4");
                        intent.setAction("ChatRecord.receiver");
                        sendBroadcast(intent);

                        //手机震动
                        vibrator.vibrate(new long[]{1000, 50, 50, 100, 50}, -1);

                    }
                }catch(Exception e){
                    Log.w("Rece", "wwwww信息接收处-报错" + e);
                }
            }
    }

    //多人聊天热点端接收消息类
    public class TogRece implements Runnable{

        private DatagramSocket ds ;

        public TogRece(DatagramSocket ds) {
            this.ds = ds;
        }

        @Override
        public void run() {
            try {
                while (true) {

                    // 创建数据包。
                    byte[] buf = new byte[1024];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);

                    // 使用接收方法将数据存储到数据包中。阻塞式的。
                    ds.receive(dp);

                    Log.w("Rece","wwwww消息以收到");

                    //存储信息内容
                    String serviceReText = new String(dp.getData(), 0, dp.getLength());

                    //调用SavaData方法保存数据
                    SaveData("多人聊天室",serviceReText);

                    //将发送者的ip写入未读消息列表中
                    AddNotReadMsg("多人聊天室");

                    //获取接入者信息
                    ArrayList<String> ConnectedHotIP = getConnectedHotIP();
                    //将信息分别发给加入热点的接收端
                    for(int i = 0;i < ConnectedHotIP.size(); i ++){
                        setServiceSeText(serviceReText);//设置发送的内容
                        if(!ConnectedHotIP.get(i).equals("IP")) {
                            setServiceSeIp(ConnectedHotIP.get(i));//设置接收者ip
                            setServiceSePort(14747);//设置接收端端口号
                            useSend(15959+i);//指明发送端端口号，并发送消息
                            sleep(50);
                            Log.w("abc","wwwww我群发了啊======"+i+"========"+ConnectedHotIP.get(i));
                        }
                    }

                    //发送广播通知activity
                    Intent intent = new Intent();

                    //传递发送者IP给activity
                    intent.putExtra("serviceReIp","多人聊天室");

                    //传递发送者信息内容给activity
                    intent.putExtra("serviceReText", serviceReText);

                    intent.setAction("JoinChat.receiver");
                    sendBroadcast(intent);
                    Log.w("Rece","wwwww消息已广播1");
                    intent.setAction("HostChat.receiver");
                    sendBroadcast(intent);
                    Log.w("Rece","wwwww消息已广播2");
                    intent.setAction("TogHostChat.receiver");
                    sendBroadcast(intent);
                    Log.w("Rece","wwwww消息已广播3");
                    intent.setAction("ChatRecord.receiver");
                    sendBroadcast(intent);

                    //如果不是自己发送的信息，则手机震动
                    if(!dp.getAddress().getHostAddress().equals(getLocalIPAddress())) {
                        Log.w("abc","wwwwww这是谁的ip===="+dp.getAddress().getHostAddress()+"=========="+getLocalIPAddress());
                        vibrator.vibrate(new long[]{1000, 50, 50, 100, 50}, -1);
                    }
                }
            } catch (Exception e) {
                Log.w("Rece","wwwww信息接收处-报错"+e);
            }
        }
    }


    //调用发送消息的函数
    // *port合法参数：15959--群聊信息发送端口    15959+i--动态群聊信息发送端口     14949--单聊信息发送端口
    public void useSend(int port) {

                //创建单人聊天发送端
                DatagramSocket send = null;
                try {
                    send = new DatagramSocket(port);
                    Log.w("Rece", "wwwww消息以接收7ooo");
                } catch (SocketException e) {
                    e.printStackTrace();
                    Log.w("Rece", "wwwww调用失败" + e);
                }
                //开启新线程
                new Thread(new Send(send)).start();
            }

    //存放要发送的文本的方法
    public void setServiceSeText(String serviceSeText){
        this.serviceSeText = serviceSeText;
    }
    //存放发送的目的IP的方法
    public void setServiceSeIp(String serviceSeIp){
        this.serviceSeIp = serviceSeIp;
    }

    public void setServiceSePort(int serviceSePort){
        this.serviceSePort = serviceSePort;
    }

    //发送消息类
    public class Send implements Runnable {

        private DatagramSocket ds;

        public Send(DatagramSocket ds){
            this.ds = ds;
        }

        @Override
        public void run() {
            try {
                //发送的数据
                String line = serviceSeText;

                    byte[] buf = line.getBytes();
                    DatagramPacket dp =
                            new DatagramPacket(buf,buf.length, InetAddress.getByName(serviceSeIp),serviceSePort);
                    //发送数据
                Log.w("Send","wwwww消息将发送");
                Log.w("Send","wwwww消息将发送------"+serviceSeIp);
                Log.w("Send","wwwww消息将发送------"+serviceSePort);
                Log.w("Send","wwwww消息将发送------"+buf.length);
                Log.w("Send","wwwww消息将发送------"+ds);
                    ds.send(dp);
                Log.w("Send","wwwww消息已发送");
                    //调用SavaData方法保存数据,只有非群聊时保存
                    if(serviceSePort == 14747) {
                        SaveData(serviceSeIp, serviceSeText);
                        Log.w("Send","wwwww消息已存储");
                    }
                Log.w("Send","wwwww消息wei存储");
                //关闭资源
                ds.close();
                Log.w("HAHAH","wwwwwwWO GUAN DIAO LE A ");
            } catch (Exception e) {
                Log.w("Send","wwwww消息发送端-报错"+e);
            }
        }
    }

    //存储聊天记录
    public void SaveData(String ip,String msg){
        String str = msg;//临时存放聊天数据
        //检查ip是否存在
        // 若存在，则将内容续写
        // 若不存在，则创建
        if(msgdata.containsKey(ip)){
            Log.w("sssss","我进来了");
            str = msgdata.get(ip);//获取已存记录
            str = str + msg;//续写
            msgdata.put(ip,str);
            Log.w("sssss","我进来了---"+str);
        }else {
            Log.w("sssss","我没进来了");
            msgdata.put(ip, str);
            Log.w("sssss","我没进来了---"+str);
        }
    }

    //读取指定ip数据
    public String GetSaveData(String ip){
        String str = "";
        Log.w("sdfgs","wwwww猜猜我进来没");
        if(msgdata.containsKey(ip))
            str = msgdata.get(ip);
        return str;
    }

    //获取所有聊天的ip列表对象
    public Hashtable<String,String> getMsgdata(){
        return msgdata;
    }

    //存储单人未读信息ip列表
    public void AddNotReadMsg(String noReadIp){

        //列表中不存在则添加ip
        if(!noRead.contains(noReadIp)){
            noRead.add(noReadIp);
        }

    }

    //获取未读信息ip列表对象
    public HashSet<String> getNoRead(){
        return noRead;
    }

    //删除未读列表信息
    public void DelNotReadMsg(String readIp){
        if(noRead.contains(readIp)){
            noRead.remove(readIp);
            Log.w("act","wwwww删除了");
        }
    }

    //广播类
    public class ServiceRecevier extends BroadcastReceiver {

        @Override
        //收到广播则把ip从未读列表中删除
        public void onReceive(Context context, Intent intent) {
            String readIp;//存放已读信息的ip
            readIp = intent.getStringExtra("uiReIp");
            DelNotReadMsg(readIp);
        }

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
            Log.w("WWWWWWWW","wwwwww我失败了");
        }
        return connectedIp;
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



