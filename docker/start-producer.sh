#!/usr/bin/env sh
set -eu

ORB_HOST="${ORB_HOST:-0.0.0.0}"
ORB_PORT="${ORB_PORT:-1050}"
CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME:-NewsService}"

echo "Starting CORBA naming service at ${ORB_HOST}:${ORB_PORT}"
orbd -ORBInitialHost "${ORB_HOST}" -ORBInitialPort "${ORB_PORT}" &

echo "Starting CORBA producer service '${CORBA_SERVICE_NAME}'"
exec java -cp /app/app.jar com.dwsc.corba.news.server.NewsProducerServer
