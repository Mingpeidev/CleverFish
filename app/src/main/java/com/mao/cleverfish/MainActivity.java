package com.mao.cleverfish;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Message;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private TextView temp_text=null;
    private TextView rain_text=null;
    private TextView light_text=null;
    private TextView wet_text=null;
    private TextView fishname_text=null;

    private ImageButton lightcontrol_Btn=null;
    private ImageButton waterexchange_Btn=null;
    private ImageButton addo2_Btn=null;
    private ImageButton addfood_Btn=null;
    private ImageButton addwater_Btn=null;
    private ImageButton heating_Btn=null;
    private ImageButton addfish_Btn=null;
    private ImageButton setting_Btn=null;

    private ImageView light_view=null;
    private ImageView water_view=null;
    private ImageView heatting_view=null;
    private ImageView o2_view=null;

    private int smart;
    private int watertime;
    private int watertemp;
    private int o2time;

    /**
     * 主 变量
     */
    // 主线程Handler
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;

    // Socket变量
    private Socket socket;

    // 线程池
    // 为了方便展示,此处直接采用线程池进行线程管理,而没有一个个开线程
    private ExecutorService mThreadPool;

    /**
     * 接收服务器消息 变量
     */
    // 输入流对象
    InputStream is;

    // 输入流读取器对象
    InputStreamReader isr ;
    BufferedReader br ;

    // 接收服务器发送过来的消息
    String response;
    String rain="";
    String light="";
    String temp="";
    String humi="";
    String control="";

    String light_control="";
    String water_control="";
    String addo2_control="";
    String heating_control="";

    /**
     * 发送消息到服务器 变量
     */
    // 输出流对象
    OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        setContentView(R.layout.activity_main);

        init();

        light_view.setVisibility(View.INVISIBLE);
        water_view.setVisibility(View.INVISIBLE);
        heatting_view.setVisibility(View.INVISIBLE);
        o2_view.setVisibility(View.INVISIBLE);

        // 初始化线程池
        mThreadPool = Executors.newCachedThreadPool();

        mMainHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        rain_text.setText(rain);
                        break;
                    case 1:
                        String x=hexString2binaryString(control);

                        light_control=x.substring(4,5);
                        water_control=x.substring(5,6);
                        addo2_control=x.substring(6,7);
                        heating_control=x.substring(7,8);

                        Log.d("hahacher", "onClick: "+light_control+water_control+addo2_control+heating_control);

                        if (light_control.equals("1")){
                            lightcontrol_Btn.setBackgroundResource(R.drawable.light);
                            light_view.setVisibility(View.VISIBLE);
                        }else if (light_control.equals("0")){
                            lightcontrol_Btn.setBackgroundResource(R.drawable.light1);
                            light_view.setVisibility(View.INVISIBLE);
                        }
                        if (water_control.equals("1")){
                            water_view.setVisibility(View.VISIBLE);
                            waterexchange_Btn.setBackgroundResource(R.drawable.water);
                        }else if (water_control.equals("0")){
                            water_view.setVisibility(View.INVISIBLE);
                            waterexchange_Btn.setBackgroundResource(R.drawable.water1);
                        }
                        if (heating_control.equals("1")){
                            heatting_view.setVisibility(View.VISIBLE);
                            heating_Btn.setBackgroundResource(R.drawable.sun);
                        }else if (heating_control.equals("0")){
                            heatting_view.setVisibility(View.INVISIBLE);
                            heating_Btn.setBackgroundResource(R.drawable.sun1);
                        }
                        if (addo2_control.equals("1")){
                            o2_view.setVisibility(View.VISIBLE);
                            addo2_Btn.setBackgroundResource(R.drawable.o2);
                        }else if (addo2_control.equals("0")){
                            o2_view.setVisibility(View.INVISIBLE);
                            addo2_Btn.setBackgroundResource(R.drawable.o21);
                        }
                        break;
                    case 2:
                        light_text.setText(light);
                        break;
                    case 3:
                        temp_text.setText(temp);
                        break;
                    case 4:
                        wet_text.setText(humi);
                        break;
                    default:break;
                }
            }
        };


        Opensocket();//打开socket

        lightcontrol_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (light_control.equals("1")){
                    Toast.makeText(MainActivity.this,"关灯",Toast.LENGTH_SHORT).show();
                    SendOne("0"+binaryString2hexString("0"+water_control+addo2_control+heating_control));
                }
                if (light_control.equals("0")){
                    Toast.makeText(MainActivity.this,"开灯",Toast.LENGTH_SHORT).show();
                    SendOne("0"+binaryString2hexString("1"+water_control+addo2_control+heating_control));
                }

            }
        });

        waterexchange_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (water_control.equals("1")){
                    Toast.makeText(MainActivity.this,"关水",Toast.LENGTH_SHORT).show();
                    SendOne("0"+binaryString2hexString(light_control+"0"+addo2_control+heating_control));
                }
                if (water_control.equals("0")){
                    Toast.makeText(MainActivity.this,"开水",Toast.LENGTH_SHORT).show();
                    SendOne("0"+binaryString2hexString(light_control+"1"+addo2_control+heating_control));
                }
            }
        });

        heating_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (heating_control.equals("1")){
                    Toast.makeText(MainActivity.this,"取消加热",Toast.LENGTH_SHORT).show();
                    SendOne("0"+binaryString2hexString(light_control+water_control+addo2_control+"0"));
                }
                if (heating_control.equals("0")){
                    Toast.makeText(MainActivity.this,"加热",Toast.LENGTH_SHORT).show();
                    SendOne("0"+binaryString2hexString(light_control+water_control+addo2_control+"1"));
                }
            }
        });

        addo2_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (addo2_control.equals("1")){
                    Toast.makeText(MainActivity.this,"取消供氧",Toast.LENGTH_SHORT).show();
                    SendOne("0"+binaryString2hexString(light_control+water_control+"0"+heating_control));
                }
                if (addo2_control.equals("0")){
                    Toast.makeText(MainActivity.this,"供氧",Toast.LENGTH_SHORT).show();
                    SendOne("0"+binaryString2hexString(light_control+water_control+"1"+heating_control));
                }
            }
        });

        setting_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {//打开可返回数据活动
                Intent intent=new Intent(MainActivity.this,SettingActivity.class);
                startActivityForResult(intent,1);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {//从另一活动返回数据并发送
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if (resultCode==RESULT_OK){

                    smart=data.getIntExtra("smart",0);
                    watertime=data.getIntExtra("watertime",0);
                    watertemp=data.getIntExtra("watertemp",0);
                    o2time=data.getIntExtra("o2time",0);

                    JSONObject jsonObject=new JSONObject();
                    try {
                        jsonObject.put("id",1);
                        jsonObject.put("smart",smart);
                        jsonObject.put("watertemp",watertemp);
                        jsonObject.put("watertime",watertime);
                        jsonObject.put("o2",o2time);

                        SendOne(jsonObject.toString());//发送json到服务器

                        Log.d("json", "onClick: "+jsonObject.toString());

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void init(){
        temp_text=(TextView)findViewById(R.id.temp_text);
        rain_text=(TextView)findViewById(R.id.rain_text);
        light_text=(TextView)findViewById(R.id.light_text);
        wet_text= (TextView) findViewById(R.id.wet_text);
        fishname_text=(TextView)findViewById(R.id.fishname_text);

        lightcontrol_Btn=(ImageButton)findViewById(R.id.lightcontrol_Btn);
        waterexchange_Btn=(ImageButton)findViewById(R.id.waterexchange_Btn);
        addo2_Btn=(ImageButton)findViewById(R.id.addo2_Btn);
        addfood_Btn=(ImageButton)findViewById(R.id.addfood_Btn);
        addwater_Btn=(ImageButton)findViewById(R.id.addwater_Btn);
        heating_Btn=(ImageButton)findViewById(R.id.heating_Btn);
        addfish_Btn=(ImageButton)findViewById(R.id.addfish_Btn);
        setting_Btn=(ImageButton)findViewById(R.id.setting_Btn);

        light_view=(ImageView)findViewById(R.id.light_view);
        water_view=(ImageView)findViewById(R.id.water_view);
        heatting_view=(ImageView)findViewById(R.id.heatting_view);
        o2_view=(ImageView)findViewById(R.id.o2_view);

    }

    private void Opensocket(){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    // 创建Socket对象 & 指定服务端的IP 及 端口号
                    socket = new Socket("192.168.43.191", 8989);
                    // 判断客户端和服务器是否连接成功

                    System.out.println(socket.isConnected());

                    outputStream = socket.getOutputStream();

                    // 步骤2：写入需要发送的数据到输出流对象中
                    outputStream.write(("111"+"\n").getBytes("utf-8"));
                    // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞

                    // 步骤3：发送数据到服务端
                    outputStream.flush();

                    // 步骤1：创建输入流对象InputStream
                    is = socket.getInputStream();

                    // 步骤2：创建输入流读取器对象 并传入输入流对象
                    // 该对象作用：获取服务器返回的数据
                    isr = new InputStreamReader(is);
                    br = new BufferedReader(isr);

                    while (true) {
                        try {

                            // 步骤3：通过输入流读取器对象 接收服务器发送过来的数据
                            response = br.readLine();

                            String s=new String("");
                            String t=new String("");

                            int x = response.split(" ").length;
                            String[] handler = response.split(" ");//02 07 18 00 f1 00 00 01 44 44 ff
                            if (x == 10) {
                                // s传感器短地址，t传感器获取数据
                                s = handler[6]  + handler[5] + handler[7];
                                t = handler[8];
                                System.out.println("网络地址：" + s + " 值：" + t);
                            } else if (x == 11) {
                                s = handler[6]  + handler[5]  + handler[7];//010201 2345
                                t = handler[9]  + handler[8];
                                // System.err.println(s);
                                // System.err.println(t);
                                System.out.println("网络地址：" + s + " 值：" + t);
                            }


                            // 步骤4:通知主线程,将接收的消息显示到界面
                            Message msg = Message.obtain();


                            if(s.equals("61EE01")){
                                rain=t;
                                msg.what=0;
                                System.out.println("rain:"+rain);
                            }else if(s.equals("A00001")){
                                control=t;
                                Log.d("haha", "run: "+control);
                                msg.what=1;
                                System.out.println("control:"+control);
                            }else if(s.equals("15D701")){
                                light=Integer.parseInt(t,16)/100.00+"";
                                msg.what=2;
                                System.out.println("light:"+light);
                            }else if(s.equals("8C4701")) {
                                temp = Integer.parseInt(t,16)/100.00+"";
                                msg.what=3;
                                System.out.println("temp:" + temp);
                            }else if(s.equals("8C4702")){
                                humi=Integer.parseInt(t,16)/100.00+"";
                                msg.what=4;
                                System.out.println("humi:"+humi);
                            }

                            mMainHandler.sendMessage(msg);

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void SendOne(final String data){
        mThreadPool.execute(new Runnable() {
            @Override
            public void run() {

                try {
                    // 步骤1：从Socket 获得输出流对象OutputStream
                    // 该对象作用：发送数据
                    outputStream = socket.getOutputStream();

                    // 步骤2：写入需要发送的数据到输出流对象中
                    outputStream.write((data+"\n").getBytes("utf-8"));
                    // 特别注意：数据的结尾加上换行符才可让服务器端的readline()停止阻塞

                    // 步骤3：发送数据到服务端
                    outputStream.flush();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static String hexString2binaryString(String hexString)
    {
        if (hexString == null || hexString.length() % 2 != 0)
            return null;
        String bString = "", tmp;
        for (int i = 0; i < hexString.length(); i++)
        {
            tmp = "0000" + Integer.toBinaryString(Integer.parseInt(hexString.substring(i, i + 1), 16));
            bString += tmp.substring(tmp.length() - 4);
        }
        return bString;
    }

    public static String binaryString2hexString(String bString)
    {
        if (bString == null || bString.equals("") || bString.length() % 4!= 0)
            return null;
        StringBuffer tmp = new StringBuffer();
        int iTmp = 0;
        for (int i = 0; i < bString.length(); i += 4)
        {
            iTmp = 0;
            for (int j = 0; j < 4; j++)
            {
                iTmp += Integer.parseInt(bString.substring(i + j, i + j + 1)) << (4 - j - 1);
            }
            tmp.append(Integer.toHexString(iTmp));
        }
        return tmp.toString();
    }
}