package moe.tristan.kmdah.common.mangadex.image;

import static com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING;

import java.time.ZonedDateTime;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.treatwell.immutables.styles.ValueObjectStyle;

import moe.tristan.kmdah.common.mangadex.MangadexApi;

@Immutable
@ValueObjectStyle
abstract class AbstractImageToken {

    @Parameter
    @JsonFormat(shape = STRING, pattern = MangadexApi.TIMESTAMP_FORMAT)
    public abstract ZonedDateTime getExpires();

    @Parameter
    public abstract String getHash();

}
