package com.ftpix.nowplaying.transformers;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ResponseTransformer;

public class GsonTransformer implements ResponseTransformer {
    public static final Gson GSON = new GsonBuilder().enableComplexMapKeySerialization().create();


    @Override
    public String render(Object o) throws Exception {
        return GSON.toJson(o);
    }
}
