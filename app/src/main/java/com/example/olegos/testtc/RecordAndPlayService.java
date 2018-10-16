package com.example.olegos.testtc;

import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

import static com.example.olegos.testtc.Constants.AUDIO_TIME_MILLISECONDS;
import static com.example.olegos.testtc.Constants.ORIGINAL_FILE_NAME;
import static com.example.olegos.testtc.Constants.RECORDER_AUDIO_ENCODING;
import static com.example.olegos.testtc.Constants.RECORDER_CHANNELS;
import static com.example.olegos.testtc.Constants.RECORDER_SAMPLE_RATE_HIGH;
import static com.example.olegos.testtc.Constants.REVERSE_FILE_NAME;


class RecordAndPlayService {
    private static final String TAG = "App";
    private ByteArrayOutputStream byteBuffer;
    static int myBufferSize = 0;
    private boolean isRecording = false;
    private static int frameSize;

    private AudioRecord audioRecord;
    private ContractAcrivity view;
    private FileHelper fileHelper;
    private MediaPlayer mMediaPlayer;

    private static RecordAndPlayService INSTANCE;

    public static RecordAndPlayService getGetINSTANCE() {
        if (INSTANCE == null){
            synchronized (RecordAndPlayService.class){
                INSTANCE = new RecordAndPlayService();
            }
        }
        return INSTANCE;
    }

    private RecordAndPlayService() {
        fileHelper = new FileHelper();

    }

    public void isViewReady(ContractAcrivity view){
        this.view = view;
    }

    public void startRecord() {
        if (audioRecord == null) {
            createAudioRecord();
        }
        fileHelper.deleteFiles();
        isRecording = true;
        view.setEnableButton(false);

        view.setDefaultProgressBar();
        timer(1, AUDIO_TIME_MILLISECONDS)
                .subscribe(integer -> view.progressChanged(integer), Throwable::printStackTrace, () -> isRecording = false);

        audioData
                .doOnNext(b -> addToBuffer(b))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(bytes -> Log.i(TAG, String.valueOf(bytes.length)), e -> view.setMessage(e.getMessage()), this::recordingStop);
    }

    private void createAudioRecord() {
        int sampleRate = RECORDER_SAMPLE_RATE_HIGH;
        int channelConfig = RECORDER_CHANNELS;
        int audioFormat = RECORDER_AUDIO_ENCODING;

        int minInternalBufferSize = AudioRecord.getMinBufferSize(sampleRate,
                channelConfig, audioFormat);
        frameSize = minInternalBufferSize * 4;
        Log.d(TAG, "minInternalBufferSize = " + minInternalBufferSize
                + ", internalBufferSize = " + frameSize
                + ", myBufferSize = " + myBufferSize);


        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfig, audioFormat, frameSize);

        Log.d(TAG, "init state = " + audioRecord.getRecordingState());
    }

    private void addToBuffer(byte[] b) {
        if (byteBuffer == null) {
            byteBuffer = new ByteArrayOutputStream();
        }
        try {
            byteBuffer.write(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i(TAG, "Buffer size = " + String.valueOf(byteBuffer.size()));
    }


    public void recordingStop() {
        if (byteBuffer == null) {
            return;
        }
        view.setEnableButton(true);
        audioRecord.stop();
        Log.i(TAG, "Recording stop");
        fileHelper.createFile(ORIGINAL_FILE_NAME
                , byteBuffer.toByteArray());
        byteBuffer = null;
    }

    private void prepareReverseAudioData() {
        FileInputStream inputStream = null;
        int headerWaveFileSize = 44;
        try {
            inputStream = new FileInputStream(fileHelper.getFileName(ORIGINAL_FILE_NAME));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            int size = inputStream.available() - headerWaveFileSize;
            byte[] data = new byte[size];
            /*for(int i = 1; i < size; i++){
                inputStream.read(data, size - i, 1);
            }*/
            int framesCount = (size - headerWaveFileSize) / frameSize;
            int readAt = size - frameSize;
            for (int i = 0; i < framesCount; i++) {
                inputStream.read(data, readAt, frameSize);
                readAt = readAt - frameSize;
            }
            addToBuffer(data);

        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //Log.i(TAG, String.valueOf(data.length));
    }

    public void startPlaying() {
        view.setDefaultProgressBar();
        view.setEnableButton(false);
        prepareReverseAudioData();
        fileHelper.createFile(REVERSE_FILE_NAME, byteBuffer.toByteArray());
        byteBuffer = null;
        mMediaPlayer = new MediaPlayer();

        try {
            mMediaPlayer.setDataSource(fileHelper.getFileName(REVERSE_FILE_NAME));
            mMediaPlayer.prepare();
            mMediaPlayer.setOnPreparedListener(mp -> mMediaPlayer.start());
            timer(1, AUDIO_TIME_MILLISECONDS)
                    .subscribe(integer -> view.progressChanged(integer));

        } catch (IOException e) {
            Log.e(TAG, "prepare() failed");
        }
        mMediaPlayer.setOnCompletionListener(mp -> stopPlaying());
    }

    private void stopPlaying(){
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
        view.setEnableButton(true);
    }

    private Observable<byte[]> audioData = Observable.create(imitter -> {
        audioRecord.startRecording();
        byte[] tempBuff = new byte[frameSize];
        while (isRecording) {
            int byteCoubt = audioRecord.read(tempBuff, 0, frameSize);
            if (byteCoubt > 0) {
                imitter.onNext(tempBuff);
            }
        }
        imitter.onComplete();
    });

    private Observable<Integer> timer(int start, int end) {
        return Observable.range(start, end)
                .concatMap(i -> Observable.just(i).delay(1, TimeUnit.MILLISECONDS))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public void isViewDestoy() {
        view = null;
    }
}

