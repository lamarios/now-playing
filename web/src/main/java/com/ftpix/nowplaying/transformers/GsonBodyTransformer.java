package com.ftpix.nowplaying.transformers;

import com.ftpix.sparknnotation.interfaces.BodyTransformer;

public class GsonBodyTransformer implements BodyTransformer {
    @Override
    public <T> T transform(String s, Class<T> aClass) {
        return GsonTransformer.GSON.fromJson(s, aClass);
    }
}
