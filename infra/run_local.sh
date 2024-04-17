cd local
docker-compose up --build -d
cd ..
tflocal apply -auto-approve
