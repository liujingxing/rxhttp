package com.example.httpsender;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.httpsender.DownloadMultiAdapter.MyViewHolder;
import com.example.httpsender.entity.DownloadTask;

import java.text.DecimalFormat;
import java.util.List;

/**
 * User: ljx
 * Date: 2019-06-07
 * Time: 11:10
 */
public class DownloadMultiAdapter extends RecyclerView.Adapter<MyViewHolder> {

    private OnItemClickListener<DownloadTask> mOnItemClickListener;

    private List<DownloadTask> mDownloadTasks;

    public DownloadMultiAdapter(List<DownloadTask> downloadTasks) {
        mDownloadTasks = downloadTasks;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.download_multi_adapter, viewGroup, false);
        return new MyViewHolder(view);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int i) {
        DownloadTask data = mDownloadTasks.get(i);
        viewHolder.progressBar.setProgress(data.getProgress());
        viewHolder.tvProgress.setText(String.format("%d%%", data.getProgress()));
        viewHolder.btPause.setOnClickListener(v -> {
            mOnItemClickListener.onItemClick(v, data, i);
        });
        String currentSize = new DecimalFormat("0.0").format(data.getCurrentSize() * 1.0f / 1024 / 1024);
        String totalSize = new DecimalFormat("0.0").format(data.getTotalSize() * 1.0f / 1024 / 1024);
        viewHolder.tvSize.setText(String.format("%sM/%sM", currentSize, totalSize));

        int state = data.getState();
        if (state == 0) {
            viewHolder.tvWaiting.setText("未开始");
            viewHolder.btPause.setText("开始");
        } else if (state == 1) {
            viewHolder.tvWaiting.setText("等待中..");
            viewHolder.btPause.setText("取消");
        } else if (state == 2) {
            viewHolder.tvWaiting.setText("下载中..");
            viewHolder.btPause.setText("暂停");
        } else if (state == 3) {
            viewHolder.tvWaiting.setText("已暂停");
            viewHolder.btPause.setText("继续");
        } else if (state == 4) {
            viewHolder.tvWaiting.setText("已完成");
            viewHolder.btPause.setText("已完成");
        } else if (state == 5) {
            viewHolder.tvWaiting.setText("下载失败");
            viewHolder.btPause.setText("下载失败");
        } else if (state == 6) {
            viewHolder.tvWaiting.setText("已取消");
            viewHolder.btPause.setText("重新下载");
        }

    }

    @Override
    public int getItemCount() {
        return mDownloadTasks.size();
    }

    @Override
    public long getItemId(int position) {
        return mDownloadTasks.get(position).hashCode();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;
        TextView tvProgress;
        TextView tvSize;
        Button btPause;
        TextView tvWaiting;


        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvWaiting = itemView.findViewById(R.id.tv_waiting);
            tvProgress = itemView.findViewById(R.id.tv_progress);
            tvSize = itemView.findViewById(R.id.tv_size);
            progressBar = itemView.findViewById(R.id.progress_bar);
            btPause = itemView.findViewById(R.id.bt_pause);
        }
    }


    public void setOnItemClickListener(OnItemClickListener<DownloadTask> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener<T> {
        void onItemClick(View view, T data, int position);
    }
}
