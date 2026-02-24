#!/bin/bash
#
# Publish XzCore to Maven Local for other plugins to use
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

echo "📦 Publishing XzCore to Maven Local..."
./gradlew publishToMavenLocal "$@"

echo ""
echo "✅ XzCore published to ~/.m2/repository/"
echo ""
echo "Other plugins can now use:"
echo '  implementation("com.xzatrix:xzcore:1.0.0")'
