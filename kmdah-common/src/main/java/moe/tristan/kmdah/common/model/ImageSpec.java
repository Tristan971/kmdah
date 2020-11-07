package moe.tristan.kmdah.common.model;

import java.io.File;

import moe.tristan.kmdah.common.model.mangadex.image.ImageMode;

public interface ImageSpec {

    String getFilename();

    String getChapterHash();

    ImageMode getMode();

    default String getPath() {
        return String.join(File.separator, getChapterHash(), getMode().getPathFragment(), getFilename());
    }

}
