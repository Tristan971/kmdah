package moe.tristan.kmdah.common.mangadex.image;

import java.io.InputStream;

public interface Image {

    String getContentType();

    InputStream getInputStream();

    int getSize();

}
