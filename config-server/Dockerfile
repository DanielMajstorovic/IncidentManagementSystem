# faza - Build aplikacije
FROM maven:3.9.3-eclipse-temurin-17 AS builder
WORKDIR /app

# Kopira sve fajlove u image (pom.xml, src/, itd.)
COPY . .

#Build aplikacije bez testova
RUN mvn clean install -DskipTests

#faza - Runtime okruženje
FROM openjdk:17-jdk-slim AS runtime
WORKDIR /app

#Instalacija curl-a
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

#Port kao argument (opciono)
ARG DEFAULT_PORT=8888
ENV PORT=$DEFAULT_PORT

#Izlaganje porta
EXPOSE $PORT

#Kopiranje JAR fajla iz builder faze
COPY --from=builder /app/target/config-server-*.jar app.jar

#Start aplikacije
ENTRYPOINT ["java", "-jar", "app.jar"]