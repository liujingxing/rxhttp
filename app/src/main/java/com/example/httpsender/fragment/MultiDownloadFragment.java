package com.example.httpsender.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.Adapter;

import com.example.httpsender.DownloadMultiAdapter;
import com.example.httpsender.DownloadMultiAdapter.OnItemClickListener;
import com.example.httpsender.R;
import com.example.httpsender.Tip;
import com.example.httpsender.databinding.MultiDownloadFragmentBinding;
import com.example.httpsender.entity.DownloadTask;
import com.example.httpsender.vm.MultiTaskDownloader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * User: ljx
 * Date: 2021/9/25
 * Time: 18:08
 */
public class MultiDownloadFragment extends BaseFragment<MultiDownloadFragmentBinding> implements OnItemClickListener<DownloadTask>, OnClickListener {

    private final String[] downloadUrl = {
        "http://update.9158.com/miaolive/Miaolive.apk",//喵播
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk",//探探
        "https://o8g2z2sa4.qnssl.com/android/momo_8.18.5_c1.apk",//陌陌
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.multi_download_fragment);
    }

    @Override
    public void onViewCreated(@NotNull MultiDownloadFragmentBinding binding, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(binding, savedInstanceState);
        binding.setClick(this);
        ArrayList<DownloadTask> allTask = new ArrayList<>();  //所有下载任务
        for (int i = 0; i < downloadUrl.length; i++) {
            DownloadTask task = new DownloadTask(downloadUrl[i % downloadUrl.length]);
            task.setLocalPath(getContext().getExternalCacheDir() + "/" + i + ".apk");
            allTask.add(task);
        }
        RecyclerView recyclerView = binding.recyclerView;

        MultiTaskDownloader.addTasks(allTask);
        MultiTaskDownloader.getAllLiveTask().observe(getViewLifecycleOwner(), tasks -> {
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start_all:
                MultiTaskDownloader.startAllDownloadTask();
                break;
            case R.id.cancel_all:
                MultiTaskDownloader.cancelAllTask();
                break;
        }
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
