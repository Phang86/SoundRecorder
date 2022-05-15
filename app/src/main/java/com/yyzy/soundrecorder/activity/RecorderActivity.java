package com.yyzy.soundrecorder.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.View;

import com.yyzy.soundrecorder.R;
import com.yyzy.soundrecorder.databinding.ActivityRecorderBinding;
import com.yyzy.soundrecorder.service.RecorderService;
import com.yyzy.soundrecorder.util.StartSystemPageUtils;

public class RecorderActivity extends AppCompatActivity {

    private ActivityRecorderBinding binding;
    private RecorderService recorderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRecorderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = new Intent(this, RecorderService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    private RecorderService.OnRefreshUIListener refreshUIListener = new RecorderService.OnRefreshUIListener() {
        @Override
        public void onRefresh(int db, String time) {
            binding.voicLine.setVolume(db);
            binding.tvTime.setText(time);
        }
    };

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RecorderService.RecorderBinder binder = (RecorderService.RecorderBinder) service;
            recorderService = binder.getService();
            recorderService.startRecorder();
            recorderService.setOnRefreshUIListener(refreshUIListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.iv_back:
                StartSystemPageUtils.goToHomePage(this);
                break;
            case R.id.iv_stop:
                recorderService.stopRecorder();
                startActivity(new Intent(this,AudioListActivity.class));
                finish();
                break;
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解除绑定服务
        if (connection != null) {
            unbindService(connection);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            StartSystemPageUtils.goToHomePage(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}