package com.vithamastech.smartlight.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.os.Build;
import androidx.core.app.ActivityCompat;

import com.vithamastech.smartlight.console;

import java.io.File;
import java.io.IOException;

public class SoundMeter {

    static final private double EMA_FILTER = 0.6;

    private MediaRecorder mRecorder = null;
    private double mEMA = 0.0;
    public static final int RECORD_AUDIO = 0;

    public void start(Activity mActivity) {
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
//            mRecorder.setAudioSamplingRate(16000);
//            mRecorder.setAudioEncodingBitRate(12800);
//            mRecorder.setAudioSamplingRate(8000);
//            mRecorder.setAudioEncodingBitRate(12800);
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
//            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            if (Build.VERSION.SDK_INT >= 10) {
                this.mRecorder.setAudioSamplingRate(44100);
                this.mRecorder.setAudioEncodingBitRate(96000);
                this.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                // Changed by Muataz Medini
                //mRecorder.setOutputFile("/dev/null/"); // Android 11 wont accept /dev/null file path.
                mRecorder.setOutputFile(getFilePath(mActivity.getApplicationContext()));
            } else {
                this.mRecorder.setAudioSamplingRate(8000);
                this.mRecorder.setAudioEncodingBitRate(12200);
                this.mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                mRecorder.setOutputFile(mActivity.getCacheDir().getAbsolutePath());
            }

            try {
                if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.RECORD_AUDIO},
                            RECORD_AUDIO);
                } else {
                    mRecorder.prepare();
                    mRecorder.start();
                    mEMA = 0.0;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IllegalStateException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
//            mRecorder.setAudioSamplingRate(16000);
//            mRecorder.setAudioEncodingBitRate(12800/3);
//            mRecorder.setAudioChannels(1);
        }
    }

    public void stop() {
        try {
            if (mRecorder != null) {
                mRecorder.stop();
                mRecorder.reset();
                mRecorder.release();
                mRecorder = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getTheAmplitude() {
//        if (mRecorder != null)
//            return (mRecorder.getMaxAmplitude());
        return mRecorder != null ? (double) (mRecorder.getMaxAmplitude() / 10) : 0;
//        else
//            return 1;
    }

    public double getAmplitude() {
        if (mRecorder != null) {
//            System.out.println("getMaxAmplitude " + mRecorder.getMaxAmplitude());
            return (mRecorder.getMaxAmplitude() / 2700.0);
        } else
            return 0;

    }

    /*
    Edited by Muataz Medini
     */
    public double getAmplitudes() {
//        if (mRecorder != null) {
////            System.out.println("getMaxAmplitude " + mRecorder.getMaxAmplitude());
//            return (mRecorder.getMaxAmplitude());
//        } else{
//            return 0;
//        }

        double maxAmplitude = 0;
        if (mRecorder != null) {
            try {
                maxAmplitude = mRecorder.getMaxAmplitude();
                console.log("asxubaiusxuasuxabusix",maxAmplitude);
//                System.out.println("getMaxAmplitude " + mRecorder.getMaxAmplitude());
            } catch (Exception e) {
                e.printStackTrace();
                // Todo Place general alert dialog showing error
                stop();
            }
        }
        return maxAmplitude;
    }

    public double getAmplitudeDecibel() {
        if (mRecorder != null)
            return 20 * Math.log10(mRecorder.getMaxAmplitude() / 2700.0);
        else
            return 0;
    }

    public double getAmplitudeEMA() {
        double amp = getAmplitude();
        mEMA = EMA_FILTER * amp + (1.0 - EMA_FILTER) * mEMA;
        return mEMA;
    }

    private String getFilePath(Context context){
        ContextWrapper cw = new ContextWrapper(context);
//        File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
        File directory = cw.getCacheDir();
        // Create imageDir
        long currentEpochTime = System.currentTimeMillis();
        File myPath = new File(directory, "audiorecordtest.3gp");
        return myPath.getAbsolutePath();
    }
}
