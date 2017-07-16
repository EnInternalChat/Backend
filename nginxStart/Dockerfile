FROM nginx
RUN rm -f /etc/nginx/conf.d/default.conf
ADD ./*.conf /etc/nginx/conf.d/
ADD ./web-server/app/ /usr/share/nginx/html/
EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]
