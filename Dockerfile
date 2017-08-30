
# Pull base image



FROM  openjdk:8u141-jdk


ENV SCALA_VERSION 2.12.3
ENV SBT_VERSION 0.13.16
ENV JAVA_OPTS="-Xms512M -Xmx3000M -Xss2M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=512m -XX:+UseG1GC -XX:+UseCompressedOops"
ENV SBT_OPTS="-Xms512M -Xmx3000M -Xss2M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=512m -XX:+UseG1GC"
# Scala expects this file
RUN touch /usr/lib/jvm/java-8-openjdk-amd64/release

# Install Scala
## Piping curl directly in tar
RUN \
  curl -fsL https://downloads.typesafe.com/scala/$SCALA_VERSION/scala-$SCALA_VERSION.tgz | tar xfz - -C /root/ && \
  echo >> /root/.bashrc && \
  echo 'export PATH=~/scala-$SCALA_VERSION/bin:$PATH' >> /root/.bashrc

# Install sbt
RUN \
  curl -L -o sbt-$SBT_VERSION.deb https://dl.bintray.com/sbt/debian/sbt-$SBT_VERSION.deb && \
  dpkg -i sbt-$SBT_VERSION.deb && \
  rm sbt-$SBT_VERSION.deb && \
  apt-get update && \
  apt-get install sbt && \
  sbt sbtVersion

# Define working directory

WORKDIR /root

ADD . /root

EXPOSE 80

CMD ./target/universal/stage/bin/highload -Dhttp.port=80 -J-Xms3000M -J-Xmx3000m -J-server


#Inspect your images
#docker images 
#Delete some:
#docker rmi --force 'image id'
#docker build --tag stor.highloadcup.ru/travels/solid_barracuda .
#docker commit -m "comment" highload
#sbt clean stage
#docker push stor.highloadcup.ru/travels/solid_barracuda
#docker run --rm -p 9000:80 -t highload
#env:SBT_OPTS="-Xms512M -Xmx3900M -Xss2M -XX:MaxMetaspaceSize=3048M -XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=512m"
# or on startup script:
# -J-Xms128M -J-Xmx512m -J-server 
# C:\progi\GO\bin
#./highloadcup_tester -addr http://127.0.0.1:9000 -hlcupdocs C:\inetpub\play\Obuchenie\hlcupdocs\data\FULL -test -phase 1
