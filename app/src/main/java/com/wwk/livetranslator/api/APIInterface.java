package com.wwk.livetranslator.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by wwk on 8/15/17.
 * Copyright © 2017 WYFI, Inc. All rights reserved
 */


public interface APIInterface {

    @GET("translate/enabled")
    Call<JsonObject> enabled();

    @FormUrlEncoded
    @POST("translate/text")
    Call<JsonObject> translateViaService(@Field("q") String query, @Field("sl") String sourceLanguage, @Field("tl") String targetLanguage);

    @FormUrlEncoded
    @POST("https://translate.googleapis.com/translate_a/single?client=gtx&dt=t")
    Call<JsonArray> translateViaGoogle(@Field("q") String query, @Field("sl") String sourceLanguage, @Field("tl") String targetLanguage);

    @GET("https://translate.googleapis.com/translate_tts?ie=UTF-8&total=1&idx=0&client=gtx")
    Call<Response> speech(@Query("q") String query, @Query("tl") String targetLanguage, @Query("textlen") String textLength);

}
