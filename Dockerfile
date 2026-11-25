# build
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -q dependency:go-offline
COPY src ./src
RUN ./mvnw -q package -DskipTests

# runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", \
  "-XX:+UseSerialGC", \
  "-Xmx256m", \
  "-Xss512k", \
  "-XX:MaxMetaspaceSize=128m", \
  "-XX:ReservedCodeCacheSize=64m", \
  "-XX:TieredStopAtLevel=1", \
  "-jar", "app.jar"]