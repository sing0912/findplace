#!/bin/bash

# PetPro Monitoring Stack Management Script
# Usage: ./scripts/monitoring.sh [start|stop|status|logs|restart]

PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

# Auto-detect compose command
if command -v docker-compose &> /dev/null; then
    COMPOSE_CMD="docker-compose"
elif command -v podman-compose &> /dev/null; then
    COMPOSE_CMD="podman-compose"
elif command -v docker &> /dev/null && $COMPOSE_CMD version &> /dev/null; then
    COMPOSE_CMD="$COMPOSE_CMD"
else
    echo "Error: Neither docker-compose nor podman-compose found"
    exit 1
fi

MONITORING_SERVICES="prometheus loki promtail tempo grafana"

function print_header() {
    echo ""
    echo "=========================================="
    echo "  PetPro Monitoring Stack"
    echo "=========================================="
    echo ""
}

function start_monitoring() {
    print_header
    echo "Starting monitoring services..."
    $COMPOSE_CMD up -d $MONITORING_SERVICES
    echo ""
    echo "Services started. Access points:"
    echo "  - Grafana:    http://localhost:3001 (admin / \${GRAFANA_ADMIN_PASSWORD})"
    echo "  - Prometheus: http://localhost:9090"
    echo "  - Loki:       http://localhost:3100"
    echo "  - Tempo:      http://localhost:3200"
    echo ""
}

function stop_monitoring() {
    print_header
    echo "Stopping monitoring services..."
    $COMPOSE_CMD stop $MONITORING_SERVICES
    echo "Done."
}

function restart_monitoring() {
    print_header
    echo "Restarting monitoring services..."
    $COMPOSE_CMD restart $MONITORING_SERVICES
    echo "Done."
}

function show_status() {
    print_header
    echo "Monitoring services status:"
    echo ""
    $COMPOSE_CMD ps $MONITORING_SERVICES
}

function show_logs() {
    local service=${1:-""}
    if [ -z "$service" ]; then
        $COMPOSE_CMD logs -f --tail=100 $MONITORING_SERVICES
    else
        $COMPOSE_CMD logs -f --tail=100 "$service"
    fi
}

function show_help() {
    print_header
    echo "Usage: ./scripts/monitoring.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start          Start all monitoring services"
    echo "  stop           Stop all monitoring services"
    echo "  restart        Restart all monitoring services"
    echo "  status         Show status of monitoring services"
    echo "  logs [service] Show logs (optionally for specific service)"
    echo "  help           Show this help message"
    echo ""
    echo "Services: prometheus, loki, promtail, tempo, grafana"
    echo ""
}

# Main
case "${1:-help}" in
    start)
        start_monitoring
        ;;
    stop)
        stop_monitoring
        ;;
    restart)
        restart_monitoring
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs "$2"
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "Unknown command: $1"
        show_help
        exit 1
        ;;
esac
