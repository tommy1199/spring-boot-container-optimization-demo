loosejar:
    rm -rf ext
    ./mvnw package
    java -Djarmode=tools -jar target/movie-service-0.0.1-SNAPSHOT.jar extract --destination ext
    curl -Lo ext/loosejar-1.2.0.jar https://github.com/kyrill007/loosejar/releases/download/v1.2.0/loosejar-1.2.0.jar
    java -jar -javaagent:./ext/loosejar-1.2.0.jar -Dloosejar.outputFile=test.csv -Dloosejar.format=csv ext/movie-service-0.0.1-SNAPSHOT.jar
