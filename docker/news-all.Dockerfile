FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
COPY idl ./idl
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system app && useradd --system --gid app app
COPY --from=build /build/target/corba-news-service-0.0.1-SNAPSHOT.jar /app/app.jar
COPY docker/start-all.sh /app/start-all.sh
RUN chmod +x /app/start-all.sh && chown -R app:app /app
USER app:app
EXPOSE 8095
ENTRYPOINT ["/app/start-all.sh"]
