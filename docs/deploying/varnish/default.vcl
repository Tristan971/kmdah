vcl 4.1;

backend kmdah {
    // replace with appropriate values for x-container communication
    .host = "kmdah";
    .port = "8080";
}

sub vcl_recv {
    // we have only the kmdah backend
    set req.backend_hint = kmdah;

    // we never care about cookies
    unset req.http.cookie;

    // don't cache __ paths, which are for healthchecks, prometheus and debugging
    if (req.url ~ "^/__") {
        return(pass);
    }

    // tokens and varnish don't play nicely together, alas, so strip em
    if (req.url ~ "^(/.+){4}") {
        set req.url = regsub(req.url, "^/(.+)/(.+)/(.+)/(.+)", "/\2/\3/\4");
    }
}

sub vcl_backend_response {
    set beresp.do_stream = true;
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
