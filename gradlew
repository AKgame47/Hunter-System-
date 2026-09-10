#!/bin/sh
set -eu
APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$JAR" ]; then
  mkdir -p "$(dirname "$JAR")"
  curl -fsSL "https://services.gradle.org/distributions/gradle-8.13-wrapper.jar" -o "$JAR"
  echo "b5ca811c057b3eb4164c78f4155d667c6092ff98ba91a4c90d29e127426f37a7  $JAR" | sha256sum -c -
fi
exec java -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
