# Dockerfile para a aplicação Spring Boot (Backend)

# Estágio de Build: Compilar a aplicação Java com Maven
FROM maven:3.8.5-openjdk-17 AS builder

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o pom.xml para baixar as dependências primeiro (cache de layer do Docker)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o restante do código fonte da aplicação
COPY src ./src

# Compila a aplicação e empacota em um JAR
# O skipTests é opcional, mas comum para builds Docker mais rápidas
RUN mvn package -DskipTests

# Estágio de Runtime: Executar a aplicação
FROM openjdk:17-jdk-slim

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR construído do estágio anterior
COPY --from=builder /app/target/*.jar app.jar

# Expõe a porta que a aplicação Spring Boot usa (definida em application.properties ou pela variável SERVER_PORT)
EXPOSE 8080

# Define variáveis de ambiente que podem ser sobrescritas no docker-compose.yml ou no comando docker run
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=8080

# Comando para executar a aplicação quando o container iniciar
# ENTRYPOINT ["java", "-jar", "app.jar"]
# Usar exec java -jar para que o PID 1 seja o da JVM, permitindo graceful shutdown
CMD ["java", "-jar", "/app/app.jar"] 