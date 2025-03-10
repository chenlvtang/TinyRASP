@echo off
start cmd /k "cd logstash-8.6.2 && bin\logstash.bat -f config\test.conf"
start cmd /k "cd elasticsearch-8.7.0 && bin\elasticsearch.bat"
start cmd /k "cd kibana-8.7.0 && bin\kibana.bat"
