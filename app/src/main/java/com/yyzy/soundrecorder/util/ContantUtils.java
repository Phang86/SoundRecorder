package com.yyzy.soundrecorder.util;

import com.yyzy.soundrecorder.entity.AudioEntity;

import java.util.List;

public class ContantUtils {
    //存放文件目录
    public static String PATH_APP_DIR;
    public static String PATH_FETCH_DIR_RECORDER;

    private static List<AudioEntity> mAudioList;

    public static void setAudioList(List<AudioEntity> list) {
        if (list != null) {
            ContantUtils.mAudioList = list;
        }
    }

    public static List<AudioEntity> getmAudioList(){
        return mAudioList;
    }
}
