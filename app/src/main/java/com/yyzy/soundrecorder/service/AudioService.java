package com.yyzy.soundrecorder.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import androidx.annotation.NonNull;

import com.yyzy.soundrecorder.entity.AudioEntity;
import com.yyzy.soundrecorder.util.ContantUtils;

import java.util.List;

public class AudioService extends Service implements MediaPlayer.OnCompletionListener {
    private MediaPlayer mediaPlayer = null;    //音频对象
    private List<AudioEntity> mList;           //播放列表
    private int playPosition = -1;             //记录当前播放位置

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
    }

    //设置当前进度为true
    private boolean flag = true;
    public void setFlagProgress(boolean flag) {
        this.flag = flag;
    }
    //更新播放进度的方法
    public void updateProgress(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (flag){
                    long aLong = mList.get(playPosition).getDurationLong();
                    int currentPosition = mediaPlayer.getCurrentPosition();
                    int progress = (int) (currentPosition*100/aLong);
                    mList.get(playPosition).setCurrentProgress(progress);
                    handler.sendEmptyMessageDelayed(1,1000);
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
            if (msg.what == 1){
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