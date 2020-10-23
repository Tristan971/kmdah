package moe.tristan.kmdah.common.mangadex.stop;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import com.treatwell.immutables.styles.ValueObjectStyle;

@Immutable
@ValueObjectStyle
abstract class AbstractStopRequest {

    /**
     * @return the client secret
     */
    @Parameter
    public abstract String getSecret();

}
