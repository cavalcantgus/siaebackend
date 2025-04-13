# Use a imagem base do OpenJDK

FROM openjdk:17-jdk-slim

# Instale pacotes para certificados SSL e utilitários

RUN apt-get update && apt-get install -y ca-certificates openssl

# Copiar o certificado para uma pasta temporária

COPY gmail-smtp.pem /tmp/gmail-smtp.pem

# Copiar o certificado exportado para o container (mesma pasta do Dockerfile)

COPY avast_certificado.cer /tmp/avast_certificado.cer

# Converte para Base64 PEM

RUN openssl x509 -inform DER -in /tmp/avast_certificado.cer -out /tmp/avast_certificado.pem

# Adicionar o certificado ao sistema (Linux)

RUN cp /tmp/gmail-smtp.pem /usr/local/share/ca-certificates/gmail-smtp.crt && update-ca-certificates

RUN keytool -importcert \
  -trustcacerts \
  -alias avast_certificado \
  -file /tmp/avast_certificado.pem \
  -keystore $JAVA_HOME/lib/security/cacerts \
  -storepass changeit \
  -noprompt

# Adicionar o certificado à Java KeyStore

RUN keytool -importcert \

    -trustcacerts \

    -alias gmail-smtp \

    -file /tmp/gmail-smtp.pem \

    -keystore $JAVA_HOME/lib/security/cacerts \

    -storepass changeit \

    -noprompt

# Criar diretório da aplicação

RUN mkdir -p /app

WORKDIR /app

# Copiar o JAR da aplicação

COPY target/*.jar app.jar

# Expor a porta

EXPOSE 8080

# Variável opcional

ENV JAVA_OPTS=""

# Comando de entrada

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]