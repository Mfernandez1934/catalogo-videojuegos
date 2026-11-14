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

# 1b. Por las dudas, liberar el puerto: si quedo una instancia huerfana (por ejemplo,
# arrancada por un lanzador de java que no es el proceso real), se detiene igual.
$enPuerto = Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue |
    Select-Object -ExpandProperty OwningProcess -Unique
foreach ($id in $enPuerto) {
    Write-Host "Deteniendo proceso que ocupa el puerto $Port (PID $id)..."
    Stop-Process -Id $id -Force -ErrorAction SilentlyContinue
}
for ($i = 0; $i -lt 30 -and (Get-NetTCPConnection -LocalPort $Port -State Listen -ErrorAction SilentlyContinue); $i++) {
    Start-Sleep -Seconds 1
}

# 2. Copiar el nuevo jar a la carpeta de produccion
Copy-Item $jarOrigen (Join-Path $ProdDir "catalogo-web.jar") -Force

# 3. Iniciar la nueva version en segundo plano, DESACOPLADA de Jenkins.
# No se usa Start-Process con -Redirect*: el proceso java heredaba el pipe de salida
# del paso "powershell" de Jenkins y el build quedaba colgado esperando que se cerrara.
# Win32_Process.Create lanza el proceso sin heredar handles; el log lo escribe Spring Boot.
# Se usa el java.exe REAL (java.home): el "java" del PATH puede ser el lanzador de Oracle
# (javapath\java.exe), que arranca otro proceso hijo y haria que app.pid apunte al lanzador.
$javaHome = (cmd /c "java -XshowSettings:properties -version 2>&1" |
    Select-String '^\s*java\.home\s*=\s*(.+)$').Matches[0].Groups[1].Value.Trim()
$java = Join-Path $javaHome "bin\java.exe"
if (-not (Test-Path $java)) {
    Write-Error "No se encontro java.exe (java.home = '$javaHome')"
    exit 1
}
$log = Join-Path $ProdDir "app.log"
$cmd = "`"$java`" -jar catalogo-web.jar --server.port=$Port --logging.file.name=`"$log`""
$r = Invoke-CimMethod -ClassName Win32_Process -MethodName Create `
    -Arguments @{ CommandLine = $cmd; CurrentDirectory = $ProdDir }
if ($r.ReturnValue -ne 0) {
    Write-Error "No se pudo iniciar la app (Win32_Process.Create devolvio $($r.ReturnValue))"
    exit 1
}
$r.ProcessId | Out-File -FilePath $pidFile -Encoding ascii

Write-Host "Version desplegada en $ProdDir (PID $($r.ProcessId), puerto $Port)"
