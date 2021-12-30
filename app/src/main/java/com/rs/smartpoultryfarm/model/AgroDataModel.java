package com.rs.smartpoultryfarm.model;

import android.app.Application;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.Request;
import com.rs.smartpoultryfarm.api.ApiHandler;
import com.rs.smartpoultryfarm.util.SharedPreference;

public class AgroDataModel extends AndroidViewModel {

    @Nullable
    private JsonLiveData data;

    public MutableLiveData getRefresh() {
        return refresh;
    }

    private final MutableLiveData<Integer> refresh = new MutableLiveData<>();

    public AgroDataModel(@NonNull Application application) {
        super(application);
        data = new JsonLiveData(application);
    }

    public MutableLiveData<PoultryData> getHealthData() {
        return data;
    }

    public void RefreshData() {
        refresh.setValue(0);
        data = new JsonLiveData(this.getApplication());
    }

    public class JsonLiveData extends MutableLiveData<PoultryData> {
        private final Context context;

        public JsonLiveData(Context context) {
            this.context = context;
            LoadData();
        }

        private void LoadData() {
            SharedPreference sp = new SharedPreference(context);
            Channel channel = sp.channelData(SharedPreference.POULTRY_CHANNEL_SP_KEY);
            ApiHandler.invoke(context, PoultryData.class, Request.Method.GET,
                    ApiHandler.feedsUrl(channel.getChannelId(), channel.getReadKey(), 2),
                    new ApiHandler.OnDataListener<PoultryData>() {
                        @Override
                        public void onData(PoultryData data) {
                            if (data == null) return;
                            if (data.getCode() == 404) return;
                            postValue(data);
                            setValue(data);
                            refresh.postValue(1);
                        }
                        @Override
                        public void onError() {}
                    });
        }
    }
}
