package moe.tristan.kmdah.common.api.worker;

import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Derived;
import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;
import org.springframework.util.unit.DataSize;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractWorker {

    @Parameter
    public abstract String getUniqueName();

    @Parameter
    @Auxiliary
    public abstract long getBandwidthMbps();

    @Derived
    public DataSize getBandwidth() {
        return DataSize.ofMegabytes(getBandwidthMbps());
    }

}
