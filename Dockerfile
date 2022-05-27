FROM docker.io/library/eclipse-temurin:18

RUN apt -qq update && \
    apt install -y --no-install-recommends libsodium23 && \
    apt autoremove -y && \
    apt -qq -y clean && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/* /var/cache/* /var/log/*

RUN groupadd -r -g 999 kmdah && \
    useradd -u 999 -r -g 999 kmdah
COPY --chown=root:root target/kmdah.jar /opt/kmdah/kmdah.jar

USER kmdah
WORKDIR /tmp

STOPSIGNAL 15

CMD [ "java", "-jar", "/opt/kmdah/kmdah.jar" ]
