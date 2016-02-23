package kilfat.pleervk.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.os.PowerManager;
import android.widget.Toast;

import java.io.IOException;

public class PleerService extends Service implements MediaPlayer.OnPreparedListener,MediaPlayer.OnErrorListener {
    MediaPlayer mediaPlayer=null;
    WifiManager.WifiLock wifiLock;
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onCreate()
    {
        Toast.makeText(this, "Служба создана",
                Toast.LENGTH_SHORT).show();
    }

    boolean isPlay=false;
    public void playResume(){
        if(isPlay ){
            isPlay=false;
            mediaPlayer.pause();
        }
        else {
            isPlay=true;
            mediaPlayer.start();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
       if(mediaPlayer==null) initMediaPlayer(intent.getStringExtra("url"));
       else {
           if(isPlay) {
               Toast.makeText(this, "ПАУЗА",
                       Toast.LENGTH_SHORT).show();
               mediaPlayer.pause();
               isPlay=false;
           }else {
               isPlay=true;
               mediaPlayer.start();
           }
       }
       return Service.START_STICKY;
       // return super.onStartCommand(intent, flags, startId);
    }

    public void onPrepared(MediaPlayer player) {
        player.start();
    }

    public void initMediaPlayer(String url) {
        if(mediaPlayer==null){
            mediaPlayer = new MediaPlayer();
        }
        try {
            mediaPlayer.setDataSource(url);
        }catch (IOException e){
            e.printStackTrace();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.prepareAsync();
        mediaPlayer.setLooping(false);
        mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnErrorListener(this);
        wifiLock = ((WifiManager) getSystemService(Context.WIFI_SERVICE))
                .createWifiLock(WifiManager.WIFI_MODE_FULL, "mylock");

        wifiLock.acquire();
        Toast.makeText(this, "Служба инициализирована",
                Toast.LENGTH_SHORT).show();
    }
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
     // RESET!
        Toast.makeText(this, "Ошибка службы!",
                Toast.LENGTH_SHORT).show();
        if(mediaPlayer!=null) {
            mediaPlayer.reset();
        }
        return false;
    }
    @Override
    public void onDestroy()
    {
        Toast.makeText(this, "Служба остановлена",
                Toast.LENGTH_SHORT).show();
        if(wifiLock!=null)
            wifiLock.release();
        if(mediaPlayer!=null) {
            mediaPlayer.release();
        }
    }
}