package com.yscoco.lib.util;

import static android.content.Context.AUDIO_SERVICE;

import android.media.AudioManager;

public class AudioUtil {

    public static void resumeMusic() {
        AudioManager audioManager = (AudioManager) ContextUtil.getAppContext()
                .getSystemService(AUDIO_SERVICE);
        audioManager.abandonAudioFocus(null);
    }

    public static void stopMusic() {
        AudioManager audioManager = (AudioManager) ContextUtil.getAppContext()
                .getSystemService(AUDIO_SERVICE);
        audioManager.requestAudioFocus(null, AudioManager.STREAM_RING,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }
}
