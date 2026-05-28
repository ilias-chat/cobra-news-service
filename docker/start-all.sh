#!/usr/bin/env sh
set -eu

ORB_HOST="${ORB_HOST:-127.0.0.1}"
ORB_PORT="${ORB_PORT:-1050}"
HTTP_PORT="${PORT:-${GATEWAY_HTTP_PORT:-8095}}"
CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME:-NewsService}"
JACORB_NET_OPTS="-Djacorb.dns.enable=false -Djacorb.iiop.address=${ORB_HOST} -Djacorb.iiop.listener_host=${ORB_HOST}"

echo "Starting CORBA naming on ${ORB_HOST}:${ORB_PORT}"
java ${JACORB_NET_OPTS} -cp /app/app.jar org.jacorb.naming.NameServer \
  -DOAPort="${ORB_PORT}" -DORBBindAddr="${ORB_HOST}" &
sleep 4

echo "Starting CORBA producer (${CORBA_SERVICE_NAME})"
ORB_HOST="${ORB_HOST}" ORB_PORT="${ORB_PORT}" CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME}" \
  java ${JACORB_NET_OPTS} -cp /app/app.jar com.dwsc.corba.news.server.NewsProducerServer &
sleep 6

echo "Starting HTTP gateway on port ${HTTP_PORT}"
ORB_HOST="${ORB_HOST}" ORB_PORT="${ORB_PORT}" GATEWAY_HTTP_PORT="${HTTP_PORT}" \
  CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME}" \
  exec java ${JACORB_NET_OPTS} -cp /app/app.jar com.dwsc.corba.news.gateway.NewsGatewayServer
