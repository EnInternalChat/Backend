mongoimport -h localhost -p 27017 -d EnInternalChatTest -c company /docker-entrypoint-initdb.d/0-company.json
mongoimport -h localhost -p 27017 -d EnInternalChatTest -c employee /docker-entrypoint-initdb.d/0-employee.json
mongoimport -h localhost -p 27017 -d EnInternalChatTest -c section /docker-entrypoint-initdb.d/0-section.json
mongoimport -h localhost -p 27017 -d EnInternalChatTest -c notification /docker-entrypoint-initdb.d/0-notification.json
mongoimport -h localhost -p 27017 -d EnInternalChat -c company /docker-entrypoint-initdb.d/0-company.json
mongoimport -h localhost -p 27017 -d EnInternalChat -c employee /docker-entrypoint-initdb.d/0-employee.json
mongoimport -h localhost -p 27017 -d EnInternalChat -c section /docker-entrypoint-initdb.d/0-section.json
mongoimport -h localhost -p 27017 -d EnInternalChat -c notification /docker-entrypoint-initdb.d/0-notification.json
