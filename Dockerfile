# Multi-stage build pour optimiser la taille de l'image
FROM maven:3.9-eclipse-temurin-17 AS build

# Configure Maven for better network handling
ENV MAVEN_OPTS="-Dmaven.wagon.http.retryHandler.class=standard -Dmaven.wagon.http.retryHandler.count=8 -Dmaven.wagon.http.retryHandler.requestSentEnabled=true -Dmaven.wagon.http.pool=false -Dmaven.wagon.httpconnectionManager.ttlSeconds=300 -Xmx1024m"

WORKDIR /app

# Copier les fichiers de configuration Maven
COPY pom.xml .
COPY .mvn/settings.xml /root/.m2/settings.xml

# Télécharger les dépendances (cache layer) avec retry et timeout augmentés
RUN mvn -U dependency:go-offline -B -s /root/.m2/settings.xml || \
    (sleep 10 && mvn -U dependency:go-offline -B -s /root/.m2/settings.xml) || \
    (sleep 10 && mvn -U dependency:go-offline -B -s /root/.m2/settings.xml)

# Copier le code source
COPY src ./src

# Build de l'application avec retry
RUN mvn -U clean package -DskipTests -s /root/.m2/settings.xml || \
    (sleep 10 && mvn -U clean package -DskipTests -s /root/.m2/settings.xml) || \
    (sleep 10 && mvn -U clean package -DskipTests -s /root/.m2/settings.xml)

# Stage de production
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S spring && adduser -S spring -G spring

# Copier le JAR depuis le stage de build
COPY --from=build /app/target/*.jar app.jar

# Changer le propriétaire
RUN chown spring:spring app.jar

# Passer à l'utilisateur non-root
USER spring:spring

# Exposer le port
EXPOSE 2000

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:2000/actuator/health || exit 1

# Commande de démarrage
ENTRYPOINT ["java", "-jar", "app.jar"]
