package kilfat.pleervk;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKScope;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import kilfat.pleervk.adapter.RecyclerViewAdapter;
import kilfat.pleervk.model.AudioInfo;
import kilfat.pleervk.model.Person;
import kilfat.pleervk.service.PleerService;
import kilfat.pleervk.util.OnLoadMoreListener;


public class MainActivity extends AppCompatActivity {

    private final int NOTIFICATION_ID=100;
    private Person user;
    private String[] scope=new String[]{VKScope.AUDIO};
    private RecyclerView recyclerView;
    private Context context;
    private boolean isInitialized=false;
    private RecyclerTouchListener recyclerTouchListener;
    private android.support.v7.widget.Toolbar toolbar;
    private ArrayList<AudioInfo> audioInfoList;
    private int audioOffset=15; // Cash 15/15.
    private int audioLoaded=0;
    private TextView emptyView;
    private int audioCount=0;
    private RecyclerViewAdapter adapter;
    private Toast toast;
    private boolean isPlay=false;
    Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context=this;
        VKSdk.login(this, scope);
        toolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        audioInfoList=new ArrayList<AudioInfo>();
        recyclerView = (RecyclerView)findViewById(R.id.recyclerView);
        emptyView = (TextView) findViewById(R.id.empty_view);
        AudioClickListener listener=new AudioClickListener();
        intent=new Intent(MainActivity.this, PleerService.class);
        recyclerTouchListener = new RecyclerTouchListener(context, recyclerView,listener);
    }

    private class AudioClickListener implements ClickListener{
        @Override
        public void onClick(View view, int position) {
            toast = Toast.makeText(getApplicationContext(), "Позиция: "+position, Toast.LENGTH_SHORT);
            toast.show();
            // wrong position - just for show
            intent.putExtra("url",audioInfoList.get(position).url);
            startService(intent);
            if(!isPlay)
                setNotification();
            isPlay=true;
        }
    }

    public String getCase(int count){
       return this.getResources().getQuantityString(R.plurals.track_count,count,count);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                VKRequest request=new VKRequest("execute",
                        VKParameters.from("code",context.getString(R.string.code_request_name_count) ));

                request.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        super.onComplete(response);
                        try {
                            if(response!=null) {
                                JSONObject jsonObject = response.json.getJSONObject("response");
                            // KOSTIL BEGIN!
                                String temp1 = jsonObject.getString("first_name");
                                String temp2 = jsonObject.getString("last_name");
                                String name = null;
                                String surname = null;
                                if (temp1 != null && temp1.length() > 4)
                                    name = temp1.substring(2, temp1.length() - 2);
                                if (temp2 != null && temp2.length() > 4)
                                    surname = temp2.substring(2, temp2.length() - 2);
                                temp1 = jsonObject.getString(VKApiConst.USER_ID);
                                String id = jsonObject.getString(VKApiConst.USER_ID);
                                try {
                                    user = new Person(name, surname, Integer.parseInt(temp1.substring(1, temp1.length() - 1))
                                            , jsonObject.getInt("audioCount"));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                              // KOSTIL END!
                                toolbar.setTitle(user.getFirstLastName());
                                toolbar.setSubtitle(getCase(user.getAudioCount()));
                                audioCount=user.getAudioCount();
                                requestAudioList(context, audioLoaded, 2*audioOffset);
                                changeIndex();
                            }else {
                                toolbar.setTitle("Some error!!1");
                                //show message ?
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                            // catch them!
                        }
                    }
                });
            }

            @Override
            public void onError(VKError error) {
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{
        private GestureDetector gestureDetector;
        private ClickListener clickListener;
        public RecyclerTouchListener(Context context,RecyclerView recyclerView,
                                     ClickListener clickListener){
            gestureDetector=new GestureDetector(context, new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }
            });
            this.clickListener=clickListener;
        }
        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            View view = rv.findChildViewUnder(e.getX(),e.getY());
            if(view!=null && clickListener!=null && gestureDetector.onTouchEvent(e)){
                clickListener.onClick(view,rv.getChildLayoutPosition(view));
            }
            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public static interface ClickListener{
        public void onClick(View view,int position);
    }

    public boolean requestAudioList(Context context, int offset, int count){
        String code  =  context.getString(R.string.code_request_audio);
        VKRequest request=new VKRequest("execute",
                VKParameters.from("code",String.format(code, offset,  count )));
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                try {
                    if (response != null) {
                        boolean flagCash=false;
                        int offsetArray=0;
                        if(audioInfoList.size()!=0) {
                            audioInfoList.remove(audioInfoList.size() - 1);
                            flagCash = true;
                            offsetArray=audioInfoList.size()/2-1;
                        }
                        JSONObject jsonObject = response.json.getJSONObject("response");
                        JSONArray jsonAudioList=jsonObject.getJSONObject("audioList").getJSONArray("items");
                        JSONObject tempObject;
                        AudioInfo audioInfo=null;
                        for(int i=0;i<jsonAudioList.length();i++){
                            tempObject=jsonAudioList.getJSONObject(i);
                            audioInfo=new AudioInfo(AudioInfo.getTime(tempObject.getInt("duration")),
                                    tempObject.getString("artist"),tempObject.getString("title"),
                                    tempObject.getString("url"));
                            if(flagCash){
                                offsetArray++;
                                audioInfoList.set(i,audioInfoList.get(offsetArray));
                                audioInfoList.set(offsetArray,audioInfo);
                            }else audioInfoList.add(audioInfo);
                        }

                        if(!isInitialized) showAudioList();
                        else adapter.setLoaded();
                        isInitialized=true;

                        if (audioInfoList.isEmpty()) {
                            recyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);

                        } else {
                            recyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();// notifyItemRangeChanged(0, audioInfoList.size()-1);
                        }
                    } else {
                        //show message ?
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    // catch them!
                }
            }
        });
        return true;
    }

    public void changeIndex(){
        if(audioCount>(audioLoaded-audioOffset)) {
            audioLoaded+=audioOffset;
        }
        else {
            audioLoaded=audioCount;
        }
    }

    public void showAudioList(){
        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.addOnItemTouchListener(recyclerTouchListener);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new RecyclerViewAdapter(audioInfoList, recyclerView);
        recyclerView.setAdapter(adapter);
        adapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                audioInfoList.add(null);
                adapter.notifyItemInserted(audioInfoList.size() - 1);
                requestAudioList(context, audioLoaded, audioOffset);
                changeIndex();
            }
        });
    }

    private void setNotification() {
        RemoteViews remoteViews = new RemoteViews(getPackageName(),
                R.layout.pleer_view);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.ic_ab_app).setContent(
                remoteViews);
        Intent resultIntent = new Intent(this, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(resultIntent);

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent switchIntent = new Intent(this, switchButtonListener.class);
        PendingIntent pendingSwitchIntent = PendingIntent.getBroadcast(this, 0, switchIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.button_pause_resume, pendingSwitchIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public static class switchButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.startService(intent);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(intent);
    }
}
