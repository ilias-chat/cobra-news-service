#!/usr/bin/env sh
set -eu

ORB_HOST="${ORB_HOST:-127.0.0.1}"
ORB_PORT="${ORB_PORT:-1050}"
HTTP_PORT="${PORT:-${GATEWAY_HTTP_PORT:-8095}}"
CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME:-NewsService}"

# -Djava.net.preferIPv4Stack=true: forces Java to use IPv4 on all interfaces.
#   Without this, JacORB may publish an IPv6 address in IORs which Cloud Run
#   cannot route, causing silent hangs on CORBA invocations.
# -Djacorb.dns.enable=false: prevents JacORB from performing reverse DNS lookups
#   which can block indefinitely in Cloud Run's sandboxed network.
# -Djacorb.iiop.address / listener_host: pins the address published in IORs to
#   the loopback interface so producer and gateway can always find each other
#   within the same container.
JACORB_NET_OPTS="-Djava.net.preferIPv4Stack=true \
  -Djacorb.dns.enable=false \
  -Djacorb.iiop.address=${ORB_HOST} \
  -Djacorb.iiop.listener_host=${ORB_HOST}"

echo "Starting CORBA naming on ${ORB_HOST}:${ORB_PORT}"
# -DOAPort and -DOAIAddr are placed BEFORE -cp so they are JVM system properties
# (not program arguments) and are guaranteed to be read by JacORB at startup.
java ${JACORB_NET_OPTS} \
  -DOAPort="${ORB_PORT}" \
  -DOAIAddr="${ORB_HOST}" \
  -cp /app/app.jar org.jacorb.naming.NameServer &
sleep 4

echo "Starting CORBA producer (${CORBA_SERVICE_NAME})"
ORB_HOST="${ORB_HOST}" ORB_PORT="${ORB_PORT}" CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME}" \
  java ${JACORB_NET_OPTS} \
  -cp /app/app.jar com.dwsc.corba.news.server.NewsProducerServer &
sleep 6

echo "Starting HTTP gateway on port ${HTTP_PORT}"
ORB_HOST="${ORB_HOST}" ORB_PORT="${ORB_PORT}" GATEWAY_HTTP_PORT="${HTTP_PORT}" \
  CORBA_SERVICE_NAME="${CORBA_SERVICE_NAME}" \
  exec java ${JACORB_NET_OPTS} \
  -cp /app/app.jar com.dwsc.corba.news.gateway.NewsGatewayServer
