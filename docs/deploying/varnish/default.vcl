vcl 4.0;

backend kmdah {
    .host = "host.docker.internal";
    .port = "8080";
}

sub vcl_recv {
    // we have only the kmdah backend
    set req.backend_hint = default;

    // we never care about cookies
    unset req.http.cookie;

    // proxy client ip to backend for geoip when needed
    set req.http.X-Forwarded-For = client.ip;

    // don't cache __ paths, which are for healthchecks, prometheus and debugging
    if (req.url ~ "^/__") {
        return(pass);
    }

    // tokens and varnish don't play nicely together, alas, so strip em
    if (req.url ~ "^(/.+){4}") {
        set req.url = regsub(req.url, "^/(.+)/(.+)/(.+)/(.+)", "/\2/\3/\4");
    }
}

sub vcl_deliver {
    set resp.http.X-Age = resp.http.Age;
    unset resp.http.Age;

    // add a distinction between backend HIT and Varnish HIT
    if (obj.hits > 0) {
        set resp.http.X-Cache-Mode = "HIT (Varnish)";
        set resp.http.X-Varnish-Cache-Hits = obj.hits;
    }
}
