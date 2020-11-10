package moe.tristan.kmdah.common.model;

import java.io.InputStream;
import java.util.Optional;
import java.util.OptionalLong;

import moe.tristan.kmdah.common.api.CacheMode;

public interface ImageContent {

    InputStream getInputStream();

    Optional<String> getContentType();

    OptionalLong getContentLength();

    CacheMode getCacheMode();

}
