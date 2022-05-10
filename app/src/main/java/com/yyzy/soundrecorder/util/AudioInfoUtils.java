package com.yyzy.soundrecorder.util;

import android.media.MediaMetadataRetriever;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

public class AudioInfoUtils {
    private static AudioInfoUtils audioInfoUtils;

    private MediaMetadataRetriever mediaMetadataRetriever;

    public AudioInfoUtils() {
    }

    public static AudioInfoUtils getInstance() {
        if (audioInfoUtils == null) {
            synchronized (AudioInfoUtils.class) {
                if (audioInfoUtils == null) {
                    audioInfoUtils = new AudioInfoUtils();
                }
            }
        }
        return audioInfoUtils;
    }

    public long getAudioFileDuration(String filePath) {
        long duration = 0;
        if (mediaMetadataRetriever == null) {
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }
        mediaMetadataRetriever.setDataSource(filePath);
        String s = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        duration = Long.parseLong(s);
        return duration;
    }

    public String getAudioFileFormatDuration(String format, long duration) {
        duration -= 8 * 3600 * 1000;
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(duration));
    }

    //转换为固定的时长类型 HH:mm:ss
    public String getAudioFileFormatDuration(long duration) {
        return getAudioFileFormatDuration("HH:mm:ss", duration);
    }

    //获取多媒体文件的艺术家
    public String getAudioFileArtist(String filePath) {
        if (mediaMetadataRetriever == null) {
            mediaMetadataRetriever = new MediaMetadataRetriever();
        }
        mediaMetadataRetriever.setDataSource(filePath);
        String artist = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
        return artist;
    }

    //释放空间
    public void releseRetriever() {
        if (mediaMetadataRetriever != null) {
            mediaMetadataRetriever.release();
            mediaMetadataRetriever = null;
        }
    }
}
