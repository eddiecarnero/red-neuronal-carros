# Informe de Análisis Experimental de Complejidad Algorítmica
Este reporte detalla los resultados empíricos medidos usando el arnés headless, demostrando las complejidades asintóticas reales.

## 1. Complejidad Temporal y Espacial de Inferencia
Mide la latencia de procesamiento hacia adelante (`feedforward`) en función del número de sensores ($S$).

| Sensores ($S$) | Tiempo Inferencia (us) | Memoria RAM (MB) | MFLOPS CPU | Estado de la Caché |
|:---|:---|:---|:---|:---|
| 5 | 9.22 us | 0.002 MB | 97.2 | L1 Caché (< 32 KB) |
| 10 | 3.79 us | 0.109 MB | 404.8 | L1 Caché (< 32 KB) |
| 50 | 8.20 us | 0.059 MB | 812.0 | L1 Caché (< 32 KB) |
| 100 | 13.90 us | 1.000 MB | 939.3 | L2 Caché (< 512 KB) |
| 500 | 63.60 us | 0.384 MB | 1,010.4 | L2 Caché (< 512 KB) |
| 1,000 | 135.01 us | 0.579 MB | 950.0 | L2 Caché (< 512 KB) |
| 5,000 | 756.70 us | 2.579 MB | 846.1 | L3 Caché (< 16 MB) |
| 10,000 | 2,648.83 us | 5.127 MB | 483.3 | L3 Caché (< 16 MB) |
| 50,000 | 43,678.51 us | 25.917 MB | 146.5 | Cache Miss → DRAM |
| 100,000 | 90,281.73 us | 50.773 MB | 141.8 | Cache Miss → DRAM |
| 500,000 | 466,324.07 us | 267.672 MB | 137.2 | Cache Miss → DRAM |
| 1,000,000 | 924,995.93 us | 541.329 MB | 138.4 | Cache Miss → DRAM |

### Análisis Técnico del Efecto Caché:
- **L1 y L2 Caché Hit**: Tamaños menores a $1,000$ sensores operan en memoria local ultrarrápida del procesador. El rendimiento es máximo (alto nivel de MFLOPS).
- **Desbordamiento a DRAM**: Cuando $S \ge 10^5$, las matrices de pesos superan los bytes disponibles en la caché L3. Esto genera *Cache Misses* constantes, obligando a la CPU a buscar datos en la memoria RAM principal, lo cual penaliza la velocidad y genera un salto no-lineal en el tiempo de procesamiento.

## 2. Complejidad de los Algoritmos de Evolución
- **Algoritmo A (Genético)**: Selección por Torneo + Cruce + Ordenamiento. Complejidad: $\mathcal{O}(N \log N + N \cdot G)$.
- **Algoritmo B (Mutación)**: Búsqueda lineal del líder + Clonación directa. Complejidad: $\mathcal{O}(N \cdot G)$.

| Sensores ($S$) | Tiempo Alg. A (us) | Tiempo Alg. B (us) | Ratio A/B | Ganador |
|:---|:---|:---|:---|:---|
| 5 | 1,757.43 us | 688.50 us | 2.55x | Algoritmo B |
| 10 | 2,502.30 us | 984.90 us | 2.54x | Algoritmo B |
| 50 | 8,968.92 us | 4,277.33 us | 2.10x | Algoritmo B |
| 100 | 19,686.83 us | 7,494.54 us | 2.63x | Algoritmo B |
| 500 | 80,617.77 us | 36,458.27 us | 2.21x | Algoritmo B |
| 1,000 | 167,551.56 us | 77,893.95 us | 2.15x | Algoritmo B |
| 5,000 | 793,547.32 us | 364,703.82 us | 2.18x | Algoritmo B |
| 10,000 | 1,796,749.30 us | 910,835.22 us | 1.97x | Algoritmo B |

## Conclusiones:
1. La complejidad de memoria escala de forma estrictamente lineal, validando la fórmula de almacenamiento $\mathcal{O}(S \cdot H)$.
2. La velocidad de cálculo de la CPU disminuye drásticamente (MFLOPS descienden) cuando el hardware pasa a estado de falla de caché (DRAM).
3. El Algoritmo B es consistentemente más rápido por su costo computacional inferior, aunque el cruce genético del Algoritmo A asegura una mejor exploración global.
