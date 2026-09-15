FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# 회사 보안 인증서
COPY cert/forcepoint-f1e-np-session.cer /tmp/forcepoint-f1e-np-session.cer
COPY cert/somansa-root-ca.cer /tmp/somansa-root-ca.cer

# Forcepoint
RUN keytool -importcert \
    -trustcacerts \
    -noprompt \
    -alias forcepoint-f1e-np-session \
    -file /tmp/forcepoint-f1e-np-session.cer \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit

# Somansa
RUN keytool -importcert \
    -trustcacerts \
    -noprompt \
    -alias somansa-root-ca \
    -file /tmp/somansa-root-ca.cer \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit

# 등록 확인
RUN keytool -list \
    -keystore "$JAVA_HOME/lib/security/cacerts" \
    -storepass changeit \
    -alias somansa-root-ca

COPY pom.xml .

RUN mvn -B -ntp dependency:go-offline

COPY src ./src

RUN mvn -B -ntp verify

FROM eclipse-temurin:21-jre-jammy
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --uid 10001 --create-home appuser
WORKDIR /app
COPY --from=build --chown=appuser:appuser /build/target/resolve-log-api-1.0.0.jar app.jar
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]

