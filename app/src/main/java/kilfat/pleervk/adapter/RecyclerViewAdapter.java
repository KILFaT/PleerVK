package kilfat.pleervk.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.vk.sdk.api.model.VKApiAudio;

import java.util.ArrayList;
import java.util.List;

import kilfat.pleervk.R;
import kilfat.pleervk.model.AudioInfo;
import kilfat.pleervk.util.OnLoadMoreListener;


/**
 * Created by insider on 16.02.2016.
 */
public class RecyclerViewAdapter extends RecyclerView.Adapter {
    private final int VIEW_ITEM = 1;
    private final int VIEW_PROG = 0;
    private ArrayList<AudioInfo> audios;
    private int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;
    private boolean loading;
    private OnLoadMoreListener onLoadMoreListener;

    public RecyclerViewAdapter(ArrayList<AudioInfo> audios, RecyclerView recyclerView){
        this.audios = audios;
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView
                    .getLayoutManager();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                        @Override
                        public void onScrolled(RecyclerView recyclerView,
                                               int dx, int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            totalItemCount = linearLayoutManager.getItemCount();
                            lastVisibleItem = linearLayoutManager
                                    .findLastVisibleItemPosition();
                            if (!loading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                                if (onLoadMoreListener != null) {
                                    onLoadMoreListener.onLoadMore();
                                }
                                loading = true;
                            }
                        }
                    });
        }
    }

    @Override
    public int getItemViewType(int position) {
        return audios.get(position) != null ? VIEW_ITEM : VIEW_PROG;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        RecyclerView.ViewHolder vh;
        if (viewType == VIEW_ITEM) {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.recycler_view_item, parent, false);

            vh = new audioViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(
                    R.layout.progressbar_item, parent, false);
            vh = new ProgressViewHolder(v);
        }
        return vh;
    }
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof audioViewHolder) {
            
            AudioInfo singleAudio = audios.get(position);
            ((audioViewHolder) holder).artist.setText(singleAudio.artist);
            ((audioViewHolder) holder).title.setText(singleAudio.title);
            ((audioViewHolder) holder).duration.setText(singleAudio.duration);
        } else {
            ((ProgressViewHolder) holder).progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded() {
        loading = false;
    }

    @Override
    public int getItemCount() {
        return audios.size();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener onLoadMoreListener) {
        this.onLoadMoreListener = onLoadMoreListener;
    }


    //
    public static class audioViewHolder extends RecyclerView.ViewHolder {
        private TextView artist;
        private TextView title;
        private TextView duration;

        public audioViewHolder(View v) {
            super(v);
            artist = (TextView)itemView.findViewById(R.id.artist);
            title = (TextView)itemView.findViewById(R.id.title);
            duration = (TextView)itemView.findViewById(R.id.duration);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //
                }
            });
        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
        }
    }
}