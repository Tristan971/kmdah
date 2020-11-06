package moe.tristan.kmdah.common.internal.api.worker;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractWorkerConfiguration {

    @Parameter
    public abstract String getImageServer();

}
