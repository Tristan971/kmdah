FROM docker.mdcloud.moe/library/eclipse-temurin:17

RUN apt update && apt install -y libsodium23

WORKDIR /mangahome
ADD target/kmdah.jar /mangahome/kmdah.jar

STOPSIGNAL 15

CMD [ "java", "-jar", "/mangahome/kmdah.jar" ]
