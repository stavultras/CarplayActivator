package com.ultras.carplayactivator;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;

import android.media.MediaPlayer;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends Activity
{
    protected MediaPlayer mp = null;
    protected MainActivity my = null;
    protected MediaRecorder mRecorder = null;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        my = this;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            AssetFileDescriptor afd = null;
            try {
                afd = getAssets().openFd("silence-500ms.mp3");

                Log.i("Carplay Activator", "player start");

                mp = new MediaPlayer();
                mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
                mp.prepare();
                mp.start();

                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    public void onCompletion(MediaPlayer mp) {
                        mp.reset();
                        mp.release();

                        Log.i("Carplay Activator", "player stop");

                        mRecorder = new MediaRecorder();
                        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
                        mRecorder.setOutputFile("/dev/null");
                        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

                        try {
                            mRecorder.prepare();
                            mRecorder.start();

                            Log.i("Carplay Activator", "record start");

                            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        mRecorder.stop();
                                        mRecorder.reset();
                                        mRecorder.release();
                                        mRecorder = null;

                                        Log.i("Carplay Activator", "record stop");
                                    } catch (Exception e) {

                                    }

                                    my.finishAffinity();
                                }
                            }, 200);

                        } catch (Exception e) {

                        }
                    }
                });
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            String[] privs = {android.Manifest.permission.RECORD_AUDIO};

            ActivityCompat.requestPermissions(this, privs, 777);

            this.finishAffinity();
        }
    }
}