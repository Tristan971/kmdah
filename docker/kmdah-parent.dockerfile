FROM adoptopenjdk:15-jre-hotspot

ADD https://github.com/Yelp/dumb-init/releases/download/v1.2.2/dumb-init_1.2.2_amd64 /bin/dumb-init
RUN chmod +x /bin/dumb-init

ENV XMS "128M"
ENV XMX "256M"
ENV JAVA_TOOL_OPTIONS "-XX:+UseShenandoahGC -Xms${XMS} -Xmx${XMX}"

ARG KMDAH_VERSION
ENV KMDAH_VERSION "$KMDAH_VERSION"

WORKDIR /mangahome

ADD docker/entrypoint.sh /mangahome/entrypoint.sh
ENV KMDAH_CONFIG_DIR "/mangahome/"

ENTRYPOINT ["/bin/dumb-init", "--" ]
CMD ["/mangahome/entrypoint.sh"]
