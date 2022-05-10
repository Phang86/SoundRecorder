package com.yyzy.soundrecorder.util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils {
    private static PermissionUtils permissions;
    private final int mRequestCode = 100;

    public interface OnPermissionCallbackListener {
        void onGranted();

        void onDenied(List<String> denidePermission);
    }

    private OnPermissionCallbackListener mListener;

    public PermissionUtils() {

    }

    public static PermissionUtils getInstance() {
        if (permissions == null) {
            synchronized (PermissionUtils.class) {
                if (permissions == null) {
                    permissions = new PermissionUtils();
                }
            }
        }
        return permissions;
    }

    public void onRequestPermission(Activity context, String[] permission, OnPermissionCallbackListener listener) {
        mListener = listener;
        if (Build.VERSION.SDK_INT >= 23) {
            List<String> mList = new ArrayList<>();
            for (int i = 0; i < permission.length; i++) {
                int res = ContextCompat.checkSelfPermission(context, permission[i]);
                if (res != PackageManager.PERMISSION_GRANTED) {
                    mList.add(permission[i]);
                }
            }
            if (mList.size() > 0) {
                String[] permission_arr = mList.toArray(new String[mList.size()]);
                ActivityCompat.requestPermissions(context, permission_arr, mRequestCode);
            }else {
                mListener. onGranted();
            }
        }
    }

    public void onRequestPermissionResult(Activity context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == mRequestCode) {
            List<String> deniedPermission = new ArrayList<>();
            if (grantResults.length > 0) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                        deniedPermission.add(permissions[i]);
                    }
                }
            }
            if (deniedPermission.size() == 0) {
                mListener.onGranted();
            } else {
                mListener.onDenied(deniedPermission);
            }
        }else {
            mListener.onGranted();
        }
    }

    public void showDialogTipUserGoToSetting(Activity context){
        DialogUtils.showDialog(context, "提示信息", "已禁用权限，请手动开启权限！", "取消", new DialogUtils.OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                context.finish();
            }
        }, "确定", new DialogUtils.OnRightClickListener() {
            @Override
            public void onRightClick() {
                StartSystemPageUtils.goToSettings(context);
                context.finish();
            }
        });

    }
}
