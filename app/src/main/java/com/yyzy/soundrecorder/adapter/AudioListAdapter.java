package com.yyzy.soundrecorder.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.yyzy.soundrecorder.R;
import com.yyzy.soundrecorder.databinding.ItemAudioBinding;
import com.yyzy.soundrecorder.entity.AudioEntity;

import java.util.List;

public class AudioListAdapter extends BaseAdapter {
    private Context context;
    private List<AudioEntity> mDatas;

    //点击每个item播放按钮能够回调的接口
    public interface OnItemPlayClickListener {
        void OnItemPlayClick(AudioListAdapter adapter, View converView, View playView, int position);
    }

    private OnItemPlayClickListener onItemPlayClickListener;

    public void setOnItemPlayClickListener(OnItemPlayClickListener onItemPlayClickListener) {
        this.onItemPlayClickListener = onItemPlayClickListener;
    }

    public AudioListAdapter(Context context, List<AudioEntity> mlist) {
        this.context = context;
        this.mDatas = mlist;
    }

    @Override
    public int getCount() {
        return mDatas.size();
    }

    @Override
    public Object getItem(int i) {
        return mDatas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        ViewHolder viewHolder = null;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_audio, viewGroup, false);
            viewHolder = new ViewHolder(view);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        AudioEntity data = mDatas.get(i);
        viewHolder.ab.tvDate.setText(data.getTime());
        viewHolder.ab.tvDuration.setText(data.getDuration());
        viewHolder.ab.tvTitle.setText(data.getTitle());
        if (data.isPlay()) {
            viewHolder.ab.tvllayout.setVisibility(View.VISIBLE);
            viewHolder.ab.pb.setMax(100);
            viewHolder.ab.pb.setProgress(data.getCurrentProgress());
            viewHolder.ab.ivPlay.setImageResource(R.mipmap.red_pause);
        } else {
            viewHolder.ab.ivPlay.setImageResource(R.mipmap.red_play);
            viewHolder.ab.tvllayout.setVisibility(View.GONE);
        }
        View itemView = view;
        //设置点击事件  点击播放图标可播放或暂停
        viewHolder.ab.ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemPlayClickListener != null) {
                    onItemPlayClickListener.OnItemPlayClick(AudioListAdapter.this, itemView, view, i);
                }
            }
        });
        return view;
    }

    class ViewHolder {
        private ItemAudioBinding ab;
        public ViewHolder(View view) {
            ab = ItemAudioBinding.bind(view);
        }
    }
}
