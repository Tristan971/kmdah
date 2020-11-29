package moe.tristan.kmdah.service.kubernetes;

import moe.tristan.kmdah.mangadex.ping.TlsData;

public record TlsDataReceivedEvent(
    TlsData tlsData
) {}
