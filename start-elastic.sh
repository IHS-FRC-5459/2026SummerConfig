#!/usr/bin/env bash
# Launch the modified Elastic dashboard (importable gauges fork).
# Intended for WSL Ubuntu after: flutter build linux

set -euo pipefail

ELASTIC_ROOT="${ELASTIC_ROOT:-$HOME/elastic_dashboard_with_importable_gauges}"
BUNDLE_DIR="$ELASTIC_ROOT/build/linux/x64/release/bundle"
BINARY="$BUNDLE_DIR/elastic_dashboard"

if [[ ! -x "$BINARY" ]]; then
  echo "Elastic binary not found or not executable:"
  echo "  $BINARY"
  echo
  echo "Build it first (in WSL):"
  echo "  cd \"$ELASTIC_ROOT\""
  echo "  flutter build linux"
  echo
  echo "Or set ELASTIC_ROOT to your clone path:"
  echo "  ELASTIC_ROOT=/path/to/elastic_dashboard_with_importable_gauges $0"
  exit 1
fi

cd "$BUNDLE_DIR"
exec ./elastic_dashboard "$@"