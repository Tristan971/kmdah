package moe.tristan.kmdah.service.tls;

import moe.tristan.kmdah.mangadex.ping.TlsData;

public record TlsDataReceivedEvent(
    TlsData tlsData
) {}
