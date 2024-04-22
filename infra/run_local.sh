cd local
podman compose up -d
cd ..
tflocal apply -auto-approve
