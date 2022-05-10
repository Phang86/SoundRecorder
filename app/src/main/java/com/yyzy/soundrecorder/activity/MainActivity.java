package com.yyzy.soundrecorder.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.yyzy.soundrecorder.R;
import com.yyzy.soundrecorder.databinding.ActivityMainBinding;
import com.yyzy.soundrecorder.util.ContantUtils;
import com.yyzy.soundrecorder.util.IFileInter;
import com.yyzy.soundrecorder.util.PermissionUtils;
import com.yyzy.soundrecorder.util.SDCardUtils;

import java.io.File;
import java.util.List;
import java.util.logging.LogRecord;

public class MainActivity extends AppCompatActivity {

    private int num = 4;
    private ActivityMainBinding mainBinding;
    private String time = "倒计时";
    String[] permissions = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE};

    final PermissionUtils.OnPermissionCallbackListener mListener = new PermissionUtils.OnPermissionCallbackListener() {
        @Override
        public void onGranted() {
            //判断是否创建文件夹
            createAppDir();
            //倒计时进入应用
            handler.sendEmptyMessageDelayed(1, 1000);
        }

        @Override
        public void onDenied(List<String> denidePermission) {
            PermissionUtils.getInstance()
                    .showDialogTipUserGoToSetting(MainActivity.this);
        }
    };

    private void createAppDir(){
        File recorderDir = SDCardUtils.getInstance().createAppFetchDir(IFileInter.FETCH_DIR_AUDIO);
        ContantUtils.PATH_FETCH_DIR_RECORDER = recorderDir.getAbsolutePath();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());
        mainBinding.mainTv.setText(time+num+"s");
        PermissionUtils.getInstance().onRequestPermission(this, permissions, mListener);
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            if (message.what == 1) {
                num--;
                if (num == 0) {
                    startActivity(new Intent(MainActivity.this, AudioListActivity.class));
                    finish();
                }else {
                    mainBinding.mainTv.setText(time+num+"s");
                    handler.sendEmptyMessageDelayed(1, 1000);
                }
            }
            return false;
        }
    });


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.getInstance().onRequestPermissionResult(this, requestCode, permissions, grantResults);
    }
}