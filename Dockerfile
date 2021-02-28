##FROM tomcat:9-jre8-alpine
#FROM tomcat:9.0.30
#VOLUME /tmp
#COPY target/civilworkoffers.war /usr/local/tomcat/webapps/civilworkoffers.war
#EXPOSE 8086
#ENTRYPOINT [ "sh", "-c", "java -Dspring.profiles.active=docker -Djava.security.egd=file:/dev/./urandom -jar /usr/local/tomcat/webapps/civilworkoffers.war" ]

FROM openjdk:8-jdk-alpine
ADD target/civilworkoffers.war .
EXPOSE 8085
CMD java -jar civilworkoffers.war --spring.datasource.url=jdbc:mysql://mysql-standalone:3306/civilworkoffers --spring.jpa.hibernate.ddl-auto=update