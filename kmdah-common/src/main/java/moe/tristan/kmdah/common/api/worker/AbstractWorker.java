package moe.tristan.kmdah.common.api.worker;

import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Immutable;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractWorker {

    public abstract String getUniqueName();

    @Auxiliary
    public abstract long getBandwidthMegabitsPerSecond();

}
