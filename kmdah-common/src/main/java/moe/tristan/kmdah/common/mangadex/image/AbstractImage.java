package moe.tristan.kmdah.common.mangadex.image;

import org.immutables.value.Value.Immutable;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractImage {

    public abstract String getContentType();

    public abstract byte[] getBytes();

}
