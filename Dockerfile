FROM freedomkk/tomcat-maven
RUN mkdir /usr/local/tomcat/db
RUN mkdir /usr/local/tomcat/content
ADD ./BackContent/ /usr/local/tomcat/content
ADD ./db /usr/local/tomcat/db/
RUN cd /usr/local/tomcat/content
RUN ls /usr/local/tomcat/content
RUN mvn package
RUN mv ./target/*.war /usr/local/tomcat/webapps
EXPOSE 8080
CMD "/usr/local/tomcat/bin/startup.sh"
