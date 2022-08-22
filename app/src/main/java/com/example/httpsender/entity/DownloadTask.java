package com.example.httpsender.entity;


import io.reactivex.rxjava3.disposables.Disposable;

/**
 * User: ljx
 * Date: 2019-06-08
 * Time: 10:09
 */
public class DownloadTask {

    private String url;
    private String localPath;

    private int progress;
    private long currentSize;
    private long totalSize;

    private int state; //0=未开始 1=等待中 2=下载中 3=暂停中 4=已完成  5=下载失败 6=已取消


    public DownloadTask(String url) {
        this.url = url;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DownloadTask task = (DownloadTask) o;

        return url.equals(task.url);
    }

    @Override
    public int hashCode() {
        return url.hashCode();
    }
}
