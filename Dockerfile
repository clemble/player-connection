FROM java:8-jre
MAINTAINER antono@clemble.com

EXPOSE 10010

ADD target/player-connection-0.17.0-SNAPSHOT.jar /data/player-connection.jar

CMD java -jar -Dspring.profiles.active=cloud -Dserver.port=10010 /data/player-connection.jar
