FROM openjdk:10-jre

RUN apt-get install -y fonts-dejavu-extra fonts-dejavu-core
COPY ./*.jar /usr/src/app/app.jar

EXPOSE 4567
RUN mkdir /config

WORKDIR /usr/src/app
VOLUME /config
VOLUME /mnt


CMD java -Dconfig.folder=/config -Xmx128M -jar app.jar

