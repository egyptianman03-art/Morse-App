#!/bin/sh
# Gradle wrapper script for Unix
GRADLE_OPTS="${GRADLE_OPTS} \"-Xdock:name=$APP_NAME\" \"-Xdock:icon=$APP_HOME/media/gradle.icns\""
exec "$JAVACMD" "$@"
