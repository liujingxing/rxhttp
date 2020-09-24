package com.example.httpsender;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.httpsender.DownloadMultiAdapter.OnItemClickListener;
import com.example.httpsender.entity.DownloadTask;
import com.example.httpsender.vm.MultiTaskDownloader;

import java.util.ArrayList;

/**
 * 多任务下载
 * User: ljx
 * Date: 2019-06-07
 * Time: 11:02
 */
public class DownloadMultiActivity extends ToolBarActivity implements OnItemClickListener<DownloadTask> {

    private String[] downloadUrl = {
        "http://update.9158.com/miaolive/Miaolive.apk",//喵播
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk",//探探
        "https://o8g2z2sa4.qnssl.com/android/momo_8.18.5_c1.apk",//陌陌
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.download_multi_activity);

        ArrayList<DownloadTask> allTask = new ArrayList<>();  //所有下载任务
        for (int i = 0; i < downloadUrl.length; i++) {
            DownloadTask task = new DownloadTask(downloadUrl[i % downloadUrl.length]);
            task.setLocalPath(getExternalCacheDir() + "/" + i + ".apk");
            allTask.add(task);
        }
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        MultiTaskDownloader.addTasks(allTask);
        MultiTaskDownloader.getAllLiveTask().observe(this, tasks -> {
            Adapter adapter = recyclerView.getAdapter();
            if (adapter == null) {
                DownloadMultiAdapter multiAdapter = new DownloadMultiAdapter(tasks);
                multiAdapter.setOnItemClickListener(this);
                multiAdapter.setHasStableIds(true);
                recyclerView.setAdapter(multiAdapter);
            } else {
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (MultiTaskDownloader.haveTaskExecuting()) {
            new AlertDialog.Builder(this)
                .setMessage("当前有人任务正在执行，是否需要结束")
                .setPositiveButton("立即结束", (dialog, which) -> {
                    MultiTaskDownloader.cancelAllTask();
                    finish();
                })
                .setNegativeButton("后台继续下载", (dialog, which) -> {
                    finish();
                }).show();
        } else {
            super.onBackPressed();
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
                MultiTaskDownloader.startAllDownloadTask();
                item.setTitle("全部取消");
            } else if ("全部取消".contentEquals(item.getTitle())) {
                MultiTaskDownloader.cancelAllTask();
                item.setTitle("全部下载");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(View view, DownloadTask task, int position) {
        switch (view.getId()) {
            case R.id.bt_pause:
                int curState = task.getState();  //任务当前状态
                if (curState == MultiTaskDownloader.IDLE         //未开始->开始下载
                    || curState == MultiTaskDownloader.PAUSED    //暂停下载->继续下载
                    || curState == MultiTaskDownloader.CANCEL    //已取消->重新开始下载
                    || curState == MultiTaskDownloader.FAIL      //下载失败->重新下载
                ) {
                    MultiTaskDownloader.download(task);
                } else if (curState == MultiTaskDownloader.WAITING) {       //等待中->取消下载
                    MultiTaskDownloader.removeWaitTask(task);
                } else if (curState == MultiTaskDownloader.DOWNLOADING) {   //下载中->暂停下载
                    MultiTaskDownloader.pauseTask(task);
                } else if (curState == MultiTaskDownloader.COMPLETED) {   //任务已完成
                    Tip.show("该任务已完成");
                }
                break;
        }
    }
}
