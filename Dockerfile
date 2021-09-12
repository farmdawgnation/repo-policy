FROM adoptopenjdk:11-jdk-hotspot AS build-env
COPY . /opt/app
WORKDIR /opt/app
RUN ./gradlew build

FROM gcr.io/distroless/java:11
COPY --from=build-env /opt/app/app/build/libs/repo-policy-all.jar /opt/repo-policy-all.jar
WORKDIR /opt

# We override entrypoint here so that callers can provide their own
# arguments to the policy evaluator as they would expect
ENTRYPOINT ["/usr/bin/java", "-jar", "repo-policy-all.jar"]
