package com.rs.smartpoultryfarm.model;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.GsonBuilder;
import com.rs.smartpoultryfarm.api.API;
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
            API.invoke(context, Request.Method.GET,
                    API.getDataFeedURL(sp.channelData(SharedPreference.CHANNEL_ID_SP_KEY), sp.channelData(SharedPreference.CHANNEL_KEY_SP_KEY), 2),
                    data -> {
                        if (data == null) return;
                        if (data.getCode() == 404) return;
                        postValue(data);
                        setValue(data);
                        refresh.postValue(1);
                    });
        }
    }
}
