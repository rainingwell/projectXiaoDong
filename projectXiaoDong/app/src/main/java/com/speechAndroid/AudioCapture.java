package com.speechAndroid;

import android.media.AudioRecord;
import android.util.Log;

public class AudioCapture
{
    private static final int AUDIO_FORMAT = 2;
    private static final int CHANNEL = 16;
    private static int SAMPLE_RATE = 16000;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_RECORDING = 1;
    private static final int STATE_STOP = 3;
    private static final String TAG = "AudioCapture";
    private int AUDIO_SOURCE = 1;
    private AudioRecord mAudioRecord = null;
    private int mBufferSizeInBytes = 8192;
    private int mRecorderState = 3;

    private int getBufferSizeInBytes()
    {
        if (this.mBufferSizeInBytes == 0) {
            this.mBufferSizeInBytes = AudioRecord.getMinBufferSize(SAMPLE_RATE, 16, 2);
        }
        StringBuilder localStringBuilder = new StringBuilder();
        localStringBuilder.append("MinBufferSize");
        localStringBuilder.append(this.mBufferSizeInBytes);
        Log.e("AudioCapture", localStringBuilder.toString());
        return this.mBufferSizeInBytes;
    }

    public int read(byte[] paramArrayOfByte)
    {
        if (this.mRecorderState == 1)
        {
            AudioRecord localAudioRecord = this.mAudioRecord;
            if (localAudioRecord != null) {
                return localAudioRecord.read(paramArrayOfByte, 0, 2048);
            }
        }
        return 0;
    }

    public boolean start()
    {
        Log.v("AudioCapture", "start");
        if (this.mAudioRecord == null)
        {
            this.mRecorderState = 1;
            AudioRecord localAudioRecord = new AudioRecord(this.AUDIO_SOURCE, SAMPLE_RATE, 16, 2, getBufferSizeInBytes());
            this.mAudioRecord = localAudioRecord;
            localAudioRecord.startRecording();
            if (this.mAudioRecord.getRecordingState() != 3)
            {
                Log.e("AudioCapture", "mAudioRecord may be conflict or have some other exception");
                return false;
            }
        }
        return true;
    }

    public boolean stop()
    {
        Object localObject = new StringBuilder();
        ((StringBuilder)localObject).append("stop, mRecorderState: ");
        ((StringBuilder)localObject).append(this.mRecorderState);
        Log.v("AudioCapture", ((StringBuilder)localObject).toString());
        if (this.mRecorderState == 3) {
            return true;
        }
        this.mRecorderState = 3;
        localObject = this.mAudioRecord;
        if (localObject != null)
        {
            ((AudioRecord)localObject).stop();
            this.mAudioRecord.release();
            this.mAudioRecord = null;
        }
        return true;
    }
}

