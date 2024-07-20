#!/usr/bin/env bash

cd infra/local
docker-compose -f docker-compose-full.yml up -d
cd ..
tflocal apply -auto-approve
