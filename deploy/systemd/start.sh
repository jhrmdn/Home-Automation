#!/bin/sh
# Keep Java in the foreground so systemd can supervise it and deliver SIGTERM.
set -eu

script_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)
java_bin=${JAVA_BIN:-/usr/bin/java}
app_jar=${HOME_AUTOMATION_JAR:-$script_dir/home-automation.jar}

if [ ! -x "$java_bin" ]; then
    printf 'Java executable not found: %s\n' "$java_bin" >&2
    exit 1
fi
if [ ! -r "$app_jar" ]; then
    printf 'Application JAR is not readable: %s\n' "$app_jar" >&2
    exit 1
fi

# Prepend environment settings; explicit command-line values take precedence.
if [ -n "${HOME_AUTOMATION_CYCLE_SECONDS:-}" ]; then
    set -- -c "$HOME_AUTOMATION_CYCLE_SECONDS" "$@"
fi
if [ -n "${HOME_AUTOMATION_SERVER_ADDRESS:-}" ]; then
    set -- --server-address "$HOME_AUTOMATION_SERVER_ADDRESS" "$@"
fi
if [ -n "${HOME_AUTOMATION_SERVER_PORT:-}" ]; then
    set -- --server-port "$HOME_AUTOMATION_SERVER_PORT" "$@"
fi
if [ -n "${HOME_AUTOMATION_HTTPS_CERTIFICATE:-}" ]; then
    set -- --https-certificate "$HOME_AUTOMATION_HTTPS_CERTIFICATE" "$@"
fi

for setting in HTTPS WITH_USERS NO_MIN_PASSWORD_LENGTH VERBOSE TEST; do
    case "$setting" in
        HTTPS) value=${HOME_AUTOMATION_HTTPS:-false}; flag=--https ;;
        WITH_USERS) value=${HOME_AUTOMATION_WITH_USERS:-false}; flag=-with-users ;;
        NO_MIN_PASSWORD_LENGTH) value=${HOME_AUTOMATION_NO_MIN_PASSWORD_LENGTH:-false}; flag=-no-min-password-length ;;
        VERBOSE) value=${HOME_AUTOMATION_VERBOSE:-false}; flag=-v ;;
        TEST) value=${HOME_AUTOMATION_TEST:-false}; flag=-test ;;
    esac
    case "$value" in
        true|1|yes|on) set -- "$flag" "$@" ;;
        false|0|no|off|'') ;;
        *) printf 'Invalid Boolean HOME_AUTOMATION_%s: %s\n' "$setting" "$value" >&2; exit 1 ;;
    esac
done

if [ -n "${HOME_AUTOMATION_LOG_LEVEL:-}" ]; then
    set -- -l "$HOME_AUTOMATION_LOG_LEVEL" "$@"
fi

exec "$java_bin" -Djava.awt.headless=true -jar "$app_jar" "$@"
