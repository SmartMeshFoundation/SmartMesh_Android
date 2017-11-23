package com.lingtuan.firefly.chat.audio;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.Button;

public class RecordAudioView extends Button {

    private static final String TAG = "RecordAudioView";

    private Context context;
    private IRecordAudioListener recordAudioListener;
    private boolean isCanceled;
    private float downPointY;
    private static final float DEFAULT_SLIDE_HEIGHT_CANCEL = 150;
    private boolean isRecording;
    private MotionEvent event;
    private boolean touchOk;
    public RecordAudioView(Context context) {
        super(context);
        initView(context);
    }

    public RecordAudioView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public RecordAudioView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context){
        this.context = context;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        super.onTouchEvent(event);
        this.event = event;
        if(recordAudioListener != null){
            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN:
                    touchOk = true;
                    downPointY = event.getY();
                    recordAudioListener.onFingerPress();
                    startRecordAudio();
                    break;
                case MotionEvent.ACTION_UP:
                    onFingerUp();
                    touchOk = false;
                    return  false;
                case MotionEvent.ACTION_MOVE:
                    if(touchOk)
                    {
                        onFingerMove(event);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    isCanceled = true;
                    touchOk = false;
                    onFingerUp();
                    return  false;
                default:
                    break;
            }
        }
        return true;
    }

    /**
     * Finger is raised, can be cancelled to record can also be successful
     */
    private void onFingerUp(){
        if(isRecording){
            if(isCanceled){
                isRecording = false;
                recordAudioListener.onRecordCancel();
            }else{
                stopRecordAudio();
            }
        }
    }

    private void onFingerMove(MotionEvent event){
        float currentY = event.getY();
        isCanceled = checkCancel(currentY);
        if(isCanceled){
            recordAudioListener.onSlideTop();
        }else{
            recordAudioListener.onFingerSlid();
        }
    }

    private boolean checkCancel(float currentY){
        return downPointY - currentY >= DEFAULT_SLIDE_HEIGHT_CANCEL;
    }

    /**
     * Check if ready to record, if the start of the recording has been ready
     */
    private void startRecordAudio() throws RuntimeException {
        boolean isPrepare = recordAudioListener.onRecordPrepare();
        if(isPrepare){
            recordAudioListener.onRecordStart();
            isRecording = true;
        }
    }

    /**
     * Stop the recording
     */
    private void stopRecordAudio() throws RuntimeException {
        if(isRecording){
            isRecording = false;
            this.recordAudioListener.onRecordStop();
        }
    }

    /**
     * Need to set up IRecordAudioStatus, listening to start recording end operations, and to deal with authority
     * @param recordAudioListener
     */
    public void setRecordAudioListener(IRecordAudioListener recordAudioListener) {
        this.recordAudioListener = recordAudioListener;
    }

    public void invokeStop(){
        if(event !=null)
        {
            event.setAction(MotionEvent.ACTION_UP);
            onTouchEvent(event);
        }
    }

    public interface IRecordAudioListener {
        boolean onRecordPrepare();
        String onRecordStart();
        boolean onRecordStop();
        boolean onRecordCancel();
        void onSlideTop();
        void onFingerPress();
        void onFingerSlid();
    }
}
