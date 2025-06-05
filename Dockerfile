FROM eclipse-temurin:17-jre

WORKDIR /app

# JAR 파일 복사 (로컬에서 빌드된 파일 사용)
COPY build/libs/*.jar app.jar

EXPOSE 8080

# 힙 메모리 설정 (최소 2GB, 최대 4GB)
ENTRYPOINT ["java", "-Xms2g", "-Xmx4g", "-jar", "app.jar"]