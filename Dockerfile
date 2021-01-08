FROM fedora:33 as loom-prepare

WORKDIR /tmp/loom
ADD "https://download.java.net/java/early_access/loom/9/openjdk-16-loom+9-316_linux-x64_bin.tar.gz" /tmp/loom/loom.tar.gz
RUN tar xf loom.tar.gz

FROM fedora:33
RUN dnf install -y dumb-init

COPY --from=loom-prepare /tmp/loom/jdk-16 /jdk
ENV JAVA_HOME "/jdk"
ENV PATH "/jdk/bin:$PATH"

WORKDIR /mangahome
ADD target/kmdah.jar /mangahome/kmdah.jar

ENV JAVA_TOOL_OPTIONS "-Xms64M -Xmx384M"

ENTRYPOINT [ "dumb-init", "--rewrite", "2:15", "--" ]
CMD [ "java", "-jar", "--enable-preview", "/mangahome/kmdah.jar" ]
