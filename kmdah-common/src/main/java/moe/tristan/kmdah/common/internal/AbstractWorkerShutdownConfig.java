package moe.tristan.kmdah.common.internal;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractWorkerShutdownConfig {

    @Parameter
    public abstract int getGracefulShutdownRequestedSeconds();

}
