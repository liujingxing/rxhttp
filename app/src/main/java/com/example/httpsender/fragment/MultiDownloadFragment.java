package com.example.httpsender.fragment;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

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
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/uS12nZLR.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/BYGanTMW.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/Iu9hZLL8.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/DdKLk5VX.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/Byww5X8k.ts",
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?111",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?222",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?333",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?444",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?555",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?666",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?777",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?888",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?999",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?101",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?102",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?103",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?104",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?105",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?106",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?107",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk?108",//探探
        "https://apk-ssl.tancdn.com/3.5.3_276/%E6%8E%A2%E6%8E%A2.apk",//探探
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/OUkREagY.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/ZJUsgPSd.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/I5ivzoXR.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/PFPXapY7.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/tJj2JTVy.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/mj2fFYjH.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/MOXijkzw.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/uiwVyFej.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/HijAOXaK.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/h2mFS6ef.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/vf8fjmJd.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/0jsVXFSa.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/aZfnIVnP.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/l7cddTCQ.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/QaMi23d0.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/ljLywei6.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/FQpwzm4U.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/4oW2C2iZ.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/57OL3KeG.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/vYlV9nTw.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/XUmd5HWF.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/btVvEY5r.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/eJRKGoaP.ts",
//        "https://hnzy4.jinhaiwzhs.com:65/20210625/mndoRF1O/2000kb/hls/iz5kx1X1.ts",
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
            String url = downloadUrl[i];
            DownloadTask task = new DownloadTask(url);
            String suffix = url.substring(url.lastIndexOf("."));
            task.setLocalPath(getContext().getExternalCacheDir() + "/" + i + suffix);
            allTask.add(task);
        }
        RecyclerView recyclerView = binding.recyclerView;

        MultiTaskDownloader.addTasks(allTask);
        DownloadMultiAdapter multiAdapter = new DownloadMultiAdapter(MultiTaskDownloader.getAllTask());
        multiAdapter.setOnItemClickListener(this);
        recyclerView.setAdapter(multiAdapter);
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        MultiTaskDownloader.getLiveTask().observe(getViewLifecycleOwner(), task -> {
            int index = MultiTaskDownloader.getAllTask().indexOf(task);
            if (index != -1) {
                //任务有更新，刷新单个item
                multiAdapter.notifyItemChanged(index);
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
        if (view.getId() == R.id.bt_pause) {
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
        }
    }
}
