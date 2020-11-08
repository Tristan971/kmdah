package moe.tristan.kmdah.operator.service.mangadex;

import org.immutables.value.Value.Immutable;
import org.immutables.value.Value.Parameter;

import com.treatwell.immutables.styles.EventStyle;

import moe.tristan.kmdah.common.model.mangadex.ping.PingResponse;

@Immutable
@EventStyle
abstract class PingResponseReceived {

    @Parameter
    public abstract PingResponse getPingResponse();

}
