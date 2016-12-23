package com.uhome.haier.audiobarrage;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.opendanmaku.DanmakuItem;
import com.opendanmaku.DanmakuView;
import com.opendanmaku.IDanmakuItem;
import com.uhome.haier.audiobarrage.utils.Constants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import bf.cloud.android.playutils.VodPlayer;

public class MainActivity extends AppCompatActivity implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener,
        MediaPlayer.OnBufferingUpdateListener, View.OnClickListener {

    private SurfaceView mSurfaceview;
    private EditText metText;
    private Button mSend;
    private Button mHint;
    private SurfaceHolder surfaceholder;
    private MediaPlayer mediaplayer;
    private Uri uri;
    private DanmakuView mDanmakuView;

    private String getUriString = "http://192.168.2.75:8080/test.mp4";
    private WebView mVideo;
    private LinearLayout mLayoyt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        mSurfaceview = (SurfaceView) findViewById(R.id.sfv_surfaceView);

        metText = (EditText) findViewById(R.id.et_text);
        mSend = (Button) findViewById(R.id.send);
        mHint = (Button) findViewById(R.id.hint);
        mVideo = (WebView) findViewById(R.id.wv_video);
        mLayoyt = (LinearLayout) findViewById(R.id.ll_layout);
        mDanmakuView = (DanmakuView) findViewById(R.id.danmakuView);
        //创建弹幕条目的list集合
        List<IDanmakuItem> list = initItems();
        Collections.shuffle(list);

        //尝试使用webview播放视频
        WebSettings setting = mVideo.getSettings();
        setSettings(setting);
        mVideo.setWebChromeClient(new WebChromeClient());
        mVideo.setWebViewClient(new WebViewClient());
        mVideo.loadUrl("http://baidu.com");

        //弹幕的显示
        mDanmakuView.addItem(list, true);
        mHint.setOnClickListener(this);
        mSend.setOnClickListener(this);

        // 设置surfaceHolder
        surfaceholder = mSurfaceview.getHolder();
        // 设置Holder类型,该类型表示surfaceView自己不管理缓存区,虽然提示过时，但最好还是要设置
        surfaceholder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 设置surface回调
        surfaceholder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {

                //被创建

                PlayVideo();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //改变

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //销毁
                if(mediaplayer != null){
                    mediaplayer.release();
                    mediaplayer=null;
                    PlayVideo();
                }
            }
        });
    }

    private void setSettings(WebSettings setting) {
        setting.setJavaScriptEnabled(true);
        setting.setBuiltInZoomControls(true);
        setting.setDisplayZoomControls(false);
        setting.setSupportZoom(true);

        setting.setDomStorageEnabled(true);
        setting.setDatabaseEnabled(true);
        // 全屏显示
        setting.setLoadWithOverviewMode(true);
        setting.setUseWideViewPort(true);
    }

    /**初始化弹幕条目的方法*/
    private List<IDanmakuItem> initItems() {
        List<IDanmakuItem> list = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            IDanmakuItem item = new DanmakuItem(this, i + " : plain text danmuku", mDanmakuView.getWidth());
            list.add(item);
        }

        String msg = " : text with image   ";
        for (int i = 0; i < 100; i++) {
            ImageSpan imageSpan = new ImageSpan(this, R.drawable.em);
            SpannableString spannableString = new SpannableString(i + msg);
            spannableString.setSpan(imageSpan, spannableString.length() - 2, spannableString.length() - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            IDanmakuItem item = new DanmakuItem(this, spannableString, mDanmakuView.getWidth(), 0, 0, 0, 1.5f);
            list.add(item);
        }
        return list;
    }
    /**播放视频*/
    private void PlayVideo() {
        try {
        // 初始化MediaPlayer
        mediaplayer = new MediaPlayer();
        // 重置mediaPaly,建议在初始滑mediaplay立即调用。
        mediaplayer.reset();
        // 设置声音效果
        mediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // 设置播放完成监听
        mediaplayer.setOnCompletionListener(this);
        // 设置媒体加载完成以后回调函数。
        mediaplayer.setOnPreparedListener(this);
        // 错误监听回调函数
        mediaplayer.setOnErrorListener(this);
        // 设置缓存变化监听
        mediaplayer.setOnBufferingUpdateListener(this);
        uri = Uri.parse(getUriString);
        //设置数据来源方式
            mediaplayer.setDataSource(this, uri);
            mediaplayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "加载视频错误！", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onCompletion(MediaPlayer mp) {

    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        // 判断是否有保存的播放位置,防止屏幕旋转时，界面被重新构建，播放位置丢失。
       if(Constants.playPosition >= 0){
           mediaplayer.seekTo(Constants.playPosition);
           Constants.playPosition = -1;
       }
        mediaplayer.start();
        mediaplayer.setDisplay(surfaceholder);
        // 设置surfaceView保持在屏幕上
        mediaplayer.setScreenOnWhilePlaying(true);
        surfaceholder.setKeepScreenOn(true);
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        switch (what){
            case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                Log.e("text","发生未知错误");

                break;
            case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                Log.e("text","媒体服务器死机");
                break;
            default:
                Log.e("text","onError+"+what);
                break;
        }
        switch (extra){
            case MediaPlayer.MEDIA_ERROR_IO:
                //io读写错误
                Log.e("text","文件或网络相关的IO操作错误");
                break;
            case MediaPlayer.MEDIA_ERROR_MALFORMED:
                //文件格式不支持
                Log.e("text","比特流编码标准或文件不符合相关规范");
                break;
            case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                //一些操作需要太长时间来完成,通常超过3 - 5秒。
                Log.e("text","操作超时");
                break;
            case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                //比特流编码标准或文件符合相关规范,但媒体框架不支持该功能
                Log.e("text","比特流编码标准或文件符合相关规范,但媒体框架不支持该功能");
                break;
            default:
                Log.e("text","onError+"+extra);
                break;
        }
        //如果未指定回调函数， 或回调函数返回假，VideoView 会通知用户发生了错误。
        return false;

    }

    @Override
    public void onBufferingUpdate(MediaPlayer mp, int percent) {
        // percent 表示缓存加载进度，0为没开始，100表示加载完成，在加载完成以后也会一直调用该方法
        Log.e("text", "onBufferingUpdate-->" + percent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDanmakuView.show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDanmakuView.hide();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDanmakuView.clear();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //恢复显示
            case R.id.hint:
                if (mDanmakuView.isPaused()) {
                    mHint.setText(R.string.hide);
                    mDanmakuView.show();
                } else {
                    mHint.setText(R.string.show);
                    mDanmakuView.hide();
                }
                break;
            //发送
            case R.id.send:
                String input = metText.getText().toString();
                if (TextUtils.isEmpty(input)) {
                    Toast.makeText(MainActivity.this, R.string.empty_prompt, Toast.LENGTH_SHORT).show();
                } else {

                    IDanmakuItem item = new DanmakuItem(this, new SpannableString(input),
                    mDanmakuView.getWidth(),0,R.color.my_item_color,0,1);
                    mDanmakuView.addItemToHead(item);
                }
                metText.setText("");
                break;
        }
    }
}
