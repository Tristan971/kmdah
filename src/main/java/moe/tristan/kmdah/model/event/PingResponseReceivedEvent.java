package moe.tristan.kmdah.model.event;

import moe.tristan.kmdah.mangadex.ping.PingResponse;

public record PingResponseReceivedEvent(

    PingResponse pingResponse

) {}
