FROM openjdk:12

#RUN apt-get install -y fonts-dejavu-extra fonts-dejavu-core
COPY ./*.jar /usr/src/app/app.jar

EXPOSE 4567
RUN mkdir /config

WORKDIR /usr/src/app
VOLUME /config
VOLUME /mnt
ENV JAVA_OPTS="-Xmx128m"

CMD java -Dconfig.folder=/config  $JAVA_OPTS  -jar app.jar

