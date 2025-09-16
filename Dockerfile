FROM openjdk:21-jdk-slim
WORKDIR /app
COPY ./build/libs/Tabilog-0.0.1-SNAPSHOT.jar app.jar
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=$PORT"]
