package h264.screen_tcp_h264.screen;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.projection.MediaProjection;
import android.view.Surface;

import h264.screen_tcp_h264.media.VideoMediaCodec;


public class ScreenRecord extends Thread {

    private final static String TAG = "ScreenRecord";

    private Surface mSurface;
    private Context mContext;
    private VirtualDisplay mVirtualDisplay;
    private MediaProjection mMediaProjection;

    private VideoMediaCodec mVideoMediaCodec;

    public ScreenRecord(Context context, MediaProjection mp){
        this.mContext = context;
        this.mMediaProjection = mp;
        mVideoMediaCodec = new VideoMediaCodec();
    }

    @Override
    public void run() {
        mVideoMediaCodec.prepare();
        mSurface = mVideoMediaCodec.getSurface();
        mVirtualDisplay =mMediaProjection.createVirtualDisplay(TAG + "-display", Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT, Constant.VIDEO_DPI, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mSurface, null, null);
        mVideoMediaCodec.isRun(true);
        mVideoMediaCodec.getBuffer();
    }

    /**
     * 停止
     * **/
    public void release(){
        mVideoMediaCodec.release();
    }




}
