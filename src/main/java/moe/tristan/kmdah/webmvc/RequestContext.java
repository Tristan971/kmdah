package moe.tristan.kmdah.webmvc;

import org.slf4j.MDC;

public final class RequestContext {

    private RequestContext() {
    }

    public static String getId() {
        return MDC.get("ruid");
    }

    public static void setId(String ipAddress) {
        MDC.put("ruid", ipAddress);
    }

    public static void reset() {
        MDC.remove("ruid");
    }

}
