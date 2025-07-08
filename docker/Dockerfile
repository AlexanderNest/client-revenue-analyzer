FROM openjdk:17-jdk-slim
WORKDIR /app
COPY ../web/target/web-0.0.1-SNAPSHOT.jar /app/web.jar
CMD ["java", \
     "-Dcom.sun.management.jmxremote", \
     "-Dcom.sun.management.jmxremote.port=9010", \
     "-Dcom.sun.management.jmxremote.authenticate=false", \
     "-Dcom.sun.management.jmxremote.ssl=false", \
     "-jar", "web.jar"]
EXPOSE 8080