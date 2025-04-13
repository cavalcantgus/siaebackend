# Use a imagem base do OpenJDK
FROM openjdk:17-jdk-alpine

RUN apk add --no-cache ca-certificates && update-ca-certificates

# Crie o diretório da aplicação
RUN mkdir -p /app

# Defina o diretório de trabalho
WORKDIR /app

# Copie o arquivo JAR da aplicação para o container
COPY target/*.jar app.jar

# Exponha a porta usada pela aplicação Spring Boot
EXPOSE 8080

# Variável de ambiente opcional (melhor prática para configurar o Java)
ENV JAVA_OPTS=""

# Comando para rodar o aplicativo com as opções do Java
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
