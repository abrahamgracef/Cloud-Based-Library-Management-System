#!/bin/bash
set -e

echo "=== [ApplicationStart] Starting Spring Boot Application ==="

APP_JAR="/opt/library-app/library-management-system-1.0.0-SNAPSHOT.jar"
LOG_FILE="/var/log/library-app.log"

if [ ! -f "$APP_JAR" ]; then
    echo "ERROR: JAR file not found at $APP_JAR"
    exit 1
fi

echo "Launching application in background with active profile 'prod'..."
nohup java -jar -Dspring.profiles.active=prod "$APP_JAR" > "$LOG_FILE" 2>&1 &

NEW_PID=$!
echo "Application started with PID: $NEW_PID"
echo $NEW_PID > /opt/library-app/app.pid

echo "=== Application start phase completed ==="
