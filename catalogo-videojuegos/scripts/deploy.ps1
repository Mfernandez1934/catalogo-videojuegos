# Deploy al ambiente simulado de produccion (carpeta local) - Windows
# Uso: powershell -ExecutionPolicy Bypass -File scripts\deploy.ps1 [-ProdDir C:\prod\catalogo] [-Port 8081]
param(
    [string]$ProdDir = "C:\prod\catalogo",
    [int]$Port = 8081
)
$ErrorActionPreference = "Stop"

$jarOrigen = Join-Path (Get-Location) "web\target\catalogo-web.jar"
if (-not (Test-Path $jarOrigen)) {
    Write-Error "No se encontro $jarOrigen. Ejecutar antes: mvn package"
    exit 1
}

New-Item -ItemType Directory -Force -Path $ProdDir | Out-Null
$pidFile = Join-Path $ProdDir "app.pid"

# 1. Detener la version anterior (si esta corriendo)
if (Test-Path $pidFile) {
    $pidAnterior = [int](Get-Content $pidFile | Select-Object -First 1)
    $proc = Get-Process -Id $pidAnterior -ErrorAction SilentlyContinue
    if ($proc) {
        Write-Host "Deteniendo version anterior (PID $pidAnterior)..."
        Stop-Process -Id $pidAnterior -Force
        $proc.WaitForExit(30000) | Out-Null
        Start-Sleep -Seconds 1
    }
    Remove-Item $pidFile -Force
}

# 2. Copiar el nuevo jar a la carpeta de produccion
Copy-Item $jarOrigen (Join-Path $ProdDir "catalogo-web.jar") -Force

# 3. Iniciar la nueva version en segundo plano
$p = Start-Process -FilePath "java" `
    -ArgumentList "-jar", "catalogo-web.jar", "--server.port=$Port" `
    -WorkingDirectory $ProdDir `
    -RedirectStandardOutput (Join-Path $ProdDir "app.log") `
    -RedirectStandardError (Join-Path $ProdDir "app-error.log") `
    -PassThru
$p.Id | Out-File -FilePath $pidFile -Encoding ascii

Write-Host "Version desplegada en $ProdDir (PID $($p.Id), puerto $Port)"
