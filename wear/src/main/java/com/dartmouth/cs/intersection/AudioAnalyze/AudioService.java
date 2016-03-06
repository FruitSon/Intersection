package com.dartmouth.cs.intersection.AudioAnalyze;

import android.app.Service;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.util.Log;

import com.dartmouth.cs.intersection.AudioAnalyze.audio.MicrophoneRecorder;
import com.dartmouth.cs.intersection.Global;
import com.dartmouth.cs.intersection.service.MobileMsgService;

public class AudioService extends Service implements MicrophoneRecorder.MicrophoneListener{

    private Object[] result = new Object[12];
    private FeaturizeWeka weka;
    private int global_voiced = 0;
    private int[] buffer;

    private int voiced = 0;
    private int un_voiced = 0;
    private int voiced_min = 0;
    private int un_voiced_min = 0;


    public AudioService() {
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        MicrophoneRecorder recorder = MicrophoneRecorder.getInstance();
        recorder.registerListener(AudioService.this);
        recorder.startRecording();

        weka = new FeaturizeWeka();
        weka.init();
        buffer = new int[40];
        for(int i=0;i<40;i++)
            buffer[i]=1;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void microphoneBuffer(short[] buffer, int window_size) {
        int voiced = 0;
        int size = 200;


        //TODO: Fill out appropriate numbers in the following for loop statement
        for(int k=0;k<200;k+=size){
            //TODO: replace ‘??’ with appropriate numbers below
            double[] features = weka.ComputeFeaturesForFrame(buffer,size,0,k);
            try {
                result=getObjectDoubleArray(features,result);
                if(0 == (int)WekaClassifier.classify(result)) {
                    global_voiced++;
                    if(global_voiced>10)
                        global_voiced=10;
                    Log.d("Voice", "Yes");
                } else {
                    global_voiced--;
                    if(global_voiced<-10)
                        global_voiced = -10;
                    Log.d("Voice","No");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(global_voiced>8){
            voiced = voiced++;
        } else {
            un_voiced = un_voiced++;
        }

        //calculate time
        if(voiced>2399){
            voiced = 0;
            voiced_min = voiced_min +1;
        }

        if(un_voiced>2399){
            un_voiced = 0;
            un_voiced_min = un_voiced_min +1;
        }

        new CountDownTimer(24*60*60*1000, 1000) {
            public void onFinish() {
                String audio;
                int total = voiced_min+un_voiced_min;
                double voiced_p = Math.round(voiced_min/total*100)/100.00;
                double un_voiced_p = Math.round(un_voiced_min/total*100)/100.00;;
                audio = voiced_p+","+un_voiced_p;
                MobileMsgService.sendMessage(Global.AUDIO,audio);
            }

            public void onTick(long millisUntilFinished) {
            }
        }.start();

    }

    public static Object[] getObjectDoubleArray(double[] in,Object[] result){
        for (int i=0;i<in.length;i++){
            result[i] = new Double(in[i]);
        }
        return result;
    }
}
