# Dockerfile

# --- 1. 빌드 단계 ---
# Java 21과 Gradle을 포함한 빌드 환경을 사용합니다.
FROM gradle:8.7-jdk21 AS build

# 작업 디렉토리를 설정합니다.
WORKDIR /home/gradle/src

# 빌드에 필요한 파일들을 복사합니다.
COPY build.gradle settings.gradle ./
COPY src ./src

# Gradle을 사용하여 애플리케이션을 빌드하고 실행 가능한 Jar 파일을 만듭니다.
# 테스트는 제외하여 빌드 속도를 높입니다.
RUN gradle bootJar --no-daemon

# --- 2. 실행 단계 ---
# 실제 서버에서 사용할 최소한의 Java 21 환경을 사용합니다.
FROM openjdk:21-jdk-slim

# 작업 디렉토리를 설정합니다.
WORKDIR /app

# 빌드 단계에서 생성된 Jar 파일을 실행 단계로 복사합니다.
COPY --from=build /home/gradle/src/build/libs/*.jar ./app.jar

# 애플리케이션 실행 시 사용할 포트를 8080으로 노출합니다.
EXPOSE 8080

# 컨테이너가 시작될 때 실행할 명령어를 정의합니다.
ENTRYPOINT ["java", "-jar", "app.jar"]
