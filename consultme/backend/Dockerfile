# ---------- 1. faza: build (JDK + Maven wrapper) ----------
FROM eclipse-temurin:25-jdk AS build

WORKDIR /app

# Prvo samo Maven fajlovi -> Docker kešira skinute biblioteke,
# pa se ponovo skidaju samo kad se promeni pom.xml
COPY mvnw pom.xml ./
COPY .mvn .mvn
# sed uklanja Windows kraj linije (CRLF) iz mvnw, inače Linux ne može da ga pokrene
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B dependency:go-offline

# Zatim izvorni kod i build (testovi se preskaču jer im treba baza)
COPY src src
RUN ./mvnw -B package -DskipTests

# ---------- 2. faza: pokretanje (samo JRE, manji image) ----------
FROM eclipse-temurin:25-jre

WORKDIR /app
COPY --from=build /app/target/consultme.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
