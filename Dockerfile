FROM adoptopenjdk:15

WORKDIR /mangahome
ADD target/kmdah.jar /mangahome/kmdah.jar

STOPSIGNAL 15

CMD [ "java", "-jar", "--enable-preview", "/mangahome/kmdah.jar" ]
