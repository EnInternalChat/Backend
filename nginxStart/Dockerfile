FROM nginx
ADD ./web-server/ /usr/share/
RUN rm -f /etc/nginx/nginx.conf
ADD ./nginx.conf /etc/nginx/nginx.conf
EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]
