#!/usr/bin/env bash

cd local
docker-compose up -d
cd ..
tflocal apply -auto-approve
