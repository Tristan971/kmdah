package moe.tristan.kmdah.common.model;

import java.io.InputStream;
import java.util.Optional;

import moe.tristan.kmdah.common.api.CacheMode;

public interface ImageContent {

    Optional<String> getContentType();

    InputStream getInputStream();

    CacheMode getCacheMode();

}
