package com.patil.geobells.lite.service;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import com.patil.geobells.lite.utils.Constants;

import java.util.HashMap;
import java.util.Locale;

public class SpeakService extends Service implements TextToSpeech.OnInitListener {

    private TextToSpeech mTts;
    String text;

    @Override
    public void onCreate() {
        super.onCreate();
        mTts = new TextToSpeech(this, this);
        mTts.setLanguage(Locale.getDefault());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mTts != null) {
            mTts.stop();
            mTts.shutdown();
        }
    }

    public SpeakService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onInit(int i) {
        if(i == TextToSpeech.LANG_AVAILABLE) {
            mTts.setOnUtteranceCompletedListener(new TextToSpeech.OnUtteranceCompletedListener() {
                public void onUtteranceCompleted(String string) {
                    SpeakService.this.stopSelf();
                }
            });
        }
        sayText(text);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        text = intent.getExtras().getString(Constants.EXTRA_SPEAKTEXT);
        return START_NOT_STICKY;
    }

    public void sayText(String text) {
        AudioManager localAudioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
        localAudioManager.setSpeakerphoneOn(true);
        HashMap localHashMap = new HashMap();
        localHashMap.put("utteranceId", "finished");
        int i = localAudioManager.getStreamVolume(3);
        int j = localAudioManager.getStreamMaxVolume(3);
        if (i < j / 2) {
            localAudioManager.setStreamVolume(3, j / 2, 0);
        }
        this.mTts.speak(text, 0, localHashMap);
    }
}
