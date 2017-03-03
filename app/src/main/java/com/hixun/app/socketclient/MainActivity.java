package com.hixun.app.socketclient;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
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
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;

import android.R.integer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;

public class MainActivity extends Activity implements OnClickListener{

    private SurfaceHolder holder;
    private Paint paint;
    final int HEIGHT=540;
    final int WIDTH=1280;
    final int X_OFFSET = 5;
    private int cx = X_OFFSET;

    public int rec_data = 10;

    //实际的Y轴的位置
    int centerY = HEIGHT /2;
    Timer timer = new Timer();
    TimerTask task = null;

    private EditText mIPEdt, mPortEdt,  mMessageEdt;
    private static TextView mConsoleTxt;

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

        holder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder,int format,int width,int height){
                drawBack(holder);
            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                // TODO Auto-generated method stub
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub
                timer.cancel();
            }
        });
    }

    private void initView() {
        mIPEdt = (EditText) findViewById(R.id.ip_edt);
        mPortEdt = (EditText) findViewById(R.id.port_edt);
        mMessageEdt = (EditText) findViewById(R.id.msg_edt);
        mConsoleTxt = (TextView) findViewById(R.id.receive_txt);
        findViewById(R.id.start_btn).setOnClickListener(this);
        findViewById(R.id.send_btn).setOnClickListener(this);
        findViewById(R.id.sin).setOnClickListener(this);


        final SurfaceView surface = (SurfaceView)findViewById(R.id.show);
        //初始化SurfaceHolder对象
        holder = surface.getHolder();
        //初始化画笔paint
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(3);

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
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.send_btn:
                send();
                drawBack(holder);
                cx = X_OFFSET;
                if(task != null){
                    task.cancel();
                }

                task = new TimerTask() {

                    @Override
                    public void run() {
//                        int cy = v.getId() == R.id.sin ? centerY -(int)(100 * Math.sin((cx -5) *2 * Math.PI/150)):
//                                centerY -(int)(100 * Math.cos((cx-5)*2*Math.PI/150));
                        int cy = v.getId() == R.id.sin ? centerY -(int)(100 * Math.sin((cx -5) *2 * Math.PI/150)):
                                centerY -(int)(100 * Math.cos((cx-5)*2*Math.PI/150));
//                        int cy = 20;
                        Canvas canvas = holder.lockCanvas(new Rect(cx,cy-2,cx+2,cy+2));
                        canvas.drawPoint(cx, cy, paint);
                        cx++;
                        if(cx >WIDTH){
                            task.cancel();
                            task = null;
                        }
                        holder.unlockCanvasAndPost(canvas);
                    }
                };
                timer.schedule(task, 0,30);
                break;
            case R.id.sin:
                drawBack(holder);
                cx = X_OFFSET;
                if(task != null){
                    task.cancel();
                }

                task = new TimerTask() {

                    @Override
                    public void run() {
                        int cy = v.getId() == R.id.sin ? centerY -(int)(100 * Math.sin((cx -5) *2 * Math.PI/150)):
                                centerY -(int)(100 * Math.cos((cx-5)*2*Math.PI/150));
//                        int cy = 20;
                        Canvas canvas = holder.lockCanvas(new Rect(cx,cy-2,cx+2,cy+2));
                        canvas.drawPoint(cx, cy, paint);
                        cx++;
                        if(cx >WIDTH){
                            task.cancel();
                            task = null;
                        }
                        holder.unlockCanvasAndPost(canvas);
                    }
                };
                timer.schedule(task, 0,30);
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

        private int rec_data;

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    try {
                       rec_data = Integer.parseInt(msg.obj.toString());
                        mConsoleStr.append(msg.obj + "  ");
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

    private void drawBack(SurfaceHolder holder){
        Canvas canvas = holder.lockCanvas();
        //绘制白色背景
        canvas.drawColor(Color.BLACK);
        Paint p = new Paint();
        p.setColor(Color.GREEN);
        p.setStrokeWidth(2);

        //绘制坐标轴
        canvas.drawLine(X_OFFSET, centerY, WIDTH, centerY, p); //绘制X轴
        canvas.drawLine(X_OFFSET, 10, X_OFFSET, HEIGHT, p);    //绘制Y轴
        holder.unlockCanvasAndPost(canvas);
        holder.lockCanvas(new Rect(0,0,0,0));
        holder.unlockCanvasAndPost(canvas);
    }

}