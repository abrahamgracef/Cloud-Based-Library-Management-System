#!/bin/bash
set -e

echo "=== [BeforeInstall] Installing Dependencies & Preparing Directory Structure ==="

# Update system packages
if command -v yum &> /dev/null; then
    yum update -y
    # Install Java 21 (Amazon Corretto 21) if not already installed
    if ! java -version 2>&1 | grep -q "21\."; then
        echo "Installing Amazon Corretto 21..."
        dnf install -y java-21-amazon-corretto-devel || yum install -y java-21-amazon-corretto
    fi
elif command -v apt-get &> /dev/null; then
    apt-get update -y
    if ! java -version 2>&1 | grep -q "21\."; then
        echo "Installing OpenJDK 21..."
        apt-get install -y openjdk-21-jre curl
    fi
fi

# Create app system user if it doesn't exist
if ! id -u libraryapp &>/dev/null; then
    echo "Creating user 'libraryapp'..."
    useradd -r -s /bin/false libraryapp || true
fi

# Create application directory and set permissions
echo "Creating application directory /opt/library-app..."
mkdir -p /opt/library-app
chown -R libraryapp:libraryapp /opt/library-app

# Create log directory and set permissions
echo "Preparing log file /var/log/library-app.log..."
touch /var/log/library-app.log
chown libraryapp:libraryapp /var/log/library-app.log

echo "=== Dependencies installation completed successfully ==="
