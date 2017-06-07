FROM freedomkk/tomcat-maven
ADD ./BackContent/ /usr/local/tomcat/
RUN cd /usr/local/tomcat
RUN mvn package
RUN ls ./target
RUN mv ./target/*.war /usr/local/tomcat/webapps
EXPOSE 8080
ENTRYPOINT ["/usr/local/tomcat/bin/catalina.sh", "run" ]
#CMD /usr/local/tomcat/bin/startup.sh
