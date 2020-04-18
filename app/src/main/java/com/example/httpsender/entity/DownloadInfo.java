package com.example.httpsender.entity;


import io.reactivex.rxjava3.disposables.Disposable;

/**
 * User: ljx
 * Date: 2019-06-08
 * Time: 10:09
 */
public class DownloadInfo {

    private int taskId;

    private String url;
    private int progress;

    private long currentSize;
    private long totalSize;

    private Disposable mDisposable;


    private int state; //0=未开始 1=等待中 2=下载中 3=暂停中 4=已完成  5=下载失败 6=已取消

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public Disposable getDisposable() {
        return mDisposable;
    }

    public void setDisposable(Disposable disposable) {
        mDisposable = disposable;
    }

    public boolean isDownloading() {
        return mDisposable != null && !mDisposable.isDisposed();
    }

    public DownloadInfo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public long getCurrentSize() {
        return currentSize;
    }

    public void setCurrentSize(long currentSize) {
        this.currentSize = currentSize;
    }

    public long getTotalSize() {
        return totalSize;
    }

    public void setTotalSize(long totalSize) {
        this.totalSize = totalSize;
    }
}
