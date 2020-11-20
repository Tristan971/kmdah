package moe.tristan.kmdah.api;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import io.micrometer.core.annotation.Timed;

@Component
public class ImageRequestReferrerValidator extends HttpFilter {

    private static final Pattern MANGADEX_HOST_MATCHER = Pattern.compile(
        // a subdomain followed by a dot (if any), then either mangadex.org, mangadex.network or mdah.tristan.moe
        "^((.+[.])?mangadex(\\.org|\\.network))|(mdah\\.tristan\\.moe)$"
    );

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        validate(request);
        super.doFilter(request, response, chain);
    }

    @Timed
    protected void validate(HttpServletRequest request) {
        String referrer = request.getHeader(HttpHeaders.REFERER);

        if (referrer == null || "".equals(referrer)) {
            return;
        }

        try {
            URI referrerUri = new URI(referrer);
            String host = referrerUri.getHost();
            if (host == null) {
                throw new InvalidReferrerException("Invalid referrer didn't have a host for " + referrer);
            }

            if (!MANGADEX_HOST_MATCHER.matcher(host).find()) {
                throw new InvalidReferrerException("Invalid Referrer header had unexpected host for " + referrer);
            }
        } catch (URISyntaxException e) {
            throw new InvalidReferrerException("Invalid Referrer header was present but not a URI for " + referrer, e);
        }
    }

}
