cd local
docker-compose up --build -d
cd ..
tflocal apply -auto-approve
docker run -d --name rmt-redis -p 6379:6379 redis:7.2.4-alpine
