FROM mongo
ADD ./init/ /
#RUN sh initMongo.sh
ENTRYPOINT ["/entrypoint.sh"]
CMD ["mongod"]

