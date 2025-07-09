# 1. Java 17 기반 경량 이미지 사용 (필요 시 버전 조정)
FROM openjdk:17-jdk-alpine

# 2. 앱 실행 위치 설정
WORKDIR /app

# 3. 빌드된 jar 파일을 이미지로 복사
COPY build/libs/*.jar app.jar

# 4. 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]