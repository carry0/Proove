package com.proove.smart.manager;

import static android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.proove.smart.R;
import com.yscoco.lib.util.ContextUtil;
import com.yscoco.lib.util.LogUtil;

import java.util.ArrayList;
import java.util.List;


/**
 * MediaPlayUtils 播放声音
 */
public class MediaPlayManager {
    private static final String TAG = "MediaPlayManager";

    private MediaPlayer mMediaPlayer;
    private AudioManager mAudioManager;
    private boolean isPlay;
    private boolean isMaxVolume;
    private int currentMediaVolume = -1;
    private AudioManager audioManager;
    private AudioFocusRequest audioFocusRequest;
    private final List<IStateListener> listeners = new ArrayList<>();

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final AudioManager.OnAudioFocusChangeListener mListener = focusChange -> {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                // TBD 继续播放
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                // TBD 停止播放
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                // TBD 暂停播放
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                // TBD 混音播放
                break;
            default:
                break;
        }

    };

    private MediaPlayManager() {
    }

    public static MediaPlayManager getInstance() {
        return Singleton.instance;
    }

    private static final class Singleton {
        private static final MediaPlayManager instance = new MediaPlayManager();
    }

    public interface IStateListener {
        void onMediaStart();

        void onMediaPause();

        void onMediaStop();
    }

    public void init() {
        mAudioManager = (AudioManager) ContextUtil.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        mMediaPlayer = MediaPlayer.create(ContextUtil.getAppContext(), R.raw.opening);
        mMediaPlayer.setLooping(true);
        if (null == audioManager) {
            audioManager = (AudioManager) ContextUtil.getAppContext().getSystemService(Context.AUDIO_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest = new AudioFocusRequest.Builder(AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    .setWillPauseWhenDucked(true)
                    .setAcceptsDelayedFocusGain(true)
                    .setOnAudioFocusChangeListener(mListener, mHandler)
                    .build();
            int i = audioManager.requestAudioFocus(audioFocusRequest);
        }
        mMediaPlayer.setOnCompletionListener(mp -> stop());
    }

    public void play(boolean isMaxVolume) {
        this.isMaxVolume = isMaxVolume;
        try {
            init();
            start();
            if (isMaxVolume) {
                setMaxVolume();
            }
            isPlay = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.pause();
        notifyOnPause();
    }

    public void start() {
        if (mMediaPlayer == null) {
            return;
        }
        mMediaPlayer.start();
        notifyOnStart();
    }

    public void stop() {
        LogUtil.info(TAG, "stop ringing");
        try {
            if (mMediaPlayer != null) {
                if (currentMediaVolume != -1 && isPlay) {
                    setVolume(currentMediaVolume);
                }
                isPlay = false;

                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (null != audioManager && null != audioFocusRequest) {
                    audioManager.abandonAudioFocusRequest(audioFocusRequest);
                }
            }
            notifyOnStop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isPlay() {
        return isPlay;
    }

    private void setMaxVolume() {
        //最大音量
        int mediaMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //当前音量
        currentMediaVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        setVolume(mediaMaxVolume);
    }

    //设置音量
    private void setVolume(int volume) {
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    public void addStateListener(IStateListener listener) {
        if (listener == null || listeners.contains(listener)) {
            return;
        }
        listeners.add(listener);
    }

    public void removeStateListener(IStateListener listener) {
        listeners.remove(listener);
    }

    private void notifyOnStart() {
        for (IStateListener listener : listeners) {
            listener.onMediaStart();
        }
    }

    private void notifyOnPause() {
        for (IStateListener listener : listeners) {
            listener.onMediaPause();
        }
    }

    private void notifyOnStop() {
        for (IStateListener listener : listeners) {
            listener.onMediaStop();
        }
    }
}
