package com.mao.cleverfish;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mingpeidev on 2018/9/12.
 */

public class SettingActivity extends AppCompatActivity{

    private List<String> fishList=new ArrayList<>();

    private Handler UIHandler;

    private int watertemp;
    private int o2time;
    private int smart;
    private int watertime;

    //popwindow
    private RecyclerView popwindow_recycleview=null;
    private TextView popwindow_item=null;

    private Button watertemp_Btn;
    private Button watertime_Btn;
    private Button o2time_Btn;
    private Button smart_Btn;
    private Button submit_Btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar=getSupportActionBar();
        if (actionBar!=null){
            actionBar.hide();
        }
        setContentView(R.layout.seetting);

        watertemp_Btn=(Button)findViewById(R.id.watertemp_Btn);
        watertime_Btn=(Button)findViewById(R.id.watertime_Btn);
        o2time_Btn=(Button)findViewById(R.id.o2time_Btn);
        smart_Btn=(Button)findViewById(R.id.smart_Btn);
        submit_Btn=(Button)findViewById(R.id.submit_Btn);

        getDatasync();

        UIHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0:
                        initView();
                        break;
                }
            }
        };


        watertemp_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final View view1=View.inflate(SettingActivity.this,R.layout.fishpopwindow,null);
                final PopupWindow popupWindow=new PopupWindow(view1, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                popupWindow.setFocusable(true);
                popupWindow.setAnimationStyle(R.style.my_popwindow);
                popupWindow.update();
                popupWindow.showAtLocation(view1, Gravity.RIGHT|Gravity.CENTER_HORIZONTAL,0,0);

                popwindow_recycleview=view1.findViewById(R.id.popwindow_recycleview);

                LinearLayoutManager linearLayoutManager=new LinearLayoutManager(view1.getContext());//瀑布流
                popwindow_recycleview.setLayoutManager(linearLayoutManager);
                final Recycleview_Adapter recycleview_adapter=new Recycleview_Adapter(fishList);//绑定适配器
                popwindow_recycleview.setAdapter(recycleview_adapter);
                popwindow_recycleview.addItemDecoration(new DividerItemDecoration(view1.getContext(),DividerItemDecoration.VERTICAL));//添加分割线

                popwindow_item=view1.findViewById(R.id.popwindow_item);


                String[] a={"16","17","18","19","20","21","22","23","24","25","26","27","28","29","30","31","32"};
                int b=-1;
                if (fishList.isEmpty()){
                    for (int i=0;i<a.length;i++){
                        fishList.add(a[i]);
                    }}



                Log.d("haha", "onClick: "+b);

                recycleview_adapter.setOnItemClickListener(new Recycleview_Adapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {

                        View view1=popwindow_recycleview.getChildAt(position);
                        TextView id1=view1.findViewById(R.id.popwindow_item);
                        String id2=id1.getText().toString();

                        watertemp_Btn.setText(id2+"℃");
                        watertemp=Integer.valueOf(id2);

                        popupWindow.dismiss();
                    }
                });
            }
        });

        watertime_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(SettingActivity.this,android.R.style.Theme_Holo_Light_Panel, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourofday, int minute) {
                        watertime= (hourofday*60+minute)*60000;
                        watertime_Btn.setText(hourofday+"小时"+minute+"分钟");
                    }
                },0,0,true).show();
            }
        });

        o2time_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(SettingActivity.this,android.R.style.Theme_Holo_Light_Panel, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourofday, int minute) {
                        o2time= (hourofday*60+minute)*60000;
                        o2time_Btn.setText(hourofday+"小时"+minute+"分钟");
                    }
                },0,0,true).show();
            }
        });

        smart_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (smart_Btn.getText().equals("已关闭，开启？")){
                    smart=1;
                    Toast.makeText(SettingActivity.this,"智能控制开关选择为开",Toast.LENGTH_SHORT).show();
                    smart_Btn.setText("已开启，关闭？");
                }else if (smart_Btn.getText().equals("已开启，关闭？")){
                    smart=0;
                    Toast.makeText(SettingActivity.this,"智能控制开关选择为关",Toast.LENGTH_SHORT).show();
                    smart_Btn.setText("已关闭，开启？");
                }
            }
        });

        submit_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                            Request request = new Request.Builder()
                                    .url("http://192.168.43.191:8080/ssm-manager/updataSetting?id=1&smart="+smart+"&water="+watertime+"&wendu="+watertemp+"&o2="+o2time)//请求接口。如果需要传参拼接到接口后面。
                                    .build();//创建Request 对象
                            Response response = null;
                            response = client.newCall(request).execute();//得到Response 对象
                            if (response.isSuccessful()) {

                                Log.d("kwwl","response.code()data=="+response.code());
                                Log.d("kwwl","response.message()data=="+response.message());
                                //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
                Intent intent=new Intent();

                intent.putExtra("watertemp",watertemp);
                intent.putExtra("watertime",watertime);
                intent.putExtra("o2time",o2time);
                intent.putExtra("smart",smart);

                setResult(RESULT_OK,intent);
                finish();
            }
        });

    }

    public void getDatasync(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    OkHttpClient client = new OkHttpClient();//创建OkHttpClient对象
                    Request request = new Request.Builder()
                            .url("http://192.168.43.191:8080/ssm-manager/getSettingAll")//请求接口。如果需要传参拼接到接口后面。
                            .build();//创建Request 对象
                    Response response = null;
                    response = client.newCall(request).execute();//得到Response 对象
                    if (response.isSuccessful()) {

                        Log.d("kwwl","response.code()=="+response.code());
                        Log.d("kwwl","response.message()=="+response.message());

                        //解析json
                        JSONObject jsonObject=new JSONObject(response.body().string());
                        JSONArray data=jsonObject.getJSONArray("data");
                        for (int i=0;i<data.length();i++){
                            JSONObject jsondata=new JSONObject(data.get(i).toString());

                            watertemp=jsondata.getInt("wendu");
                            watertime=jsondata.getInt("water");
                            smart=jsondata.getInt("smart");
                            o2time=jsondata.getInt("o2");

                            Message msg = Message.obtain();
                            msg.what=0;
                            UIHandler.sendMessage(msg);

                            Log.d("kwwl", "data: "+jsondata.getInt("id")+jsondata.getInt("smart")+
                                    jsondata.getInt("water")+jsondata.getInt("wendu")+jsondata.getInt("o2"));
                        }
                        //此时的代码执行在子线程，修改UI的操作请使用handler跳转到UI线程。
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void initView(){
        watertemp_Btn.setText(watertemp+"℃");
        watertime_Btn.setText(watertime/60000/60+"小时"+watertime/60000%60+"分钟");
        if (smart==1){
            smart_Btn.setText("已开启，关闭？");
        }else if (smart==0){
            smart_Btn.setText("已关闭，开启？");
        }
        o2time_Btn.setText(o2time/60000/60+"小时"+o2time/60000%60+"分钟");
    }
}
