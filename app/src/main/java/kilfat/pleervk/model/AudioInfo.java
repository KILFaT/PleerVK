package kilfat.pleervk.model;

import java.io.Serializable;

/**
 * Created by insider on 21.02.2016.
 */
public class AudioInfo implements Serializable {
    public String duration;
    public String artist;
    public String title;
    public String url;
    public AudioInfo(String duration, String artist, String title, String url){
        this.duration=duration;
        this.artist=artist;
        this.title=title;
        this.url=url;
    }
    public static String getTime(int sec){
        int seconds=sec%60;
        int minutes=(sec/60)%60;
        int hours=(sec/3600)%60;
        if(hours>0){
            return String.format("%d:%02d:%02d",hours,minutes,seconds);
        }else{
            return String.format("%02d:%02d",minutes,seconds);
        }

    }
}
