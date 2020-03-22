FROM openjdk:11.0.4-jre-slim
VOLUME /tmp
COPY BOOT-INF/lib /app/lib
COPY META-INF /app/META-INF
COPY BOOT-INF/classes /app
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-cp","app:app/lib/*","com.github.bademux.ghfetcher.GhfetcherApplication"]