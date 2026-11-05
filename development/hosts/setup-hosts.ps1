# Appends *.oms.local aliases to the Windows hosts file, pointing at either
# 127.0.0.1 or the docker-desktop WSL distro IP. Run as Administrator.

$localhostIp = "127.0.0.1"
if (Get-Command "wsl.exe" -ErrorAction SilentlyContinue) {
    $distros = wsl -l
    if ($distros -and $distros.Contains("docker-desktop")) {
        $WSLIP = wsl -- hostname -I
        $localhostIp = $WSLIP.Split(" ")[0]
    }
}

$hostsPath = "C:\Windows\system32\drivers\etc\hosts"
$output = Get-Content $hostsPath | Where-Object { $_ -NotMatch "oms.local" }

$newLines = @"
$localhostIp database.oms.local auth.oms.local kafka.oms.local kafka-ui.oms.local elasticsearch.oms.local kibana.oms.local redis.oms.local aws.oms.local
$localhostIp identity.oms.local order.oms.local audit-log.oms.local gateway.oms.local storage.oms.local
"@

$output += $newLines.Split("`n") | Where-Object { $_.Trim() -ne "" }
$output | Set-Content -Encoding UTF8 $hostsPath

Write-Host "Done. Review $hostsPath for the oms.local entries."
