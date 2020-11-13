FROM adoptopenjdk:15-jre-hotspot

RUN apt update && apt install -y dumb-init

ARG KMDAH_VERSION
ENV KMDAH_VERSION "$KMDAH_VERSION"

WORKDIR /mangahome

ADD docker/entrypoint.sh /mangahome/entrypoint.sh
ENV KMDAH_CONFIG_DIR "/mangahome/"

ENTRYPOINT ["/bin/dumb-init", "--" ]
CMD ["/mangahome/entrypoint.sh"]
