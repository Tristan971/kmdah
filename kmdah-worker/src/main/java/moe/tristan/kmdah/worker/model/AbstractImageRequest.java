package moe.tristan.kmdah.worker.model;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import com.treatwell.immutables.styles.ValueObjectStyle;

import moe.tristan.kmdah.common.mangadex.image.ImageMode;

@Immutable
@ValueObjectStyle
abstract class AbstractImageRequest {

    @Parameter
    public abstract ImageMode getMode();

    @Parameter
    public abstract String getChapter();

    @Parameter
    public abstract String getFile();

}
