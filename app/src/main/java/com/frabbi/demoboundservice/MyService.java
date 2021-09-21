package com.frabbi.demoboundservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    private static final String TAG = "MyService";
    private final IBinder mBinder = new MyBinder();
    private Handler mHandler;
    private int mProgress, mMaxValue;
    private Boolean mIsPaused;

    public MyService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mHandler = new Handler();
        mProgress = 0;
        mIsPaused = true;
        mMaxValue = 500;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return mBinder;
    }

    public void startPretendLongRunningTask() {
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (mProgress >= mMaxValue || mIsPaused) {
                    Log.d(TAG, "run: removing callbacks.");
                    mHandler.removeCallbacks(this::run);
                    pausePretendLongRunningTask();
                } else {
                    Log.d(TAG, "run: progress: " + mProgress);
                    mProgress += 100;
                    mHandler.postDelayed(this, 100);
                }
            }
        };
        mHandler.postDelayed(runnable, 100);
    }

    public void pausePretendLongRunningTask() {
        mIsPaused = true;
    }

    public void unPausePretendLongRunningTask() {
        mIsPaused = false;
        startPretendLongRunningTask();
    }

    public void resetTask() {
        mProgress = 0;
    }

    public Boolean getIsPaused() {
        return mIsPaused;
    }

    public int getProgress() {
        return mProgress;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    //Inner class MyBinder
    public class MyBinder extends Binder {
        MyService getService() {
            return MyService.this;
        }
    }
}