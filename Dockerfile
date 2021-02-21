FROM adoptopenjdk:15

RUN apt update && apt install -y libsodium23

WORKDIR /mangahome
ADD target/kmdah.jar /mangahome/kmdah.jar

STOPSIGNAL 15

CMD [ "java", "-jar", "--enable-preview", "/mangahome/kmdah.jar" ]
