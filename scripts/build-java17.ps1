# Compile le projet avec Java 17 (évite class file version 66 / Java 22)
$jdk17 = "C:\Users\USER\.jdks\jbr-17.0.9"
if (-not (Test-Path "$jdk17\bin\java.exe")) {
    Write-Error "JDK 17 introuvable: $jdk17 — ajuste le chemin dans scripts/build-java17.ps1"
    exit 1
}
$env:JAVA_HOME = $jdk17
$env:Path = "$jdk17\bin;" + $env:Path
Set-Location $PSScriptRoot\..
Write-Host "JAVA_HOME=$env:JAVA_HOME"
& java -version
& .\mvnw.cmd clean compile -U
