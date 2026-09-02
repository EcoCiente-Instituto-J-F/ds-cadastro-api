FROM eclipse-temurin:19-jdk-jammy AS build

WORKDIR /app

COPY gradlew build.gradle settings.gradle ./
COPY gradle ./gradle

RUN sed -i 's/\r$//' gradlew && \
    chmod +x gradlew

COPY src ./src

RUN ./gradlew clean bootJar --no-daemon


FROM eclipse-temurin:19-jre-jammy

WORKDIR /app

RUN groupadd --system spring && \
    useradd --system --gid spring spring

COPY --from=build /app/build/libs/*.jar app.jar

RUN chown spring:spring app.jar

USER spring:spring

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]