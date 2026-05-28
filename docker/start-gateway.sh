#!/usr/bin/env sh
set -eu

ORB_HOST="${ORB_HOST:-corba-news-producer}"
ORB_PORT="${ORB_PORT:-1050}"
CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME:-NewsService}"
GATEWAY_HTTP_PORT="${GATEWAY_HTTP_PORT:-8095}"

echo "Starting gateway on port ${GATEWAY_HTTP_PORT}"
echo "Using CORBA naming ${ORB_HOST}:${ORB_PORT}, service ${CORBA_SERVICE_NAME}"
exec java -cp /app/app.jar com.dwsc.corba.news.gateway.NewsGatewayServer
