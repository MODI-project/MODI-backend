# 1. Java 17 기반 경량 이미지 사용 (필요 시 버전 조정)
#    → 안정성을 위해 eclipse-temurin 기반 JRE 사용 권장
FROM eclipse-temurin:17-jre-alpine

# 2. 앱 실행 위치 설정
WORKDIR /app

# 3. 빌드된 jar 파일을 이미지로 복사
COPY build/libs/*.jar app.jar

# (추가) Alpine에 타임존 데이터 설치
RUN apk add --no-cache tzdata

# (추가) JVM 안정화 옵션 설정
# - 메모리 고정(-Xms/-Xmx)
# - 컨테이너 메트릭 인식 비활성화(-XX:-UseContainerSupport)
# - 타임존을 서울로 지정(-Duser.timezone=Asia/Seoul)
ENV JAVA_TOOL_OPTIONS="-Xms256m -Xmx512m -XX:-UseContainerSupport -Duser.timezone=Asia/Seoul"

# 4. 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]