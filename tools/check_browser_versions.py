import os
import shutil
import subprocess
import sys
from pathlib import Path

BROWSERS = {
    "chrome": {
        "names": ["chrome", "chrome.exe", "google-chrome"],
        "paths": [
            r"%ProgramFiles%\Google\Chrome\Application\chrome.exe",
            r"%ProgramFiles(x86)%\Google\Chrome\Application\chrome.exe",
            r"%LocalAppData%\Google\Chrome\Application\chrome.exe",
        ],
        "arg": "--version"
    },
    "edge": {
        "names": ["msedge", "msedge.exe", "edge", "microsoft-edge"],
        "paths": [
            r"%ProgramFiles%\Microsoft\Edge\Application\msedge.exe",
            r"%ProgramFiles(x86)%\Microsoft\Edge\Application\msedge.exe",
            r"%LocalAppData%\Microsoft\Edge\Application\msedge.exe",
        ],
        "arg": "--version"
    },
    "firefox": {
        "names": ["firefox", "firefox.exe"],
        "paths": [
            r"%ProgramFiles%\Mozilla Firefox\firefox.exe",
            r"%ProgramFiles(x86)%\Mozilla Firefox\firefox.exe",
            r"%LocalAppData%\Mozilla Firefox\firefox.exe",
        ],
        "arg": "--version"
    }
}

def expand(path):
    return os.path.expandvars(path)

def run_version_cmd(exe_path, arg):
    try:
        # Some browsers print version to stdout, some to stderr — capture both.
        completed = subprocess.run([exe_path, arg],
                                   stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE,
                                   check=False,
                                   text=True,
                                   timeout=5)
        output = (completed.stdout or "").strip()
        if not output:
            output = (completed.stderr or "").strip()
        return output if output else None
    except Exception:
        return None

def get_file_version_windows(exe_path):
    """Use PowerShell to read file VersionInfo.ProductVersion (Windows fallback)."""
    try:
        # Use double quotes around path to avoid issues with spaces.
        cmd = [
            "powershell",
            "-NoProfile",
            "-Command",
            f"(Get-Item \"{exe_path}\").VersionInfo.ProductVersion"
        ]
        completed = subprocess.run(cmd,
                                   stdout=subprocess.PIPE,
                                   stderr=subprocess.PIPE,
                                   check=False,
                                   text=True,
                                   timeout=5)
        out = (completed.stdout or "").strip()
        if out:
            return out
    except Exception:
        pass
    return None

def get_edge_version_registry():
    """Try reading Edge version from BLBeacon registry key (HKCU)."""
    try:
        completed = subprocess.run(
            ["reg", "query", r"HKEY_CURRENT_USER\Software\Microsoft\Edge\BLBeacon", "/v", "version"],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            check=False,
            text=True,
            timeout=5
        )
        out = (completed.stdout or "").strip()
        # Output example:
        # HKEY_CURRENT_USER\Software\Microsoft\Edge\BLBeacon
        #     version    REG_SZ    120.0.0.0
        if out:
            for line in out.splitlines():
                parts = line.strip().split()
                if len(parts) >= 3 and parts[0].lower() == "version":
                    return parts[-1].strip()
    except Exception:
        pass
    return None

def find_executable(candidate_names, candidate_paths):
    # 1) try shutil.which (PATH)
    for name in candidate_names:
        path = shutil.which(name)
        if path:
            return path
    # 2) try common absolute paths
    for p in candidate_paths:
        p_exp = expand(p)
        if os.path.isfile(p_exp):
            return p_exp
    return None

def detect_browser_versions():
    results = {}
    is_windows = os.name == 'nt'
    for key, cfg in BROWSERS.items():
        exe = find_executable(cfg["names"], cfg["paths"])
        if exe:
            ver = run_version_cmd(exe, cfg["arg"])
            # common Edge message when a running instance absorbs the call
            if is_windows and (ver is None or "opening in existing browser" in ver.lower()):
                # try PowerShell file version
                ps_ver = get_file_version_windows(exe)
                if ps_ver:
                    ver = ps_ver
                elif key == "edge":
                    # last resort: registry
                    reg_ver = get_edge_version_registry()
                    if reg_ver:
                        ver = reg_ver
            results[key] = {"exe": exe, "version": ver or "unknown (no output)"}
        else:
            results[key] = {"exe": None, "version": None}
    return results

def print_report(results):
    print("Browser version report:")
    for name, info in results.items():
        if info["exe"] is None:
            print(f"- {name.title():7}: not found")
        else:
            print(f"- {name.title():7}: {info['version']} (exe: {info['exe']})")

if __name__ == "__main__":
    res = detect_browser_versions()
    print_report(res)
    # exit code 0 if at least one found, 2 if none found
    if all(v["exe"] is None for v in res.values()):
        sys.exit(2)
    sys.exit(0)
