#!/bin/sh

nohup java -Ddd.cws.enabled=true -Ddd.agent.host=datadog-agent -javaagent:/dd-java-agent.jar -jar /span_tester.jar&

while true; do
	curl -v http://localhost:8080/etc/secret
	sleep 5
done
