#!/usr/bin/env sh
set -eu

ORB_HOST="${ORB_HOST:-127.0.0.1}"
ORB_PORT="${ORB_PORT:-1050}"
PRODUCER_OA_PORT="${PRODUCER_OA_PORT:-30001}"
HTTP_PORT="${PORT:-${GATEWAY_HTTP_PORT:-8095}}"
CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME:-NewsService}"

# Force IPv4 stack and pin all CORBA addresses to loopback so no DNS or external
# interface is involved. Cloud Run/gVisor networking does not behave like a
# standard Linux kernel for ephemeral IIOP ports between forked processes, so we
# fix the POA ports for both naming and producer.
JACORB_NET_OPTS="-Djava.net.preferIPv4Stack=true \
  -Djacorb.dns.enable=false \
  -Djacorb.iiop.address=${ORB_HOST} \
  -Djacorb.iiop.listener_host=${ORB_HOST}"

echo "Starting CORBA naming on ${ORB_HOST}:${ORB_PORT}"
java ${JACORB_NET_OPTS} \
  -DOAPort="${ORB_PORT}" \
  -DOAIAddr="${ORB_HOST}" \
  -cp /app/app.jar org.jacorb.naming.NameServer &
sleep 4

echo "Starting CORBA producer (${CORBA_SERVICE_NAME}) on ${ORB_HOST}:${PRODUCER_OA_PORT}"
ORB_HOST="${ORB_HOST}" ORB_PORT="${ORB_PORT}" CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME}" \
  java ${JACORB_NET_OPTS} \
  -DOAPort="${PRODUCER_OA_PORT}" \
  -DOAIAddr="${ORB_HOST}" \
  -cp /app/app.jar com.dwsc.corba.news.server.NewsProducerServer &
sleep 6

echo "Starting HTTP gateway on port ${HTTP_PORT}"
ORB_HOST="${ORB_HOST}" ORB_PORT="${ORB_PORT}" GATEWAY_HTTP_PORT="${HTTP_PORT}" \
  CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME}" \
  exec java ${JACORB_NET_OPTS} \
  -DOAIAddr="${ORB_HOST}" \
  -cp /app/app.jar com.dwsc.corba.news.gateway.NewsGatewayServer
