#!/bin/bash
#
# Quick build script for XzCore
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

cd "$PROJECT_DIR"

echo "🔧 Building XzCore..."

# Gradle build
./gradlew build "$@"

# Find the JAR
JAR=$(find build/libs -name "*.jar" -not -name "*-sources*" -not -name "*-javadoc*" | head -1)

if [ -n "$JAR" ] && [ -f "$JAR" ]; then
    echo ""
    echo "✅ Build successful!"
    echo "📦 JAR: $JAR"
    ls -lh "$JAR"
else
    echo "❌ Build failed - JAR not found"
    exit 1
fi
