package moe.tristan.kmdah.common.api.worker;

import java.util.Optional;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractWorkerConfiguration {

    @Parameter
    public abstract Optional<String> getImageServer();

}
