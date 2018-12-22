package h264.screen_tcp_h264.media;

import android.media.MediaCodec;

/**
 * Created by user111 on 2018/3/7.
 */

public abstract class MediaCodecBase {

    protected MediaCodec mEncoder;

    protected boolean isRun = false;

    public abstract void prepare();

    public abstract void release();


}
