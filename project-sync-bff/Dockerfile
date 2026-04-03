FROM eclipse-temurin:25-alpine

VOLUME /tmp
EXPOSE 8080

COPY target/*.jar app.jar

ENTRYPOINT ["java","--add-opens=java.base/java.nio.charset=ALL-UNNAMED","--add-opens=java.base/java.lang=ALL-UNNAMED","-jar","/app.jar"]
