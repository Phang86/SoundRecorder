package com.yyzy.soundrecorder.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.yyzy.soundrecorder.R;
import com.yyzy.soundrecorder.adapter.AudioListAdapter;
import com.yyzy.soundrecorder.databinding.ActivityAudioListBinding;
import com.yyzy.soundrecorder.entity.AudioEntity;
import com.yyzy.soundrecorder.service.AudioService;
import com.yyzy.soundrecorder.util.AudioInfoUtils;
import com.yyzy.soundrecorder.util.ContantUtils;
import com.yyzy.soundrecorder.util.DialogUtils;
import com.yyzy.soundrecorder.util.InfoDialog;
import com.yyzy.soundrecorder.util.RenameDialog;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AudioListActivity extends AppCompatActivity {

    private ActivityAudioListBinding binding;
    private List<AudioEntity> mDatas;
    private AudioListAdapter adapter;
    private AudioService audioService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAudioListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //绑定服务
        Intent intent = new Intent(this, AudioService.class);
        bindService(intent, connection, BIND_AUTO_CREATE);
        //初始化数据
        initDatas();
        //将音频对象集合保存到集合变量中
        ContantUtils.setAudioList(mDatas);
        //加载数据
        loadDatas();
        //为相关对象设置事件
        setEvents();
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            AudioService.AudioBinder audioBinder = (AudioService.AudioBinder) iBinder;
            audioService = audioBinder.getService();
            audioService.setOnPlayChangeListener(onPlayChangeListener);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    AudioService.OnPlayChangeListener onPlayChangeListener = new AudioService.OnPlayChangeListener() {
        @Override
        public void playChange(int changePosition) {
            adapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //当活动销毁则解绑服务
        unbindService(connection);
    }

    private void setEvents() {
        //播放按钮设置监听回调方法
        adapter.setOnItemPlayClickListener(new AudioListAdapter.OnItemPlayClickListener() {
            @Override
            public void OnItemPlayClick(AudioListAdapter adapter, View converView, View playView, int position) {
                for (int i = 0; i < mDatas.size(); i++) {
                    if (i == position) {
                        continue;
                    }
                    AudioEntity entity = mDatas.get(i);
                    entity.setPlay(false);
                }
                //获取当前条目的状态
                boolean play = mDatas.get(position).isPlay();
                mDatas.get(position).setPlay(!play);
                adapter.notifyDataSetChanged();
                audioService.cutMusicOrpPause(position);
            }
        });
        //给listview的item设置长按监听方法
        binding.audioLv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View playView, int position, long l) {
                showPopMenu(playView, position);
                return false;
            }
        });
        binding.audioIb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                audioService.closeMusic();
                startActivity(new Intent(AudioListActivity.this,RecorderActivity.class));
                finish();
            }
        });
    }

    //长按每一项item弹出popmenu窗口
    private void showPopMenu(View view, int position) {
        PopupMenu popupMenu = new PopupMenu(this, view, Gravity.RIGHT);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.listview_item_popmenu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.rename:
                        showRenameDialog(position);
                        break;
                    case R.id.information:
                        showInfoDialog(position);
                        break;
                    case R.id.delete:
                        delFilePosition(position);
                        break;
                }
                return false;
            }
        });
        popupMenu.show();
    }

    private void showInfoDialog(int position) {
        AudioEntity entity = mDatas.get(position);
        InfoDialog dialog = new InfoDialog(this);
        dialog.show();
        dialog.setInfoDialogWidth();
        dialog.setInfo(entity);
        dialog.setCanceledOnTouchOutside(false);
    }

    private void showRenameDialog(int position) {
        AudioEntity entity = mDatas.get(position);
        String title = entity.getTitle();
        RenameDialog dialog = new RenameDialog(this);
        dialog.show();
        dialog.setDialogWidth();
        dialog.setTipText(title);
        dialog.setOnConfirmListener(new RenameDialog.OnConfirmListener() {
            @Override
            public void onConfirm(String msg) {
                renameByPosition(msg, position);
            }
        });
    }

    private void renameByPosition(String msg, int position) {
        AudioEntity entity = mDatas.get(position);
        if (entity.getTitle().equals(msg)) {
            return;
        }
        String path = entity.getPath();
        String fileSuffix = entity.getFileSuffix();
        File srcFile = new File(path);      //原来文件路径
        //获取修改文件
        String destPath = srcFile.getParent() + File.separator + msg + fileSuffix;
        File destFile = new File(destPath);
        //进行重命名物理操作
        srcFile.renameTo(destFile);
        //从内存中进行重命名操作
        entity.setTitle(msg);
        entity.setPath(destPath);
        adapter.notifyDataSetChanged();
    }

    //删除指定的item文件
    private void delFilePosition(int position) {
        AudioEntity entity = mDatas.get(position);
        String title = entity.getTitle();
        String path = entity.getPath();
        DialogUtils.showDialog(this, "温馨提示!", "是否删除该文件吗？", "是", new DialogUtils.OnLeftClickListener() {
            @Override
            public void onLeftClick() {
                File file = new File(path);
                file.getAbsoluteFile().delete();
                mDatas.remove(entity);
                adapter.notifyDataSetChanged();
            }
        }, "否", null);
    }

    private void initDatas() {
        //设置数据源和适配器
        mDatas = new ArrayList<>();
        adapter = new AudioListAdapter(this, mDatas);
        binding.audioLv.setAdapter(adapter);
    }

    private void loadDatas() {
        //1.获取文件夹下面的音频文件
        File fetchFile = new File(ContantUtils.PATH_FETCH_DIR_RECORDER);
        File[] files = fetchFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String name) {
                if (new File(file, name).isDirectory()) {
                    return false;
                }
                if (name.endsWith(".mp3") || name.endsWith(".amr")) {
                    return true;
                }
                return false;
            }
        });
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        AudioInfoUtils audioInfoUtils = AudioInfoUtils.getInstance();
        //2.遍历数组当中的文件，依次得到文件信息
        for (int i = 0; i < files.length; i++) {
            File audioFile = files[i];
            String fname = audioFile.getName();       //文件名带后缀
            String title = fname.substring(0, fname.lastIndexOf("."));    //得到不带后缀的文件名
            String suffix = fname.substring(fname.lastIndexOf("."));      //得到文件后缀
            //获取文件最后修改时间
            long flastMod = audioFile.lastModified();
            String time = sdf.format(flastMod);            //转换固定格式的字符串
            //获取文件的字节数
            long flength = audioFile.length();
            //获取文件的路径
            String audioPath = audioFile.getAbsolutePath();
            long duration = audioInfoUtils.getAudioFileDuration(audioPath);
            String formatDuration = audioInfoUtils.getAudioFileFormatDuration(duration);
            AudioEntity audioEntity = new AudioEntity(i + "", title, time, formatDuration,
                    audioPath, duration, flastMod, suffix, flength);
            mDatas.add(audioEntity);
        }
        //释放多媒体资源对象
        audioInfoUtils.releseRetriever();
        //将集合中的元素进行时间先后排序
        Collections.sort(mDatas, new Comparator<AudioEntity>() {
            @Override
            public int compare(AudioEntity t1, AudioEntity t2) {
                if (t1.getLastModefied() < t2.getLastModefied()) {
                    return 1;
                } else if (t1.getLastModefied() == t2.getLastModefied()) {
                    return 0;
                }
                return -1;
            }
        });
        adapter.notifyDataSetChanged();
    }

}