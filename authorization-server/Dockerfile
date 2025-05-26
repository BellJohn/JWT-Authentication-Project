FROM amazoncorretto:21

WORKDIR /app

COPY build/libs/authorization-server-0.1-all-optimized.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
