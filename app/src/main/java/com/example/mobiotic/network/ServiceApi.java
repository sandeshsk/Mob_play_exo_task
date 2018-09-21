package com.example.mobiotic.network;

import com.example.mobiotic.database.MediaDetail;
import com.example.mobiotic.util.AppUtil;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.QueryMap;

public interface ServiceApi {
    @GET(AppUtil.API_ENDPOINT)
    Single<List<MediaDetail>> fetchMediaFromServer(@QueryMap HashMap<String, String> request);


}
