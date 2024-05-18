FROM amazoncorretto:21-alpine3.18

RUN apk add --no-cache aws-cli

VOLUME /tmp
EXPOSE 8082

COPY target/*.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]
