package moe.tristan.kmdah.webmvc;

import java.util.UUID;

import org.slf4j.MDC;

public final class RequestContext {

    private RequestContext() {
    }

    public static void setRuid(UUID ruid) {
        MDC.put("ruid", ruid.toString());
        Thread.currentThread().setName(ruid.toString());
    }

    public static UUID getOrCreateRuid() {
        String ruid = MDC.get("ruid");
        return ruid == null
            ? newRuid()
            : UUID.fromString(ruid);
    }

    public static UUID newRuid() {
        UUID ruid = UUID.randomUUID();
        setRuid(ruid);
        return ruid;
    }

    public static void setIp(String ipAddress) {
        MDC.put("reqip", ipAddress);
    }

    public static void reset() {
        MDC.remove("ruid");
        MDC.remove("reqip");
    }

}
