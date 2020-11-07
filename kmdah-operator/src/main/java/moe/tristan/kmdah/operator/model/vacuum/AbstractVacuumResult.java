package moe.tristan.kmdah.operator.model.vacuum;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractVacuumResult {

    @Parameter
    public abstract int getCount();

    @Parameter
    public abstract int getFreed();

}
