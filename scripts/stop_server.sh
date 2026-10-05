#!/bin/bash

echo "=== [ApplicationStop] Stopping Running Application ==="

# Check if application PID is running
PID=$(pgrep -f "library-management-system-1.0.0-SNAPSHOT.jar" || true)

if [ -n "$PID" ]; then
    echo "Stopping process PID: $PID"
    kill -15 $PID
    
    # Wait for up to 15 seconds for process to exit gracefully
    for i in {1..15}; do
        if kill -0 $PID 2>/dev/null; then
            sleep 1
        else
            echo "Process stopped gracefully."
            break
        fi
    done
    
    # Force kill if still running after 15 seconds
    if kill -0 $PID 2>/dev/null; then
        echo "Process did not terminate gracefully; sending SIGKILL..."
        kill -9 $PID
    fi
else
    echo "No running instance of library-management-system found."
fi

# Stop systemd service if active
if systemctl is-active --quiet library-app 2>/dev/null; then
    echo "Stopping systemd service 'library-app'..."
    systemctl stop library-app || true
fi

echo "=== Application stop phase completed ==="
