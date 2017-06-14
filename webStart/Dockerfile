FROM freedomkk/tomcat-maven
ADD ./BackContent/ /usr/local/tomcat/
RUN cd /usr/local/tomcat
RUN rm -f ./conf/server.xml ./conf/web.xml
ADD ./server.xml /usr/local/tomcat/conf/
ADD ./web.xml /usr/local/tomcat/conf/
ADD ./ice.garenfeather.cn.jks /usr/local/tomcat/conf/
RUN mvn package
RUN ls ./target
RUN mv ./target/*.war /usr/local/tomcat/webapps
EXPOSE 8080
ENTRYPOINT ["/usr/local/tomcat/bin/catalina.sh", "run" ]
#CMD /usr/local/tomcat/bin/startup.sh
