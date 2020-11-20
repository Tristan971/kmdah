package moe.tristan.kmdah.model;

import java.io.File;

import moe.tristan.kmdah.mangadex.image.ImageMode;

public record ImageSpec(
    String filename,
    String chapterHash,
    ImageMode mode
) {

    public String getPath() {
        return String.join(File.separator, chapterHash, mode.getPathFragment(), filename);
    }

}
