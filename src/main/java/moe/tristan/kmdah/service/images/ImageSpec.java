package moe.tristan.kmdah.service.images;

import moe.tristan.kmdah.mangadex.image.ImageMode;

public record ImageSpec(

    ImageMode mode,

    String chapter,

    String file

) {}
