# Set JDK 17 as the MACHINE-WIDE default Java.
# Why: the machine PATH lists E:\develop\jdk11\bin before any user entry,
#      so user-level JAVA_HOME/PATH alone cannot make `java` resolve to 17.
# Usage: right-click -> "Run with PowerShell" AS ADMINISTRATOR, once.
# Rollback: env-backup.txt (same folder) keeps the previous machine values.

$ErrorActionPreference = 'Stop'
$NewHome = 'E:\develop\jdk17'
$ScriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition

$isAdmin = ([Security.Principal.WindowsPrincipal][Security.Principal.WindowsIdentity]::GetCurrent()).IsInRole([Security.Principal.WindowsBuiltInRole]::Administrator)
if (-not $isAdmin) {
    Write-Host '[ERROR] Administrator privileges required. Right-click the file and choose "Run as administrator".' -ForegroundColor Red
    exit 1
}
$javaExe = Join-Path $NewHome 'bin\java.exe'
if (-not (Test-Path $javaExe)) {
    Write-Host "[ERROR] Not found: $javaExe" -ForegroundColor Red
    exit 1
}

$oldHome = [Environment]::GetEnvironmentVariable('JAVA_HOME', 'Machine')
$oldPath = [Environment]::GetEnvironmentVariable('Path', 'Machine')
$backup = Join-Path $ScriptDir 'env-backup.txt'
"JAVA_HOME=$oldHome`r`nPATH=$oldPath" | Set-Content -Path $backup -Encoding UTF8
Write-Host "Backup: $backup"

[Environment]::SetEnvironmentVariable('JAVA_HOME', $NewHome, 'Machine')

$newBin = Join-Path $NewHome 'bin'
$parts = $oldPath -split ';' | Where-Object { $_ -ne '' }
$newParts = @()
$replaced = $false
foreach ($p in $parts) {
    $t = $p.TrimEnd('\')
    if ($t -ieq 'E:\develop\jdk11\bin') {
        $newParts += $newBin
        $replaced = $true
    } elseif ($t -ieq $newBin) {
        # skip duplicate
    } else {
        $newParts += $p
    }
}
if (-not $replaced) { $newParts = @($newBin) + $newParts }
[Environment]::SetEnvironmentVariable('Path', ($newParts -join ';'), 'Machine')
Write-Host 'Machine PATH updated.'

$sig = @'
using System;
using System.Runtime.InteropServices;
public class EnvBroadcast {
    [DllImport("user32.dll", SetLastError = true, CharSet = CharSet.Auto)]
    public static extern IntPtr SendMessageTimeout(IntPtr hWnd, uint Msg, UIntPtr wParam, string lParam, uint fuFlags, uint uTimeout, out UIntPtr lpdwResult);
}
'@
try {
    Add-Type -TypeDefinition $sig -ErrorAction Stop
    $r = [UIntPtr]::Zero
    [EnvBroadcast]::SendMessageTimeout([IntPtr]0xffff, 0x001A, [UIntPtr]::Zero, 'Environment', 2, 5000, [ref]$r) | Out-Null
    Write-Host 'WM_SETTINGCHANGE broadcast done.'
} catch {
    Write-Host 'Broadcast skipped (not critical; new terminals still pick it up).'
}

Write-Host ''
Write-Host "JAVA_HOME (Machine) = $([Environment]::GetEnvironmentVariable('JAVA_HOME','Machine'))"
Write-Host 'Now open a NEW terminal and run:  where java   /   java -version   /   mvn -v'
