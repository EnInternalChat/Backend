FROM freedomkk/tomcat-maven
ENV MYSQL_ROOT_PASSWORD 123456
RUN mkdir /usr/local/tomcat/db
ADD ./BackContent/ /usr/local/tomcat/
ADD ./db/ /usr/local/tomcat/db/
RUN cd /usr/local/tomcat
RUN ls /usr/local/tomcat
RUN mvn package
RUN mv ./target/*.war /usr/local/tomcat/webapps
EXPOSE 8080
CMD "/usr/local/tomcat/bin/startup.sh"
