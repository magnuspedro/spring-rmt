FROM amazoncorretto:21-alpine3.18

RUN apk add --no-cache aws-cli

VOLUME /tmp
EXPOSE 8080

COPY target/*.jar app.jar

ENTRYPOINT ["java","--add-opens=java.base/java.nio.charset=ALL-UNNAMED","--add-opens=java.base/java.lang=ALL-UNNAMED","-jar","/app.jar"]
