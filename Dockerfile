FROM freedomkk/tomcat-maven
RUN "mkdir /usr/local/tomcat/db"
ADD activi.mysql.init.sql /usr/local/tomcat/db
RUN "cd BackContent"
RUN "mvn package"
EXPOSE 8080
CMD "/usr/local/tomcat/bin/startup.sh"