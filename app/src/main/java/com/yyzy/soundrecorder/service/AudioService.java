package com.yyzy.soundrecorder.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.yyzy.soundrecorder.R;
import com.yyzy.soundrecorder.entity.AudioEntity;
import com.yyzy.soundrecorder.util.ContantUtils;

import java.util.List;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer = null;    //音频对象
    private List<AudioEntity> mList;           //播放列表
    private int playPosition = -1;             //记录当前播放位置
    private RemoteViews remoteView;            //通知自定义的布局view对象
    private AudioReceiver receiver;
    private NotificationManager manager;
    private final int NOTIFY_ID_MUSIC = 101;   //发送通知的id

    private final String PRE_ACTION_PLAY = "com.yyzy.play";
    private final String PRE_ACTION_CLOSE = "com.yyzy.close";
    private final String PRE_ACTION_NEXT = "com.yyzy.next";
    private final String PRE_ACTION_LAST = "com.yyzy.last";
    private Notification notification = null;
    //private Notification notification;

    @Override
    public void onCreate() {
        super.onCreate();
        initRegisterReceiver();
        initRemoView();
        initNotifiaction();
    }

    //初始化通知栏
    private void initNotifiaction() {
        manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.mipmap.icon_app_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.icon_app_logo))
                .setContent(remoteView)
                .setAutoCancel(false)
                .setOngoing(true)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setPriority(Notification.PRIORITY_HIGH);
        notification = builder.build();
    }

    //更新通知栏信息的函数
    private void updateNotifyAction(int position) {
        if (mediaPlayer.isPlaying()) {
            remoteView.setImageViewResource(R.id.ny_iv_play, R.mipmap.red_pause);
        } else {
            remoteView.setImageViewResource(R.id.ny_iv_play, R.mipmap.red_play);
        }
        remoteView.setTextViewText(R.id.ny_tv_title, mList.get(position).getTitle());
        remoteView.setTextViewText(R.id.ny_content, mList.get(position).getDuration());
        manager.notify(NOTIFY_ID_MUSIC, notification);
    }

    //创建广播接收者
    class AudioReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            notifyUIControl(action);
        }
    }

    private void notifyUIControl(String action) {
        switch (action) {
            case PRE_ACTION_PLAY:
                startOrStopMusic();
                break;
            case PRE_ACTION_CLOSE:
                closeNotification();
                break;
            case PRE_ACTION_NEXT:
                nextMusic();
                break;
            case PRE_ACTION_LAST:
                previousMusic();
                break;
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        closeMusic();
    }

    //停止音乐
    public void closeMusic() {
        if (mediaPlayer != null) {
            setFlagProgress(false);
            closeNotification();
            mediaPlayer.stop();
            playPosition = -1;
        }
    }

    //关闭通知
    private void closeNotification() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mList.get(playPosition).setPlay(false);
        }
        notifyActivityRefreshUI();
        manager.cancel(NOTIFY_ID_MUSIC);
    }

    //播放下一曲
    private void nextMusic() {
        mList.get(playPosition).setPlay(false);
        if (playPosition >= mList.size() - 1) {
            playPosition = 0;
        } else {
            playPosition++;
        }
        mList.get(playPosition).setPlay(true);
        play(playPosition);
    }

    //播放上一曲
    private void previousMusic() {
        mList.get(playPosition).setPlay(false);
        if (playPosition == 0) {
            playPosition = mList.size() - 1;
        } else {
            playPosition--;
        }
        mList.get(playPosition).setPlay(true);
        play(playPosition);
    }

    //注册广播接收者，用于接受用户点击通知栏按钮发出的信息
    private void initRegisterReceiver() {
        receiver = new AudioReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PRE_ACTION_LAST);
        filter.addAction(PRE_ACTION_NEXT);
        filter.addAction(PRE_ACTION_PLAY);
        filter.addAction(PRE_ACTION_CLOSE);
        registerReceiver(receiver, filter);
        Log.e("TAG", "initRegisterReceiver: " + "到了");
    }

    //设置通知栏的显示效果以及图片的点击事件
    private void initRemoView() {
        remoteView = new RemoteViews(getPackageName(), R.layout.notify_audio);

        PendingIntent lastPI = PendingIntent
                .getBroadcast(this, 1, new Intent(PRE_ACTION_LAST), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_last, lastPI);

        PendingIntent nextPI = PendingIntent
                .getBroadcast(this, 1, new Intent(PRE_ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_next, nextPI);

        PendingIntent playPI = PendingIntent
                .getBroadcast(this, 1, new Intent(PRE_ACTION_PLAY), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_play, playPI);

        PendingIntent closePI = PendingIntent
                .getBroadcast(this, 1, new Intent(PRE_ACTION_CLOSE), PendingIntent.FLAG_UPDATE_CURRENT);
        remoteView.setOnClickPendingIntent(R.id.ny_iv_close, closePI);
    }

    public interface OnPlayChangeListener {
        public void playChange(int changePosition);
    }

    private OnPlayChangeListener onPlayChangeListener;

    public void setOnPlayChangeListener(OnPlayChangeListener onPlayChangeListener) {
        this.onPlayChangeListener = onPlayChangeListener;
    }

    //多媒体服务发生变化，通知活动刷新UI
    public void notifyActivityRefreshUI() {
        if (onPlayChangeListener != null) {
            onPlayChangeListener.playChange(playPosition);
        }
    }

    public AudioService() {
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //当前音乐播放完成直接
        nextMusic();
    }

    public class AudioBinder extends Binder {
        public AudioService getService() {
            return AudioService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new AudioBinder();
    }

    //播放音乐，点击播放
    public void play(int position) {
        if (mediaPlayer == null) {
            mediaPlayer = new MediaPlayer();
            //设置监听
            mediaPlayer.setOnCompletionListener(this);
        }
        //播放时，获取当前播放列表，判断是否有音乐
        mList = ContantUtils.getmAudioList();
        if (mList.size() <= 0) {
            return;
        }
        //如果正在播放音乐，则立即停止
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        try {
            //在切换歌曲之前，释放空间
            mediaPlayer.reset();
            //记录当前播放位置
            playPosition = position;
            //设置音频的播放路径
            mediaPlayer.setDataSource(mList.get(position).getPath());
            //进行同步播放准备
            mediaPlayer.prepare();
            mediaPlayer.start();
            //设置当前正在播放
            mList.get(position).setPlay(true);
            notifyActivityRefreshUI();
            setFlagProgress(true);
            updateProgress();
            updateNotifyAction(position);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //设置暂停还是播放音乐
    public void startOrStopMusic() {
        int playPosition = this.playPosition;
        AudioEntity audioEntity = mList.get(playPosition);
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            audioEntity.setPlay(false);
        } else {
            mediaPlayer.start();
            audioEntity.setPlay(true);
        }
        notifyActivityRefreshUI();
        updateNotifyAction(playPosition);
    }

    //设置当前进度为true
    private boolean flag = true;

    public void setFlagProgress(boolean flag) {
        this.flag = flag;
    }

    //更新播放进度的方法
    public void updateProgress() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag) {
                    long aLong = mList.get(playPosition).getDurationLong();
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int progress = (int) (currentPosition * 100 / aLong);
                    mList.get(playPosition).setCurrentProgress(progress);
                    handler.sendEmptyMessageDelayed(1, 1000);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == 1) {
                notifyActivityRefreshUI();
            }
            return true;
        }
    });

    /**
     * 播放按钮有两种可能
     * 1.不是当前播放的位置被点击了，就进行切换歌曲
     * 2.是当前播放位置被点击了，进行播放或暂停
     **/
    public void cutMusicOrpPause(int position) {
        int playPosition = this.playPosition;
        if (position != playPosition) {
            //判断是否播放，如果切歌，把上一曲改为false
            if (playPosition != -1) {
                mList.get(playPosition).setPlay(false);
            }
            play(position);
            return;
        }
        //播放位置被点击了，执行暂停还是播放操作
        startOrStopMusic();
    }
}