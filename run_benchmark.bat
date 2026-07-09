@echo off
title Benchmark de Red Neuronal - Headless
echo Compilando benchmark...
javac -encoding UTF-8 -cp target/classes -d target/classes src/com/proyecto/carro/util/Benchmark.java
if %errorlevel% neq 0 (
    echo Error al compilar el benchmark.
    pause
    exit /b
)
echo.
echo Ejecutando arnes de benchmarking...
java -cp target/classes com.proyecto.carro.util.Benchmark
pause
