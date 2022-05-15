package com.yyzy.soundrecorder.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;

import com.yyzy.soundrecorder.R;
import com.yyzy.soundrecorder.activity.RecorderActivity;
import com.yyzy.soundrecorder.util.ContantUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecorderService extends Service {
    private MediaRecorder recorder;        //创建录音对象
    private boolean isAlive = false;
    private String recorderPath;           //录音文件存放的公共目录
    private SimpleDateFormat sdf, carSdf;
    private int time;
    private RemoteViews remoteView;
    private NotificationManager manager;
    private Notification notification;
    private int NOTIFY_ID_RECORDER = 102;

    public RecorderService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        carSdf = new SimpleDateFormat("HH:mm:ss");
        recorderPath = ContantUtils.PATH_FETCH_DIR_RECORDER;
        initRemoteView();
        initNotification();
    }

    //初始化通知栏对象
    private void initNotification() {
        manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.icon_voice)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),R.mipmap.icon_voice))
                .setContent(remoteView)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(Notification.PRIORITY_HIGH);
        notification = builder.build();
    }

    //更新发送通知的函数
    private void updateNotification(String calTime){
        remoteView.setTextViewText(R.id.ny_time,calTime);
        manager.notify(NOTIFY_ID_RECORDER, notification);
    }

    //关闭通知
    private void closeNotification(){
        manager.cancel(NOTIFY_ID_RECORDER);
    }

    //初始化通知栏的远程view
    private void initRemoteView() {
        remoteView = new RemoteViews(getPackageName(), R.layout.notify_recorder);
        Intent intent = new Intent(this, RecorderActivity.class);
        PendingIntent pi = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_layout,pi);
    }

    public interface OnRefreshUIListener {
        void onRefresh(int db, String time);
    }

    private OnRefreshUIListener onRefreshUIListener;

    public void setOnRefreshUIListener(OnRefreshUIListener onRefreshUIListener) {
        this.onRefreshUIListener = onRefreshUIListener;
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (recorder == null) return false;
            double ratio = recorder.getMaxAmplitude() / 100;
            //分贝
            double db = 0;
            if (ratio > 1) {
                db = Math.log10(ratio) * 20;
            }
            time += 1000;
            if (onRefreshUIListener != null) {
                String timeStr = calTime(time);
                onRefreshUIListener.onRefresh((int) db,timeStr);
                updateNotification(timeStr);
            }
            return false;
        }
    });

    //计算时间
    private String calTime(int second) {
        second -= 8 * 60 * 60 *1000;
        String format = carSdf.format(new Date(second));
        return format;
    }

    //开启一个子线程
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            while (isAlive){
                handler.sendEmptyMessage(0);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    });

    //开启录音机
    public void startRecorder() {
        if (recorder == null) {
            recorder = new MediaRecorder();
        }
        isAlive = true;
        recorder.reset();
        try {
            //设置录音对象的参数
            setRecorder();
            recorder.prepare();
            recorder.start();
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //停止录音
    public void stopRecorder() {
        if (recorder != null) {
            recorder.stop();
            recorder = null;
            time = 0;
            closeNotification();
            isAlive = false;
        }
    }

    //设置录音对象的参数
    private void setRecorder() {
        //设置获取麦克风的声音
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        //设置输出格式
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_WB);
        //设置编码格式
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_WB);
        //设置输出文件
        String time = sdf.format(new Date());
        File file = new File(recorderPath, time + ".amr");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        recorder.setOutputFile(file.getAbsolutePath());
        //设置录音时间为10分钟
        recorder.setMaxDuration(10 * 60 * 1000);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new RecorderBinder();
    }

    public class RecorderBinder extends Binder {
        public RecorderService getService() {
            return RecorderService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopRecorder();
    }
}