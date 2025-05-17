FROM maven:3-openjdk-17 AS build

WORKDIR /build

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

RUN apk update && apk add --no-cache ca-certificates openssl

COPY gmail-smtp.pem /tmp/
COPY avast_certificado.cer /tmp/

RUN openssl x509 -inform DER -in /tmp/avast_certificado.cer -out /tmp/avast_certificado.pem && \
    cp /tmp/gmail-smtp.pem /usr/local/share/ca-certificates/gmail-smtp.crt && \
    update-ca-certificates && \
    keytool -importcert -trustcacerts -alias gmail-smtp -file /tmp/gmail-smtp.pem -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt && \
    keytool -importcert -trustcacerts -alias avast_certificado -file /tmp/avast_certificado.pem -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt && \
    rm -rf /tmp/gmail-smtp.pem /tmp/avast_certificado.cer /tmp/avast_certificado.pem

WORKDIR /app

COPY --from=build /build/target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["java"]
CMD ["-jar", "/app/app.jar"]
