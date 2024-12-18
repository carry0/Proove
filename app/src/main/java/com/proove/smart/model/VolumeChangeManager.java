package com.proove.smart.model;

import static android.media.AudioManager.FLAG_SHOW_UI;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import java.lang.ref.WeakReference;

public class VolumeChangeManager {

    private static final String VOLUME_CHANGED_ACTION = "android.media.VOLUME_CHANGED_ACTION";
    private static final String EXTRA_VOLUME_STREAM_TYPE = "android.media.EXTRA_VOLUME_STREAM_TYPE";
    private VolumeChangeListener mVolumeChangeListener;
    private VolumeBroadcastReceiver mVolumeBroadcastReceiver;
    private final Context mContext;
    private final AudioManager mAudioManager;
    private boolean mRegistered = false;

    public interface VolumeChangeListener {
        /**
         * 系统媒体音量变化
         *
         * @param volume 音量
         */
        void onSystemVolumeChanged(int volume);
    }

    public VolumeChangeManager(Context context) {
        mContext = context;
        mAudioManager = (AudioManager) context.getApplicationContext()
                .getSystemService(Context.AUDIO_SERVICE);
    }

    public int getCurrentMusicVolume() {
        return mAudioManager != null ? mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC) : -1;
    }

    public int getMaxMusicVolume() {
        return mAudioManager != null ? mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) : 15;
    }

    public int getVolumePercent() {
        return getCurrentMusicVolume() * 100 / getMaxMusicVolume();
    }

    public VolumeChangeListener getVolumeChangeListener() {
        return mVolumeChangeListener;
    }

    public void setVolumeChangeListener(VolumeChangeListener volumeChangeListener) {
        this.mVolumeChangeListener = volumeChangeListener;
    }

    public void setVolume(int volume) {
        if (mAudioManager == null) {
            return;
        }
        if (volume < 0 || volume > getMaxMusicVolume()) {
            return;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, FLAG_SHOW_UI);
    }

    public void setVolumePercent(int percent) {
        if (mAudioManager == null) {
            return;
        }
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (getMaxMusicVolume() * 0.01f * percent), 0);
    }

    public void adjustVolume(int direction, int flag) {
        if (mAudioManager == null) {
            return;
        }
        int tempFlag = flag;
        if (getCurrentMusicVolume() == getMaxMusicVolume()) {
            tempFlag = FLAG_SHOW_UI;
        }
        mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, direction, tempFlag);
    }

    public void registerReceiver() {
        mVolumeBroadcastReceiver = new VolumeBroadcastReceiver(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction(VOLUME_CHANGED_ACTION);
        mContext.registerReceiver(mVolumeBroadcastReceiver, filter);
        mRegistered = true;
    }

    public void unregisterReceiver() {
        if (mRegistered) {
            try {
                mContext.unregisterReceiver(mVolumeBroadcastReceiver);
                mVolumeChangeListener = null;
                mRegistered = false;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isMusicActive() {
        if (mAudioManager == null) {
            return false;
        }
        return mAudioManager.isMusicActive();
    }

    private static class VolumeBroadcastReceiver extends BroadcastReceiver {
        private final WeakReference<VolumeChangeManager> mObserverWeakReference;

        public VolumeBroadcastReceiver(VolumeChangeManager volumeChangeManager) {
            mObserverWeakReference = new WeakReference<>(volumeChangeManager);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            //媒体音量改变才通知
            if (VOLUME_CHANGED_ACTION.equals(intent.getAction())
                    && (intent.getIntExtra(EXTRA_VOLUME_STREAM_TYPE, -1) == AudioManager.STREAM_MUSIC)) {
                VolumeChangeManager observer = mObserverWeakReference.get();
                if (observer != null) {
                    VolumeChangeListener listener = observer.getVolumeChangeListener();
                    if (listener != null) {
                        int volume = observer.getCurrentMusicVolume();
                        if (volume >= 0) {
                            listener.onSystemVolumeChanged(volume);
                        }
                    }
                }
            }
        }
    }
}