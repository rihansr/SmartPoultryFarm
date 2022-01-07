package com.rs.smartpoultryfarm.adapter;

import static com.rs.smartpoultryfarm.util.AppExtensions.string;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.rs.smartpoultryfarm.R;
import com.rs.smartpoultryfarm.api.ApiHandler;
import com.rs.smartpoultryfarm.model.Channel;
import com.rs.smartpoultryfarm.model.Feed;
import com.rs.smartpoultryfarm.model.Field;
import com.rs.smartpoultryfarm.util.AppExtensions;
import com.rs.smartpoultryfarm.util.SharedPreference;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressLint("NotifyDataSetChanged")
public class LightAdapter extends RecyclerView.Adapter<LightAdapter.ViewHolder> {

    private final List<Field>       lights = new ArrayList<>();
    private final SharedPreference  sp;
    private Context                 context;

    public LightAdapter() {
        sp = new SharedPreference();
        Feed feed = sp.feedData(SharedPreference.CONTROLLER_FEED_SP_KEY);
        this.lights.add(new Field("field1", string(R.string.lightOne), lightState(feed.getField1())));
        this.lights.add(new Field("field2", string(R.string.lightTwo), lightState(feed.getField2())));
        this.lights.add(new Field("field3", string(R.string.lightThree), lightState(feed.getField3())));
        this.lights.add(new Field("field4", string(R.string.lightFour), lightState(feed.getField4())));
    }

    public void setFeed(Feed feed) {
        feed = feed == null ? new Feed() : feed;
        Feed prevFeed = sp.feedData(SharedPreference.CONTROLLER_FEED_SP_KEY);
        if(Objects.equals(prevFeed.getField1(), feed.getField1())
                && Objects.equals(prevFeed.getField2(), feed.getField2())
                && Objects.equals(prevFeed.getField3(), feed.getField3())
                && Objects.equals(prevFeed.getField4(), feed.getField4())) return;
        this.lights.get(0).setStatus(feed.getField1());
        this.lights.get(1).setStatus(feed.getField2());
        this.lights.get(2).setStatus(feed.getField3());
        this.lights.get(3).setStatus(feed.getField4());
        sp.feedData(SharedPreference.CONTROLLER_FEED_SP_KEY, feed);
        notifyDataSetChanged();
    }

    private String lightState(String state){
        return AppExtensions.formatVal(state, "0");
    }

    boolean isSwitchOn(String status){
        return lightState(status).equals("1");
    }

    private Field light(int pos){
        return lights.get(pos);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.sample_light, parent, false);
        return new ViewHolder(view);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final LottieAnimationView   icon;
        private final AppCompatTextView     label;

        private ViewHolder(View v) {
            super(v);
            icon = v.findViewById(R.id.light_icon);
            label = v.findViewById(R.id.light_tv);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        Field light = light(position);
        holder.label.setText(light.getLabel());
        handleLight(holder.icon, position);
        holder.itemView.setOnClickListener(v -> updateControllersState(holder.icon, position));
    }

    private  void handleLight(LottieAnimationView icon, int pos){
        if(isSwitchOn(this.lights.get(pos).getStatus()))
            icon.playAnimation();
        else icon.setProgress(0);
    }

    private void updateControllersState(LottieAnimationView icon, int pos) {
        Channel channel = sp.channelData(SharedPreference.CONTROLLER_CHANNEL_SP_KEY + "_" + sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY).getChannelId());
        if (channel.getChannelId() == null) return;
        String newStatus = icon.getProgress() == 0 ? "1" : "0";

        Feed feed = new Feed(
                pos == 0 ? newStatus : lights.get(0).getStatus(),
                pos == 1 ? newStatus : lights.get(1).getStatus(),
                pos == 2 ? newStatus : lights.get(2).getStatus(),
                pos == 3 ? newStatus : lights.get(3).getStatus()
        );

        ApiHandler.invoke(context, Feed.class, Request.Method.POST,
                ApiHandler.updateControllerFeedURL(channel.getWriteKey(), feed),
                new ApiHandler.OnDataListener<Feed>() {
                    @Override
                    public void onData(Feed feed) {
                        lights.get(pos).setStatus(newStatus);
                        sp.feedData(SharedPreference.CONTROLLER_FEED_SP_KEY, feed);
                        handleLight(icon, pos);
                    }

                    @Override
                    public void onError() {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return lights.size();
    }
}
