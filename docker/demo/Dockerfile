FROM openjdk:17-ea-17-jdk-oracle
WORKDIR /app
ENV TZ=Asia/Shanghai
EXPOSE 81
COPY target/*.jar /app/app.jar
CMD ["java", "-jar", "/app/app.jar", "--spring.profiles.active=prod"]
