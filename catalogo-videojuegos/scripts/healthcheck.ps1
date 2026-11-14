# Espera hasta 90 s a que la app responda UP en /actuator/health - Windows
param([int]$Port = 8081)

$url = "http://localhost:$Port/actuator/health"
for ($i = 1; $i -le 45; $i++) {
    try {
        $r = Invoke-RestMethod -Uri $url -TimeoutSec 2
        if ($r.status -eq "UP") {
            Write-Host "Health check OK ($url) tras $($i * 2) s"
            exit 0
        }
    } catch {
        # todavia no levanto
    }
    Start-Sleep -Seconds 2
}
Write-Error "Health check FALLO: la app no respondio en $url"
exit 1
