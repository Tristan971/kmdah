package moe.tristan.kmdah.common.model;

import java.io.InputStream;

public interface ImageContent {

    String getContentType();

    InputStream getInputStream();

    int getSize();

}
