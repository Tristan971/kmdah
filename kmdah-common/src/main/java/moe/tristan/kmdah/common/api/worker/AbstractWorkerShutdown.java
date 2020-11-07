package moe.tristan.kmdah.common.api.worker;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractWorkerShutdown {

    @Parameter
    public abstract boolean isGracefulShutdownRequired();

}
