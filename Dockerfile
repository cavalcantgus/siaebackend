FROM openjdk:17-jdk-slim

RUN apt-get update && apt-get install -y ca-certificates openssl

# Copiar certificados
COPY gmail-smtp.pem /tmp/
COPY avast_certificado.cer /tmp/

# Converte e instala
RUN openssl x509 -inform DER -in /tmp/avast_certificado.cer -out /tmp/avast_certificado.pem && \
    cp /tmp/gmail-smtp.pem /usr/local/share/ca-certificates/gmail-smtp.crt && \
    update-ca-certificates && \
    keytool -importcert -trustcacerts -alias gmail-smtp -file /tmp/gmail-smtp.pem -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt && \
    keytool -importcert -trustcacerts -alias avast_certificado -file /tmp/avast_certificado.pem -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt

# Aplicação
WORKDIR /app
COPY target/*.jar app.jar

EXPOSE 8080

ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
