package com.yyzy.soundrecorder.util;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.print.PrintAttributes;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyboardShortcutGroup;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.ContentView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.yyzy.soundrecorder.R;
import com.yyzy.soundrecorder.databinding.DialogRenameBinding;

import java.util.List;

public class RenameDialog extends Dialog implements View.OnClickListener {
    private DialogRenameBinding binding;

    public interface OnConfirmListener {
        //把msg填充到文本框
        void onConfirm(String msg);
    }

    private OnConfirmListener onConfirmListener;

    public void setOnConfirmListener(OnConfirmListener onConfirmListener) {
        this.onConfirmListener = onConfirmListener;
    }

    public RenameDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DialogRenameBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnRenameConfirm.setOnClickListener(this);
        binding.btnRenameCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_rename_cancel:
                cancel();
                break;
            case R.id.btn_rename_confirm:
                if (onConfirmListener != null) {
                    String msg = binding.edRename.getText().toString().trim();
                    onConfirmListener.onConfirm(msg);
                }
                cancel();
                break;
        }
    }

    //显示文件原名称到文本框
    public void setTipText(String oldName) {
        binding.edRename.setText(oldName);
    }

    //设置对话框宽度与屏幕宽度一致
    public void setDialogWidth() {
        //获取当前窗口对象
        Window window = getWindow();
        //获取窗口的信息参数
        WindowManager.LayoutParams wlp = window.getAttributes();
        //获取屏幕的宽度
        Display display = window.getWindowManager().getDefaultDisplay();
        //设置窗口宽度占屏幕90%
        wlp.width = (int)(display.getWidth()*0.95);
        //设置窗口底部边距
        wlp.verticalMargin = 0.04f;
        //设置窗口从底部弹出
        wlp.gravity = Gravity.BOTTOM;
        //设置窗口背景为透明
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setAttributes(wlp);
        //自动弹出软键盘
        handler.sendEmptyMessageDelayed(1,0);
    }

    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.toggleSoftInput(0,InputMethodManager.HIDE_NOT_ALWAYS);
            return false;
        }
    });
}
