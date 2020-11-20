package moe.tristan.kmdah.model;

import java.io.File;

import moe.tristan.kmdah.mangadex.image.ImageMode;

public record ImageSpec(
    ImageMode mode,
    String chapter,
    String file
) {

    public String getPath() {
        return String.join(File.separator, chapter, mode.getPathFragment(), file);
    }

}
