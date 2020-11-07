FROM tristandeloche/kmdah-parent:0.0.1-SNAPSHOT

ADD kmdah-operator/target/kmdah-operator.jar /mangahome/kmdah-operator.jar

ENV KMDAH_JARFILE /mangahome/kmdah-operator.jar
