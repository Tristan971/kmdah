package moe.tristan.kmdah.common.model.persistence;

import org.immutables.value.Value.Default;
import org.immutables.value.Value.Immutable;

import com.treatwell.immutables.styles.ValueObjectStyle;

import moe.tristan.kmdah.common.api.CacheMode;
import moe.tristan.kmdah.common.model.ImageContent;

@Immutable
@ValueObjectStyle
abstract class AbstractCachedImage implements ImageContent {

    @Override
    @Default
    public CacheMode getCacheMode() {
        return CacheMode.HIT;
    }

}
