#!/bin/sh
# Appends *.oms.local aliases pointing at the docker gateway IP into /etc/hosts.
# Run with sudo on the host machine (not inside a container).

DOCKER_IP="127.0.0.1"
if command -v docker >/dev/null 2>&1; then
  GATEWAY_IP=$(docker network inspect oms_oms 2>/dev/null | grep -m1 '"Gateway"' | sed -E 's/.*"Gateway": "([^"]+)".*/\1/')
  if [ -n "$GATEWAY_IP" ]; then
    DOCKER_IP="$GATEWAY_IP"
  fi
fi
echo "Using IP: $DOCKER_IP"

HOSTS="
database.oms.local
auth.oms.local
kafka.oms.local
kafka-ui.oms.local
elasticsearch.oms.local
kibana.oms.local
redis.oms.local
aws.oms.local
identity.oms.local
order.oms.local
audit-log.oms.local
gateway.oms.local
storage.oms.local
"

for h in $HOSTS; do
  if ! grep -q "$h" /etc/hosts; then
    echo "$DOCKER_IP $h" >> /etc/hosts
  fi
done

echo "Done. Review /etc/hosts for the oms.local entries."
