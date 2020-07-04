package com.example.httpsender;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.example.httpsender.DownloadMultiAdapter.OnItemClickListener;
import com.example.httpsender.entity.DownloadInfo;
import com.rxjava.rxlife.RxLife;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.Disposable;
import rxhttp.wrapper.param.RxHttp;

/**
 * 多任务下载
 * User: ljx
 * Date: 2019-06-07
 * Time: 11:02
 */
public class DownloadMultiActivity extends ToolBarActivity implements OnItemClickListener<DownloadInfo> {

    public static final int MAX_TASK_COUNT = 3;  //最大并发数

    private DownloadMultiAdapter mAdapter;

    private String[] downloadUrl = {
        "http://update.9158.com/miaolive/Miaolive.apk",//喵播
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk",//探探
        "https://o8g2z2sa4.qnssl.com/android/momo_8.18.5_c1.apk",//陌陌
        "http://s9.pstatp.com/package/apk/aweme/app_aweGW_v6.6.0_2905d5c.apk"//抖音
    };

    private List<DownloadInfo> waitTask = new ArrayList<>(); //等待下载的任务
    private List<DownloadInfo> downloadingTask = new ArrayList<>(); //等待下载的任务

    private List<DownloadInfo> downloadInfos = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_multi_activity);

        for (int i = 0; i < 20; i++) {
            DownloadInfo downloadInfo = new DownloadInfo(downloadUrl[i % downloadUrl.length]);
            downloadInfo.setTaskId(i);
            downloadInfos.add(downloadInfo);
        }
        mAdapter = new DownloadMultiAdapter(downloadInfos);
        mAdapter.setOnItemClickListener(this);
        mAdapter.setHasStableIds(true);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setAdapter(mAdapter);
    }

    public static long lastChangedTime;

    //500毫秒刷新一次列表
    private void notifyDataSetChanged(boolean force) {
        long time = System.currentTimeMillis();
        if (time - lastChangedTime > 500 || force) {
            mAdapter.notifyDataSetChanged();
            lastChangedTime = time;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.download, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.download_all) {
            if ("全部下载".contentEquals(item.getTitle())) {
                for (DownloadInfo info : downloadInfos) {
                    download(info);
                }
                item.setTitle("全部取消");
            } else if ("全部取消".contentEquals(item.getTitle())) {
                Iterator<DownloadInfo> iterator = waitTask.iterator();
                while (iterator.hasNext()) {
                    DownloadInfo next = iterator.next();
                    next.setState(6);
                    iterator.remove();
                }

                iterator = downloadingTask.iterator();
                while (iterator.hasNext()) {
                    DownloadInfo next = iterator.next();
                    iterator.remove();
                    Disposable disposable = next.getDisposable();
                    if (disposable != null && !disposable.isDisposed()) {
                        disposable.dispose();
                    }
                    next.setState(6);
                }
                item.setTitle("全部下载");
                notifyDataSetChanged(true);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, DownloadInfo data, int position) {
        switch (view.getId()) {
            case R.id.bt_pause:
                int state = data.getState();
                if (state == 0) {
                    download(data);
                } else if (state == 1) {
                    waitTask.remove(data);
                    data.setState(6);
                } else if (state == 2) {
                    Disposable disposable = data.getDisposable();
                    if (disposable != null && !disposable.isDisposed()) {
                        disposable.dispose();
                        data.setState(3);
                        notifyDataSetChanged(true);
                    }
                } else if (state == 3) {
                    download(data);
                } else if (state == 4) {
                    Tip.show("该任务已完成");
                } else if (state == 5) {
                    Tip.show("该任务下载失败");
                } else if (state == 6) {
                    download(data);
                }
                break;
        }
    }

    private void download(DownloadInfo data) {
        if (downloadingTask.size() >= MAX_TASK_COUNT) {
            data.setState(1);
            waitTask.add(data);
            return;
        }
        String destPath = getExternalCacheDir() + "/" + data.getTaskId() + ".apk";
        long length = new File(destPath).length();
        Disposable disposable = RxHttp.get(data.getUrl())
            .setRangeHeader(length, -1, true)  //设置开始下载位置，结束位置默认为文件末尾
            .asDownload(destPath, AndroidSchedulers.mainThread(), progress -> { //如果需要衔接上次的下载进度，则需要传入上次已下载的字节数length
                //下载进度回调,0-100，仅在进度有更新时才会回调
                data.setProgress(progress.getProgress());//当前进度 0-100
                data.setCurrentSize(progress.getCurrentSize());//当前已下载的字节大小
                data.setTotalSize(progress.getTotalSize()); //要下载的总字节大小
                notifyDataSetChanged(false);
            })
            .doFinally(() -> {//不管任务成功还是失败，如果还有在等待的任务，都开启下一个任务
                downloadingTask.remove(data);
                if (waitTask.size() > 0)
                    download(waitTask.remove(0));
            })
            .to(RxLife.as(this)) //加入感知生命周期的观察者
            .subscribe(s -> { //s为String类型
                Tip.show("下载完成" + s);
                data.setState(4);
                notifyDataSetChanged(true);
                //下载成功，处理相关逻辑
            }, (OnError) error -> {
                data.setState(5);
                //下载失败，处理相关逻辑
            });
        data.setState(2);
        downloadingTask.add(data);
        data.setDisposable(disposable);
    }
}
