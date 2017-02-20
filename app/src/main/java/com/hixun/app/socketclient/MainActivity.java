package com.hixun.app.socketclient;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

//import com.tencent.stat.MtaSDkException;
//import com.tencent.stat.StatConfig;
//import com.tencent.stat.StatService;

import android.R.integer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

public class MainActivity extends Activity implements OnClickListener{
    private EditText mIPEdt, mPortEdt,  mMessageEdt;
    private static TextView mConsoleTxt,mTest;

    private static StringBuffer mConsoleStr = new StringBuffer();
    private Socket mSocket;
    private boolean isStartRecieveMsg;

    private SocketHandler mHandler;
    protected BufferedReader mReader;//BufferedWriter 用于推送消息
    protected BufferedWriter mWriter;//BufferedReader 用于接收消息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mIPEdt = (EditText) findViewById(R.id.ip_edt);
        mPortEdt = (EditText) findViewById(R.id.port_edt);
        mMessageEdt = (EditText) findViewById(R.id.msg_edt);
        mConsoleTxt = (TextView) findViewById(R.id.receive_txt);
        mTest = (TextView)findViewById(R.id.test);
        findViewById(R.id.start_btn).setOnClickListener(this);
        findViewById(R.id.send_btn).setOnClickListener(this);
        findViewById(R.id.clear_btn).setOnClickListener(this);
        mHandler = new SocketHandler();
    }

    /**
     * 初始化socket
     */
    private void initSocket() {
        //新建一个线程，用于初始化socket和检测是否有接收到新的消息
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                String ip = mIPEdt.getText().toString();//得到界面输入的IP地址
                int port = Integer.parseInt(mPortEdt.getText().toString());//得到界面输入的Socket端口号

                try {
                    isStartRecieveMsg = true;
                    mSocket = new Socket(ip, port);
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "utf-8"));
                    mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "utf-8"));
                    while(isStartRecieveMsg) {
                        if(mReader.ready()) {
                            /*读取一行字符串，读取的内容来自于客户机
                            reader.readLine()方法是一个阻塞方法，
                            从调用这个方法开始，该线程会一直处于阻塞状态，
                            直到接收到新的消息，代码才会往下走*/
                            String data = mReader.readLine();
                            //handler发送消息，在handleMessage()方法中接收
                            mHandler.obtainMessage(0, data).sendToTarget();
                        }
                        Thread.sleep(200);
                    }
                    mWriter.close();
                    mReader.close();
                    mSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                send();
                break;
            case R.id.clear_btn:
                mConsoleStr.delete(0, mConsoleStr.length());
                mConsoleTxt.setText(mConsoleStr.toString());
                break;
            case R.id.start_btn:
                if(!isStartRecieveMsg) {
                    initSocket();
                }
                break;
            default:
                break;
        }
    }

    /**
     * 发送
     */
    private void send() {
        new AsyncTask<String, Integer, String>() {

            @Override
            protected String doInBackground(String... params) {
                sendMsg();
                return null;
            }
        }.execute();
    }
    /**
     * 发送消息
     */
    protected void sendMsg() {
        try {
            String msg = mMessageEdt.getText().toString().trim();
            mWriter.write(msg +"\n");
            mWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static class SocketHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    try {
                        mConsoleStr.append(msg.obj);
                        mConsoleTxt.setText(mConsoleStr);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    break;

                default:
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        isStartRecieveMsg = false;
    }

}

//public class MainActivity extends AppCompatActivity implements Runnable{
//
//    private EditText mIPEdt, mPortEdt, mMessageEdt;  //编辑框，分别为IP，端口号，和发送数据框
//    private static TextView mReceiveTxt, mTest;  //文本框，接收的信息内容和test用
//    private Button send_btn;
//
//    private Socket mSocket = null;
//    protected BufferedReader mReader;//BufferedWriter 用于接收消息
//    protected PrintWriter mWriter;//BufferedReader 用于发送消息
//
//    private String once_Msg = ""; //每次接收的数据
//    private StringBuilder receive_show = null;  //接收的总的数据
//
//    private void initView() {
//        receive_show = new StringBuilder();
//        mIPEdt = (EditText) findViewById(R.id.ip_edt);
//        mPortEdt = (EditText) findViewById(R.id.port_edt);
//        mMessageEdt = (EditText) findViewById(R.id.msg_edt);
//        mReceiveTxt = (TextView) findViewById(R.id.receive_txt);
//        mTest = (TextView) findViewById(R.id.test);
//
//        send_btn = (Button)findViewById(R.id.send_btn);
//
////        findViewById(R.id.start_btn).setOnClickListener(this);
////        findViewById(R.id.send_btn).setOnClickListener(this);
////        findViewById(R.id.clear_btn).setOnClickListener(this);
//    }
//
//    //定义一个handler对象,用来刷新界面
//    public Handler handler = new Handler() {
//        public void handleMessage(Message msg) {
//            if (msg.what == 0x123) {
//                receive_show.append(once_Msg);
//                mReceiveTxt.setText(receive_show.toString());
//            }
//        };
//    };
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//        initView();
////        Button send_btn = (Button) findViewById(R.id.send_btn);
////        send_btn.setOnClickListener(this);
//        //当程序一开始运行的时候就实例化Socket对象,与服务端进行连接,获取输入输出流
//        //因为4.0以后不能再主线程中进行网络操作,所以需要另外开辟一个线程
//        new Thread() {
//
//            public void run() {
//                try {
//                    String ip = mIPEdt.getText().toString();//得到界面输入的IP地址
//                    int port = Integer.parseInt(mPortEdt.getText().toString());//得到界面输入的Socket端口号
//                    mSocket = new Socket(ip, port);
//                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "UTF-8"));
//                    mWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
//                            mSocket.getOutputStream())), true);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }.start();
//        //为发送按钮设置点击事件
//        send_btn.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//        String msg = mMessageEdt.getText().toString();
//        if (mSocket.isConnected()) {
//            if (!mSocket.isOutputShutdown()) {
//                mWriter.println(msg);
//            }
//            try {
//                Thread.sleep(50);
//                receive_show.append("Send Message");
//                mReceiveTxt.setText(receive_show.toString());
//
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//
//        }
//            }
//        });
//        new Thread( MainActivity.this).start();
//    }
//
//    //重写run方法,在该方法中输入流的读取
//    @Override
//    public void run() {
//        try {
//            while (true) {
//                    if (mSocket.isConnected()) {
//
//                        receive_show.append("mSocket.isConnected");
//                        mReceiveTxt.setText(receive_show.toString());
//
//                        if (!mSocket.isInputShutdown()) {
//
//                            receive_show.append("mSocket isInputShutdown");
//                            mReceiveTxt.setText(receive_show.toString());
//
//                            if ((once_Msg = mReader.readLine()) != null) {
//                                once_Msg += "\n";
//                                handler.sendEmptyMessage(0x123);
//                            }
//                        }
//                    }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
////    @Override
////    public void onClick(View v) {
////        new Thread() {
////            @Override
////            public void run() {
////                try {
////                    acceptServer();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
////            }
////        }.start();
////    }
//
//
//
//    //    @Override
////    public void onClick(View v) {
////        switch (v.getId()) {
////            case R.id.send_btn:
////                send();
////                break;
////            case R.id.clear_btn:
////                receive_show.delete(0, receive_show.length());
////                mMessageEdt.setText(receive_show.toString());
////                break;
//////            case R.id.start_btn:
//////                if(!isStartRecieveMsg) {
//////                    initSocket();
//////                }
//////                break;
////            default:
////                break;
////        }
////    }
////
////    private void send() {
////        String msg = mMessageEdt.getText().toString();
////        if (mSocket.isConnected()) {
////            if (!mSocket.isOutputShutdown()) {
////                mWriter.println(msg);
////            }
////        }
////    }
//
//
//
//}
