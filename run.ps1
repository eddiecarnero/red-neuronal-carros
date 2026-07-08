# PowerShell runner script for Self-Driving Car AI (Neuroevolution)
# Compiles and runs the pure Java project directly without external dependencies.

$ErrorActionPreference = "Stop"

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  Iniciando Conducción Autónoma IA (Red Neuronal + AG)     " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# Ensure build output directory exists
if (!(Test-Path -Path "target/classes")) {
    New-Item -ItemType Directory -Path "target/classes" -Force | Out-Null
}

Write-Host "Buscando archivos fuente Java..." -ForegroundColor Gray
$javaFiles = Get-ChildItem -Path "src" -Filter *.java -Recurse | ForEach-Object { $_.FullName }

if ($javaFiles.Count -eq 0) {
    Write-Error "No se encontraron archivos fuentes Java en 'src'."
    exit 1
}

Write-Host "Compilando con javac..." -ForegroundColor Cyan
javac -encoding UTF-8 -d target/classes $javaFiles

Write-Host "Compilación exitosa. Ejecutando aplicación..." -ForegroundColor Green
java -cp target/classes com.proyecto.carro.Main
