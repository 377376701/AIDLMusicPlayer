package com.aidlmusicplayer.www.activity;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aidlmusicplayer.www.IMusicPlayer;
import com.aidlmusicplayer.www.IMusicPlayerListener;
import com.aidlmusicplayer.www.R;
import com.aidlmusicplayer.www.bean.MusicServiceBean;
import com.aidlmusicplayer.www.config.Constant;
import com.aidlmusicplayer.www.helper.GsonHelper;
import com.aidlmusicplayer.www.service.MusicService;
import com.aidlmusicplayer.www.ui.PlayerDiscView;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.glide.transformations.BlurTransformation;

public class MusicActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {

    @Bind(R.id.musics_player_disc_view)
    PlayerDiscView mPlayerDiscView;
    @Bind(R.id.musics_player_background)
    ImageView mMusicsPlayerBackground;
    @Bind(R.id.player_disc)
    ImageView mPlayerDisc;
    @Bind(R.id.player_disc_image)
    ImageView mPlayerDiscImage;
    @Bind(R.id.player_disc_container)
    RelativeLayout mPlayerDiscContainer;
    @Bind(R.id.player_needle)
    ImageView mPlayerNeedle;
    @Bind(R.id.musics_player_name)
    TextView mMusicsPlayerName;
    @Bind(R.id.musics_player_songer_name)
    TextView mMusicsPlayerSongerName;
    @Bind(R.id.musics_player_current_time)
    TextView mMusicsPlayerCurrentTime;

    @Bind(R.id.musics_player_total_time)
    TextView mMusicsPlayerTotalTime;
    @Bind(R.id.musics_player_progress_container)
    LinearLayout mMusicsPlayerProgressContainer;
    @Bind(R.id.musics_player_play_prev_btn)
    ImageButton mMusicsPlayerPlayPrevBtn;
    @Bind(R.id.musics_player_play_ctrl_btn)
    ImageButton mMusicsPlayerPlayCtrlBtn;
    @Bind(R.id.musics_player_play_next_btn)
    ImageButton mMusicsPlayerPlayNextBtn;
    @Bind(R.id.musics_player_loading_view)
    View mMusicsPlayerLoadingView;
    @Bind(R.id.musics_player_container)
    RelativeLayout mMusicsPlayerContainer;
    private IMusicPlayer mMusicPlayer;

    @Bind(R.id.musics_player_seekbar)
    SeekBar mMusicsPlayerSeekbar;
    private MusicServiceBean mMusicServiceBean;
    private SimpleDateFormat mFormatter;


    int FROM_THE_SERVER = 424;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        ButterKnife.bind(this);


        mMusicServiceBean = getIntent().getParcelableExtra(Constant.TAG_FLAG_1);
        setTitle("");

        bindService();

//       a. get data


        mFormatter = new SimpleDateFormat("mm:ss");
        mMusicsPlayerSeekbar.setOnSeekBarChangeListener(this);


    }

    private void setTitleAndBackground(String title, String backgroundUrl) {
        setTitle(title);
        Glide.with(this).load(backgroundUrl)
                .bitmapTransform(new BlurTransformation(this))
                .into(mMusicsPlayerBackground);
    }

    private void bindService() {
        Intent intent = new Intent(this, MusicService.class);
//        intent.putExtra(Constant.TAG_FLAG_5, new Messenger(mHandler));// use messenger
        bindService(intent, mServiceConnection, Service.BIND_AUTO_CREATE);
    }

    ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mMusicPlayer = IMusicPlayer.Stub.asInterface(service);
            try {
                mMusicPlayer.asBinder().linkToDeath(mDeathRecipient, 0);
                //playSong music
                mMusicPlayer.action(MusicService.MUSIC_ACTION_PLAY, GsonHelper.getGson().toJson(mMusicServiceBean));
                mMusicPlayer.registerListener(mPlayerListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {


        }
    };

    IMusicPlayerListener mPlayerListener = new IMusicPlayerListener.Stub() {
        @Override
        public void action(int action, Message msg) throws RemoteException {
            mHandler.sendMessage(msg);
        }
    };


    private void updateSeek(Message msg) {
        int currentPosition = msg.arg1;
        int totalDuration = msg.arg2;
        mMusicsPlayerTotalTime.setText(mFormatter.format(totalDuration));
        mMusicsPlayerCurrentTime.setText(mFormatter.format(currentPosition));
        mMusicsPlayerSeekbar.setMax(totalDuration);
        mMusicsPlayerSeekbar.setProgress(currentPosition);
    }


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MusicService.MUSIC_ACTION_SEEK_PLAY:
                    updateSeek(msg);
                    break;
                case MusicService.MUSIC_ACTION_PLAY:
                    int position = msg.arg1;
                    setTitleAndBackground(mMusicServiceBean.song_list.get(position).title,
                            mMusicServiceBean.song_list.get(position).pic_big);
                    mPlayerDiscView.loadAlbumCover(mMusicServiceBean.song_list.get(position).pic_big);
                    mPlayerDiscView.startPlay();
                    break;


                default:
                    super.handleMessage(msg);
            }

        }
    };


    private IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            bindService();
        }
    };

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        unbindService(mServiceConnection);
//        try {
//            mMusicPlayer.unregisterListener(mPlayerListener);
//        } catch (RemoteException e) {
//            e.printStackTrace();
//        }
//
//    }

    /******************************************************************/

    @OnClick({R.id.musics_player_play_prev_btn, R.id.musics_player_play_ctrl_btn, R.id.musics_player_play_next_btn})
    public void onClick(View view) {
        try {
            switch (view.getId()) {
                case R.id.musics_player_play_prev_btn:
                    mMusicPlayer.action(MusicService.MUSIC_ACTION_PREVIOUS, "");

                    break;
                case R.id.musics_player_play_ctrl_btn:

                    onPayBtnPress();

                    break;
                case R.id.musics_player_play_next_btn:

                    mMusicPlayer.action(MusicService.MUSIC_ACTION_NEXT, "");

                    break;
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void onPayBtnPress() throws RemoteException {
        switch (MusicService.MUSIC_CURRENT_ACTION) {
            case MusicService.MUSIC_ACTION_PLAY:
                mMusicPlayer.action(MusicService.MUSIC_ACTION_PAUSE, "");

                break;
            case MusicService.MUSIC_ACTION_STOP:
                mMusicPlayer.action(MusicService.MUSIC_ACTION_PLAY, GsonHelper.getGson().toJson(mMusicServiceBean));

                break;
            case MusicService.MUSIC_ACTION_PAUSE:
                mMusicPlayer.action(MusicService.MUSIC_ACTION_CONTINUE_PLAY, "");

                break;
        }
    }

    /******************************************************************/
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        try {
            seekBar.setProgress(seekBar.getProgress());
            mMusicPlayer.action(MusicService.MUSIC_ACTION_SEEK_PLAY, String.valueOf(seekBar.getProgress()));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
