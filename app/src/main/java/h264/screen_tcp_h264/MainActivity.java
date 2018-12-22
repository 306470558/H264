package h264.screen_tcp_h264;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

import h264.screen_tcp_h264.application.SysValue;
import h264.screen_tcp_h264.media.h264data;
import h264.screen_tcp_h264.screen.ScreenRecord;

public class MainActivity extends AppCompatActivity  implements View.OnClickListener{
    public static final int REQUEST_CODE_A = 10001;
    private final int PERMISSION_CODE = 0x12;
    private Button start_record,stop_record;
    private static final char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5',
            '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    public static String TAVG="H264";
    private TextView line2;

    private MediaProjectionManager mMediaProjectionManager;

    private ScreenRecord mScreenRecord;

    private boolean isRecording = false;

    private static int queuesize = 30;
    public static ArrayBlockingQueue<h264data> h264Queue = new ArrayBlockingQueue<>(queuesize);

    private String RtspAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InitView();
        InitMPManager();
        RtspAddress = displayIpAddress();
        if(RtspAddress != null){
            line2.setText(RtspAddress);
        }
        if (SysValue.api >= Build.VERSION_CODES.M) {
            getAppPermission();
        } else if (SysValue.api >= 21) {
            // getMeidProjection();
        } else {
            //todo 需要root权限或系统签名
            //  ScreenApplication.getInstance().setDisplayManager(((DisplayManager) getSystemService(Context.DISPLAY_SERVICE)));
        }
    }

    private void getAppPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                PERMISSION_CODE);
    }

    public static void putData(byte[] buffer, int type,long ts) {
        if (h264Queue.size() >= queuesize) {
            h264Queue.poll();
        }
        h264data data = new h264data();
        data.data = buffer;
        data.type = type;
        data.ts = ts;
        h264Queue.add(data);
        Log.d(TAVG,String.valueOf(buffer.length)+"_"+String.valueOf(type)+"_"+bytesToHexFun2(buffer));

    }
    /**
     *
     * **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        try {
            MediaProjection mediaProjection = mMediaProjectionManager.getMediaProjection(resultCode, data);
            if(mediaProjection == null){
                Toast.makeText(this,"程序发生错误:MediaProjection@1",Toast.LENGTH_SHORT).show();
                return;
            }
            mScreenRecord = new ScreenRecord(this,mediaProjection);
            mScreenRecord.start();
        }
        catch (Exception e){

        }
    }
    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.start_record:
                StartScreenCapture();
                break;
            case R.id.stop_record:
                StopScreenCapture();
                break;
        }
    }

    /**
     * 初始化View
     * **/
    private void InitView(){
        start_record = findViewById(R.id.start_record);
        start_record.setOnClickListener(this);
        stop_record = findViewById(R.id.stop_record);
        stop_record.setOnClickListener(this);
        line2 = (TextView)findViewById(R.id.line2);
    }

    /**
     * 初始化MediaProjectionManager
     * **/
    private void InitMPManager(){
        mMediaProjectionManager = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
    }


    /**
     * 开始截屏
     * **/
    private void StartScreenCapture(){
        if(RtspAddress != null && !RtspAddress.isEmpty()){
            isRecording = true;
            Intent captureIntent = mMediaProjectionManager.createScreenCaptureIntent();
            startActivityForResult(captureIntent, REQUEST_CODE_A);
          //  bindService(new Intent(this,RtspServer.class), mRtspServiceConnection, Context.BIND_AUTO_CREATE);
        }else{
            Toast.makeText(this,"网络连接异常！",Toast.LENGTH_SHORT).show();
        }
    }


    /**
     * 停止截屏
     * **/
    private void StopScreenCapture(){
        isRecording = false;
        mScreenRecord.release();
       // if (mRtspServer != null)
         //   mRtspServer.removeCallbackListener(mRtspCallbackListener);
          //   unbindService(mRtspServiceConnection);
    }
    /**
     * 先判断网络情况是否良好
     * */
    private String displayIpAddress() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String ipaddress = "";
        if (info!=null && info.getNetworkId()>-1) {
            int i = info.getIpAddress();
            String ip = String.format(Locale.ENGLISH,"%d.%d.%d.%d", i & 0xff, i >> 8 & 0xff,i >> 16 & 0xff,i >> 24 & 0xff);
            ipaddress += "rtsp://";
            ipaddress += ip;
            ipaddress += ":";
          //  ipaddress += RtspServer.DEFAULT_RTSP_PORT;
        }
        return ipaddress;
    }
    /*/*   * 方法二：
     * byte[] to hex string
     *
     * @param bytes
     * @return
     */
    public static String bytesToHexFun2(byte[] bytes) {
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for(byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }

        return new String(buf);
    }
}
