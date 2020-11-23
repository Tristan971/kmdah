package moe.tristan.kmdah.model;

import moe.tristan.kmdah.mangadex.image.ImageMode;

public record ImageSpec(

    ImageMode mode,

    String chapter,

    String file

) {}
