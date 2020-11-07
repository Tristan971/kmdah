package moe.tristan.kmdah.common.model.persistence;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import com.treatwell.immutables.styles.ValueObjectStyle;

import moe.tristan.kmdah.common.api.CacheMode;
import moe.tristan.kmdah.common.model.ImageContent;

@Immutable
@ValueObjectStyle
abstract class AbstractUpstreamImage implements ImageContent {

    public abstract byte[] getBytes();

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(getBytes());
    }

    @Override
    @Default
    public CacheMode getCacheMode() {
        return CacheMode.MISS;
    }

}
