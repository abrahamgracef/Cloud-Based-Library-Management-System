#!/bin/bash

echo "=== [ValidateService] Verifying Service Health ==="

HEALTH_URL="http://localhost:8080/api/stats"
MAX_ATTEMPTS=20
SLEEP_INTERVAL=5

echo "Polling health check endpoint: $HEALTH_URL"

for ((i=1; i<=MAX_ATTEMPTS; i++)); do
    HTTP_STATUS=$(curl -s -o /dev/null -w "%{http_code}" "$HEALTH_URL" || echo "000")
    
    if [ "$HTTP_STATUS" -eq 200 ]; then
        echo "SUCCESS: Service responded with HTTP 200 OK!"
        exit 0
    else
        echo "Attempt $i/$MAX_ATTEMPTS: Service returned HTTP status $HTTP_STATUS. Retrying in ${SLEEP_INTERVAL}s..."
        sleep $SLEEP_INTERVAL
    fi
done

echo "ERROR: Service failed to respond with HTTP 200 OK within time limit."
echo "Last 50 lines of application log:"
tail -n 50 /var/log/library-app.log || true
exit 1
