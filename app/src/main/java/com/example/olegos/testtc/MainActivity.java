package com.example.olegos.testtc;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.example.olegos.testtc.Constants.AUDIO_TIME_MILLISECONDS;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, ContractAcrivity {

    @BindView(R.id.btn_start)
    Button btnStart;
    @BindView(R.id.btn_play)
    Button btnPlay;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.main_layout)
    ConstraintLayout layout;

    private RecordAndPlayService recordAndPlayService;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    private static final String TAG = "App";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        recordAndPlayService = RecordAndPlayService.getGetINSTANCE();
        recordAndPlayService.isViewReady(this);

        btnStart.setOnClickListener(this);
        btnPlay.setOnClickListener(this);
        btnPlay.setEnabled(false);
        setDefaultProgressBar();
    }

    public void setDefaultProgressBar() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        progressBar.setProgress(0);
        progressBar.incrementProgressBy(1);
        progressBar.setMax(AUDIO_TIME_MILLISECONDS);
    }

    @Override
    public void setMessage(String message) {
        Snackbar.make(layout, "Can't initialize audiorecorder. " + message, Snackbar.LENGTH_LONG).show();
    }

    public void progressChanged(int i){
        progressBar.setProgress(i);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_start:
                if(checkAndRequestPermissions()){
                    startRecordClick();
                }
                break;
            case R.id.btn_play:
                startPlayClick();
        }
    }

    public void startRecordClick(){
        recordAndPlayService.startRecord();
    }

    public void startPlayClick() {
        recordAndPlayService.startPlaying();
    }

    public void setEnableButton(Boolean b){
        btnStart.setEnabled(b);
        btnPlay.setEnabled(b);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        RecordAndPlayService.getGetINSTANCE().isViewDestoy();
    }

    private  boolean checkAndRequestPermissions() {
        int storage = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int mic = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (storage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (mic != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }
        if (!listPermissionsNeeded.isEmpty())
        {
            ActivityCompat.requestPermissions(this,listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 1
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    startRecordClick();
                } else {
                    if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                        Snackbar.make(layout, "No permission to use audio or file recording", Snackbar.LENGTH_LONG).show();
                    }
                }
                return;
            }
        }
    }
}


