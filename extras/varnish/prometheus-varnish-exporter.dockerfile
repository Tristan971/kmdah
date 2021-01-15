FROM library/varnish:6 as build

WORKDIR /tmp/prometheus-varnish-exporter
ADD \
    https://github.com/jonnenauha/prometheus_varnish_exporter/releases/download/1.6/prometheus_varnish_exporter-1.6.linux-amd64.tar.gz \
    varnish-prometheus-exporter.tar.gz
RUN tar \
    --strip-components 1 \
    --extract \
    --file varnish-prometheus-exporter.tar.gz

FROM library/varnish:6

COPY --from=build /tmp/prometheus-varnish-exporter/prometheus_varnish_exporter /bin/prometheus_varnish_exporter

ENTRYPOINT "/bin/prometheus_varnish_exporter"
