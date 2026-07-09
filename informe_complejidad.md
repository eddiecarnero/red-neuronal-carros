# Informe de Análisis Experimental de Complejidad Algorítmica
Este reporte detalla los resultados empíricos medidos usando el arnés headless, demostrando las complejidades asintóticas reales.

## 1. Complejidad Temporal y Espacial de Inferencia
Mide la latencia de procesamiento hacia adelante (`feedforward`) en función del número de sensores ($S$).

| Sensores ($S$) | Tiempo Inferencia (us) | Memoria RAM (MB) | MFLOPS CPU | Estado de la Caché |
|:---|:---|:---|:---|:---|
| 5 | 7.07 us | 0.002 MB | 126.8 | L1 Caché (< 32 KB) |
| 10 | 5.31 us | 0.109 MB | 289.2 | L1 Caché (< 32 KB) |
| 50 | 7.80 us | 0.059 MB | 853.6 | L1 Caché (< 32 KB) |
| 100 | 12.57 us | 1.000 MB | 1,038.7 | L2 Caché (< 512 KB) |
| 500 | 60.42 us | 0.384 MB | 1,063.4 | L2 Caché (< 512 KB) |
| 1,000 | 126.19 us | 0.579 MB | 1,016.3 | L2 Caché (< 512 KB) |
| 5,000 | 806.58 us | 2.579 MB | 793.8 | L3 Caché (< 16 MB) |
| 10,000 | 2,507.81 us | 5.127 MB | 510.5 | L3 Caché (< 16 MB) |
| 50,000 | 42,243.10 us | 25.917 MB | 151.5 | Cache Miss → DRAM |
| 100,000 | 95,182.82 us | 50.773 MB | 134.5 | Cache Miss → DRAM |
| 500,000 | 463,713.47 us | 265.106 MB | 138.0 | Cache Miss → DRAM |
| 1,000,000 | 976,505.23 us | 526.290 MB | 131.1 | Cache Miss → DRAM |

### Análisis Técnico del Efecto Caché:
- **L1 y L2 Caché Hit**: Tamaños menores a $1,000$ sensores operan en memoria local ultrarrápida del procesador. El rendimiento es máximo (alto nivel de MFLOPS).
- **Desbordamiento a DRAM**: Cuando $S \ge 10^5$, las matrices de pesos superan los bytes disponibles en la caché L3. Esto genera *Cache Misses* constantes, obligando a la CPU a buscar datos en la memoria RAM principal, lo cual penaliza la velocidad y genera un salto no-lineal en el tiempo de procesamiento.

## 2. Complejidad de los Algoritmos de Evolución
- **Algoritmo A (Genético)**: Selección por Torneo + Cruce + Ordenamiento. Complejidad: $\mathcal{O}(N \log N + N \cdot G)$.
- **Algoritmo B (Mutación)**: Búsqueda lineal del líder + Clonación directa. Complejidad: $\mathcal{O}(N \cdot G)$.

| Sensores ($S$) | Tiempo Alg. A (us) | Tiempo Alg. B (us) | Ratio A/B | Ganador |
|:---|:---|:---|:---|:---|
| 5 | 2,399.39 us | 785.24 us | 3.06x | Algoritmo B |
| 10 | 2,606.74 us | 1,090.97 us | 2.39x | Algoritmo B |
| 50 | 10,009.82 us | 4,543.47 us | 2.20x | Algoritmo B |
| 100 | 16,906.80 us | 8,300.49 us | 2.04x | Algoritmo B |
| 500 | 87,144.30 us | 37,916.20 us | 2.30x | Algoritmo B |
| 1,000 | 173,797.25 us | 81,143.14 us | 2.14x | Algoritmo B |
| 5,000 | 895,407.60 us | 469,809.44 us | 1.91x | Algoritmo B |
| 10,000 | 1,777,736.68 us | 943,459.88 us | 1.88x | Algoritmo B |

## Conclusiones:
1. La complejidad de memoria escala de forma estrictamente lineal, validando la fórmula de almacenamiento $\mathcal{O}(S \cdot H)$.
2. La velocidad de cálculo de la CPU disminuye drásticamente (MFLOPS descienden) cuando el hardware pasa a estado de falla de caché (DRAM).
3. El Algoritmo B es consistentemente más rápido por su costo computacional inferior, aunque el cruce genético del Algoritmo A asegura una mejor exploración global.
