FROM maven:3.3-jdk-8 AS mvn-builder
COPY app /home/mvn/src
WORKDIR /home/mvn/src
RUN mvn install

FROM openjdk:8u342-jre
RUN apt update
RUN apt install -y curl
RUN mkdir /app
RUN cp /etc/passwd /etc/secret
COPY dd-java-agent-1.33.0.jar /dd-java-agent.jar
COPY --from=mvn-builder /home/mvn/src/target/span_tester-1.0-SNAPSHOT.jar /span_tester.jar
COPY --from=safchain/cws-inst:a7 /cws-instrumentation /cws-instrumentation
COPY init.sh /init.sh
CMD ["/cws-instrumentation", "trace", "--probe-addr=datadog-agent:5678", "--", "/init.sh"]
