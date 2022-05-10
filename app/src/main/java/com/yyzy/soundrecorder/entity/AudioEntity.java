package com.yyzy.soundrecorder.entity;

public class AudioEntity {
    private String id;
    private String title;       //文件名称
    private String time;        //文件存放时间
    private String duration;    //文件持续时间
    private String path;        //文件存放路径
    private long durationLong;  //文件毫秒数
    private long lastModefied;  //文件修改后的时间
    private String fileSuffix;  //文件后缀名
    private long fileLength;    //文件字节数
    private boolean isPlay = false;         //播放状态
    private int currentProgress = 0;

    public void setCurrentProgress(int currentProgress) {
        this.currentProgress = currentProgress;
    }

    public int getCurrentProgress() {
        return currentProgress;
    }

    public void setPlay(boolean play) {
        isPlay = play;
    }

    public boolean isPlay() {
        return isPlay;
    }

    public AudioEntity() {
    }

    public AudioEntity(String id, String title, String time, String duration, String path, long durationLong, long lastModefied, String fileSuffix, long fileLength) {
        this.id = id;
        this.title = title;
        this.time = time;
        this.duration = duration;
        this.path = path;
        this.durationLong = durationLong;
        this.lastModefied = lastModefied;
        this.fileSuffix = fileSuffix;
        this.fileLength = fileLength;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDurationLong() {
        return durationLong;
    }

    public void setDurationLong(long durationLong) {
        this.durationLong = durationLong;
    }

    public long getLastModefied() {
        return lastModefied;
    }

    public void setLastModefied(long lastModefied) {
        this.lastModefied = lastModefied;
    }

    public String getFileSuffix() {
        return fileSuffix;
    }

    public void setFileSuffix(String fileSuffix) {
        this.fileSuffix = fileSuffix;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }
}
