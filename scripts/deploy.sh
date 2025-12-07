#!/bin/bash

# Deployment script for remote server
# This script can be run manually on the server or called from CI/CD

set -e  # Exit on error

# Configuration
DEPLOY_PATH="${DEPLOY_PATH:-/opt/automation}"
BACKUP_PATH="${DEPLOY_PATH}/backup"
APP_PATH="${DEPLOY_PATH}/app"
LOG_PATH="${DEPLOY_PATH}/logs"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

log() {
    echo -e "${GREEN}[$(date +'%Y-%m-%d %H:%M:%S')]${NC} $1"
}

error() {
    echo -e "${RED}[ERROR]${NC} $1" >&2
}

warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Create necessary directories
create_directories() {
    log "Creating deployment directories..."
    mkdir -p "$BACKUP_PATH"
    mkdir -p "$APP_PATH"
    mkdir -p "$LOG_PATH"
}

# Backup current deployment
backup_current() {
    if [ -d "$APP_PATH" ] && [ "$(ls -A $APP_PATH)" ]; then
        log "Backing up current deployment..."
        BACKUP_FILE="$BACKUP_PATH/backup-$(date +%Y%m%d-%H%M%S).tar.gz"
        tar -czf "$BACKUP_FILE" -C "$APP_PATH" .
        log "Backup created: $BACKUP_FILE"
    else
        warning "No existing deployment to backup"
    fi
}

# Setup Docker network
setup_docker_network() {
    log "Setting up Docker network..."
    if ! docker network inspect dockervu >/dev/null 2>&1; then
        docker network create dockervu
        log "Docker network 'dockervu' created"
    else
        log "Docker network 'dockervu' already exists"
    fi
}

# Build Docker images
build_docker_images() {
    if [ -f "$APP_PATH/Dockerfile" ]; then
        log "Building Docker images..."
        cd "$APP_PATH"
        docker build -t selenium_automation_runner:latest .
        log "Docker image built successfully"
    else
        warning "No Dockerfile found, skipping image build"
    fi
}

# Deploy with Docker Compose
deploy_docker_compose() {
    log "Deploying with Docker Compose..."
    cd "$APP_PATH"
    
    if [ ! -f "docker-compose.yml" ]; then
        error "docker-compose.yml not found in $APP_PATH"
        exit 1
    fi
    
    # Pull latest images
    docker-compose pull || true
    
    # Stop existing containers
    docker-compose down
    
    # Start new containers
    docker-compose up -d --build
    
    log "Docker Compose deployment completed"
}

# Health check
health_check() {
    log "Performing health check..."
    sleep 5
    
    cd "$APP_PATH"
    docker-compose ps
    
    # Check if containers are running
    if docker-compose ps | grep -q "Up"; then
        log "✅ All services are running"
    else
        error "❌ Some services failed to start"
        docker-compose logs
        exit 1
    fi
}

# Cleanup old backups (keep last 10)
cleanup_backups() {
    log "Cleaning up old backups..."
    cd "$BACKUP_PATH"
    ls -t | tail -n +11 | xargs -r rm -f
    log "Old backups cleaned up"
}

# Main deployment function
main() {
    log "Starting deployment process..."
    
    create_directories
    backup_current
    setup_docker_network
    build_docker_images
    deploy_docker_compose
    health_check
    cleanup_backups
    
    log "✅ Deployment completed successfully!"
}

# Run main function
main "$@"

