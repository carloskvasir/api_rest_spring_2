# Estágio 1: Build da aplicação
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Copia os arquivos do Maven wrapper e o pom.xml
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Torna o script do maven executável e baixa as dependências (cria cache na camada)
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B

# Copia o código fonte
COPY src ./src

# Compila o projeto ignorando os testes (pois já rodam na esteira de CI/CD)
RUN ./mvnw clean package -DskipTests

# Estágio 2: Imagem de execução otimizada (apenas JRE)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia apenas o arquivo .jar compilado no estágio anterior
COPY --from=build /app/target/*.jar app.jar

# Expõe a porta padrão do Spring Boot
EXPOSE 8080

# Comando de inicialização
ENTRYPOINT ["java", "-jar", "app.jar"]
