FROM docker.io/library/amazoncorretto:19

RUN yum install -y https://dl.fedoraproject.org/pub/epel/epel-release-latest-7.noarch.rpm && \
    yum install -y libsodium shadow-utils && \
    yum clean all && \
    rm -rf /var/cache/yum/*

RUN groupadd -r -g 999 kmdah && \
    useradd -u 999 -r -g 999 kmdah
COPY --chown=root:root target/kmdah.jar /opt/kmdah/kmdah.jar

USER kmdah
WORKDIR /tmp

STOPSIGNAL 15

CMD [ "java", "-jar", "/opt/kmdah/kmdah.jar" ]
