package moe.tristan.kmdah.common.mangadex.image;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.immutables.value.Value.Immutable;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractUpstreamImage implements Image {

    public abstract byte[] getBytes();

    public InputStream getInputStream() {
        return new ByteArrayInputStream(getBytes());
    }

}
