package com.yyzy.soundrecorder.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;

public class DialogUtils {
    public interface OnLeftClickListener {
        void onLeftClick();
    }

    public interface OnRightClickListener {
        void onRightClick();
    }

    public static void showDialog(Context context, String title, String msg
            , String leftBtn, OnLeftClickListener leftListener
            , String rightBtn, OnRightClickListener rightListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(msg);
        builder.setNegativeButton(leftBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (leftListener != null) {
                    leftListener.onLeftClick();
                    dialogInterface.cancel();
                }
            }
        });
        builder.setPositiveButton(rightBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (rightListener != null) {
                    rightListener.onRightClick();
                    dialogInterface.cancel();
                }
            }
        });
        builder.create().show();
    }

    public void showDialogTipUserGoToSetting(Activity context){
        DialogUtils.showDialog(context, "提示信息", "已禁用权限，请手动开启权限！", "取消", new OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                context.finish();
            }
        }, "确定", new OnRightClickListener() {
            @Override
            public void onRightClick() {
                StartSystemPageUtils.goToSettings(context);
                context.finish();
            }
        });

    }
}
