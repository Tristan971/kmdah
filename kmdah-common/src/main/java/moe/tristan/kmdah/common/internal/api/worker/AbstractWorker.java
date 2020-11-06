package moe.tristan.kmdah.common.internal.api.worker;

import java.util.UUID;

import org.immutables.value.Value.Auxiliary;
import org.immutables.value.Value.Immutable;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractWorker {

    public abstract UUID getUuid();

    @Auxiliary
    public abstract String getHttpUrl();

    @Auxiliary
    public abstract int getBandwidthMbps();

}
