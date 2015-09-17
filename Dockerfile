FROM java:8-jre
MAINTAINER antono@clemble.com

EXPOSE 10010

ADD ./buildoutput/player-connection.jar /data/player-connection.jar

CMD java -jar -Dspring.profiles.active=cloud -Dlogging.config=classpath:logback.cloud.xml -Dserver.port=10010 /data/player-connection.jar
