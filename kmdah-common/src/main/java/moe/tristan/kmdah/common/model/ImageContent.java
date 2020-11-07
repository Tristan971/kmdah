package moe.tristan.kmdah.common.model;

import java.io.InputStream;

import moe.tristan.kmdah.common.api.CacheMode;

public interface ImageContent {

    String getContentType();

    InputStream getInputStream();

    int getSize();

    CacheMode getCacheMode();

}
