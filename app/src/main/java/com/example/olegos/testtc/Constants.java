package com.example.olegos.testtc;

import android.media.AudioFormat;

public class Constants {
    public static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    public static final int RECORDER_BPP = 16;
    public static final String AUDIO_RECORDER_FILE_EXT_WAV = ".wav";
    public static final String ORIGINAL_FILE_NAME = "original";
    public static final String REVERSE_FILE_NAME = "reverse";
    public static final int RECORDER_SAMPLE_RATE_HIGH = 44100;
    public static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_MONO;
    public static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    public static final int AUDIO_TIME_MILLISECONDS = 5000;
}
