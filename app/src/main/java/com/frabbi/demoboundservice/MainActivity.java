package com.frabbi.demoboundservice;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    ProgressBar progressBar;
    TextView textView;
    Button btn;
    // //
    private MyService mService;
    private MyViewModel myViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBar = findViewById(R.id.progress_bar);
        textView = findViewById(R.id.textView);
        btn = findViewById(R.id.btn);

        myViewModel = new ViewModelProvider(this).get(MyViewModel.class);
        myViewModel.getBinder().observe(this, new Observer<MyService.MyBinder>() {
            @Override
            public void onChanged(MyService.MyBinder myBinder) {
                if (myBinder != null) {
                    Log.d(TAG, "onChanged: connected to service.");
                    mService = myBinder.getService();
                } else {
                    Log.d(TAG, "onChanged: unbound from service.");
                    mService = null;
                }
            }
        });

        btn.setOnClickListener(view -> {
            progressUpdate();
        });

        myViewModel.getIsProgressUpdating().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(final Boolean aBoolean) {
                final Handler handler = new Handler();
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        if (aBoolean) {
                            if (myViewModel.getBinder().getValue() != null) {
                                if (mService.getProgress() == mService.getMaxValue()) {
                                    myViewModel.setIsProgressUpdating(false);
                                }
                                progressBar.setProgress(mService.getProgress());
                                progressBar.setMax(mService.getMaxValue());
                                String progress =
                                        String.valueOf(100 * mService.getProgress() /
                                                mService.getMaxValue() + " %");
                                textView.setText(progress);
                                handler.postDelayed(this,100);
                            }
                        } else {
                               handler.removeCallbacks(this);

                        }
                    }
                };
                handler.postDelayed(runnable,100);
                if(aBoolean){
                    btn.setText("Pause");
                }else {
                    if (mService.getProgress() == mService.getMaxValue()) {
                        btn.setText("Restart");
                    }else{
                        btn.setText("Start");
                    }
                }

            }
        });


    }

    private void progressUpdate() {
        if (mService != null) {
            if (mService.getProgress() == mService.getMaxValue()) {
                mService.resetTask();
                btn.setText("start");
            } else {
                if (mService.getIsPaused()) {
                    mService.unPausePretendLongRunningTask();
                    myViewModel.setIsProgressUpdating(true);
                } else {
                    mService.pausePretendLongRunningTask();
                    myViewModel.setIsProgressUpdating(false);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(myViewModel.getServiceConnection());
    }

    @Override
    protected void onResume() {
        super.onResume();
        startService();
    }

    private void startService() {
        Intent intent = new Intent(this, MyService.class);
        startService(intent);

        bindService();
    }

    private void bindService() {
        Intent intent = new Intent(this, MyService.class);
        bindService(intent, myViewModel.getServiceConnection(), Context.BIND_AUTO_CREATE);
    }
}