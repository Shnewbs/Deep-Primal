#!/usr/bin/env python3
"""Run an isolated Gradle build using the current shell's proxy, not a stale JVM proxy.

This is optional development tooling. It does not change project repositories,
dependencies, delivered installers or ordinary wrapper usage.

For engine game validation use --root-game-task runClient (or runServer) before
the -- separator. Unqualified Gradle game selectors also launch subprojects.
"""
import argparse
import os
from pathlib import Path
import re
import shlex
import subprocess
from urllib.parse import urlsplit


def proxy_properties(environ):
    properties = []
    for scheme in ("http", "https"):
        value = (environ.get(scheme + "_proxy") or environ.get(scheme.upper() + "_PROXY"))
        if not value:
            continue
        proxy = urlsplit(value)
        if proxy.scheme not in ("http", "https") or not proxy.hostname:
            raise ValueError("HTTP(S) proxy URL required for Gradle")
        if proxy.username is not None or proxy.password is not None:
            raise ValueError("This helper does not handle authenticated proxy URLs")
        if not re.fullmatch(r"[A-Za-z0-9_.:-]+", proxy.hostname):
            raise ValueError("Invalid proxy host")
        port = proxy.port or (443 if proxy.scheme == "https" else 80)
        properties.extend([f"-D{scheme}.proxyHost={proxy.hostname}", f"-D{scheme}.proxyPort={port}"])
    return properties


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java-home", type=Path, required=True)
    parser.add_argument("--gradle", type=Path, required=True)
    parser.add_argument("--gradle-user-home", type=Path)
    parser.add_argument("--project", type=Path, default=Path(__file__).resolve().parents[1])
    parser.add_argument("--trust-store", type=Path)
    parser.add_argument("--neoform-agent", type=Path,
                        help="Optional private NeoForm 2.0.31 process-command compatibility agent")
    parser.add_argument("--root-game-task", choices=("runClient", "runServer"),
                        help="Launch exactly the root game task; omit game selectors after --")
    parser.add_argument("gradle_args", nargs=argparse.REMAINDER)
    args = parser.parse_args()
    env = dict(os.environ)
    properties = proxy_properties(env)
    env["JAVA_HOME"] = str(args.java_home.resolve())
    if args.gradle_user_home:
        env["GRADLE_USER_HOME"] = str(args.gradle_user_home.resolve())
    if args.trust_store:
        properties.append("-Djavax.net.ssl.trustStore=" + str(args.trust_store.resolve()))
    jvm_options = list(properties)
    if args.neoform_agent:
        agent = args.neoform_agent.resolve()
        if not agent.is_file():
            raise ValueError("NeoForm compatibility agent does not exist")
        jvm_options.append("-javaagent:" + str(agent))
    if jvm_options:
        # Child NeoForm/native launcher JVMs also need the current transport route.
        # Proxy addresses come only from this invocation's environment, never a saved port.
        env["JAVA_TOOL_OPTIONS"] = " ".join(filter(None, [env.get("JAVA_TOOL_OPTIONS", ""), shlex.join(jvm_options)]))
    gradle_args = args.gradle_args[1:] if args.gradle_args[:1] == ["--"] else args.gradle_args
    if args.root_game_task:
        # Unqualified Gradle task selectors also select identically named subproject
        # tasks. In the combined build that would launch the standalone Themes game
        # as well, sharing environment/capture markers with the engine validation.
        if any(value.rsplit(":", 1)[-1] in ("runClient", "runServer")
               for value in gradle_args):
            parser.error("--root-game-task cannot be combined with game task selectors after --")
        gradle_args = [*gradle_args, ":" + args.root_game_task]
    return subprocess.run([str(args.gradle.resolve()), *properties, *gradle_args],
                          cwd=args.project.resolve(), env=env).returncode


if __name__ == "__main__":
    raise SystemExit(main())
