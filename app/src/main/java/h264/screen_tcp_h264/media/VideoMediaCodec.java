package h264.screen_tcp_h264.media;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;




import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import h264.screen_tcp_h264.MainActivity;
import h264.screen_tcp_h264.screen.Constant;

/**
 * Created by zpf on 2018/3/7.
 */

public class VideoMediaCodec extends MediaCodecBase {

    private final static String TAG = "VideoMediaCodec";


    private Surface mSurface;
    private long startTime = 0;
    private int TIMEOUT_USEC = 12000;
    public byte[] configbyte;

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/lupingxx.h264";
    private BufferedOutputStream outputStream;
    FileOutputStream outStream;
    private void createfile(){
        File file = new File(path);
        if(file.exists()){
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     *
     * **/
    public VideoMediaCodec(){
        Log.d("path",path);
                createfile(); ////////////////保存到文件
        prepare();
    }

    public Surface getSurface(){
        return mSurface;
    }

    public void isRun(boolean isR){
        this.isRun = isR;
    }


    @Override
    public void prepare(){
        try{
            MediaFormat format = MediaFormat.createVideoFormat(Constant.MIME_TYPE, Constant.VIDEO_WIDTH, Constant.VIDEO_HEIGHT);
            format.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
            format.setInteger(MediaFormat.KEY_BIT_RATE, Constant.VIDEO_BITRATE);
            format.setInteger(MediaFormat.KEY_FRAME_RATE, Constant.VIDEO_FRAMERATE);
            format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, Constant.VIDEO_IFRAME_INTER);
            mEncoder = MediaCodec.createEncoderByType(Constant.MIME_TYPE);
            mEncoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mSurface = mEncoder.createInputSurface();
            mEncoder.start();
        }catch (IOException e){

        }
    }

    @Override
    public void release() {
        this.isRun = false;

    }


    /**
     * 获取h264数据
     * **/
    public void getBuffer(){
        try
        {
            while(isRun){
                if(mEncoder == null)
                    break;

                MediaCodec.BufferInfo mBufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex  = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                while (outputBufferIndex >= 0){
                    ByteBuffer outputBuffer = mEncoder.getOutputBuffers()[outputBufferIndex];
                    byte[] outData = new byte[mBufferInfo.size];
                    outputBuffer.get(outData);
                    if(mBufferInfo.flags == 2){
                        configbyte = new byte[mBufferInfo.size];
                        configbyte = outData;
                    }
//                    else{
//                        MainActivity.putData(outData,2,mBufferInfo.presentationTimeUs*1000L);
//                    }

                    else if(mBufferInfo.flags == 1){
                        byte[] keyframe = new byte[mBufferInfo.size + configbyte.length];
                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
                       MainActivity.putData(keyframe,1,mBufferInfo.presentationTimeUs*1000L);
                         if(outputStream != null){ ////////////////保存到文件
                            outputStream.write(keyframe, 0, keyframe.length);////////////////保存到文件
                        }////////////////保存到文件
                    }else{
                         MainActivity.putData(outData,2,mBufferInfo.presentationTimeUs*1000L);
                         if(outputStream != null){ ////////////////保存到文件
                             outputStream.write(outData, 0, outData.length);////////////////保存到文件
                        }////////////////保存到文件
                    }
                    mEncoder.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mEncoder.dequeueOutputBuffer(mBufferInfo, TIMEOUT_USEC);
                }
            }
        }
        catch (Exception e){

        }
        try {
            mEncoder.stop();
            mEncoder.release();
            mEncoder = null;
        } catch (Exception e){
            e.printStackTrace();
        }
        ////////////////保存到文件
         try {
            outputStream.flush();
            outputStream.close();
            outputStream = null;
         } catch (IOException e) {
             e.printStackTrace();
         }
        ////////////////保存到文件
    }
}
