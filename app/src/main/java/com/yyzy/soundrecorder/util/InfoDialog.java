package com.yyzy.soundrecorder.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;

import com.yyzy.soundrecorder.R;
import com.yyzy.soundrecorder.databinding.DialogInfoBinding;
import com.yyzy.soundrecorder.entity.AudioEntity;

import java.text.DecimalFormat;

public class InfoDialog extends Dialog{

    private DialogInfoBinding binding;

    public InfoDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogInfoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initView();
    }

    private void initView() {
        binding.tvConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });
    }

    public void setInfo(AudioEntity entity) {
        binding.tvName.setText(entity.getTitle());
        binding.tvPath.setText(entity.getPath());
        binding.tvTime.setText(entity.getTime());
        String size = calFileSize(entity.getFileLength());
        binding.tvSize.setText(size);
    }

    private String calFileSize(long fileLength) {
        DecimalFormat df = new DecimalFormat("#.00");
        if (fileLength >= 1024 * 1024) {
            return df.format(fileLength * 1.0 / (1024 * 1024))+"MB";
        }else if (fileLength >= 1024) {
            return df.format(fileLength * 1.0 / (1024))+"KB";
        }else if (fileLength < 1024) {
            return df.format(fileLength)+"B";
        }
        return "0KB";
    }


    //设置对话框宽度与屏幕宽度一致
    public void setInfoDialogWidth() {
        //获取当前窗口对象
        Window window = getWindow();
        //获取窗口的信息参数
        WindowManager.LayoutParams wlp = window.getAttributes();
        //获取屏幕的宽度
        Display display = window.getWindowManager().getDefaultDisplay();
        //设置窗口宽度占屏幕95%
        wlp.width = (int) (display.getWidth() * 0.95);
        //设置窗口底部边距
        wlp.verticalMargin = 0.04f;
        //设置窗口从底部弹出
        wlp.gravity = Gravity.BOTTOM;
        //设置窗口背景为透明
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setAttributes(wlp);
        //自动弹出软键盘
        //handler.sendEmptyMessageDelayed(1,0);
    }
}
