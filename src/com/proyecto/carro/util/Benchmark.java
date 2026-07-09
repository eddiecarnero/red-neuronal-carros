package com.proyecto.carro.util;

import com.proyecto.carro.model.NeuralNetwork;

public class Benchmark {

    public static void main(String[] args) {
        System.out.println("==========================================================================");
        System.out.println("   Arnés de Benchmarking Headless - Red Neuronal (Complejidad Espacial/Temporal)   ");
        System.out.println("==========================================================================");
        System.out.println("Este arnés ejecuta la inferencia (feedforward) de forma aislada, sin GUI.");
        System.out.println("Mide tiempos de CPU en nanosegundos y consumo de memoria RAM.");
        System.out.println("==========================================================================");
        System.out.printf("%-12s | %-16s | %-16s | %-16s\n", "Sensores (S)", "Tiempo Inferencia", "Memoria Estimada", "Estado de Caché");
        System.out.println("--------------------------------------------------------------------------");

        // Tamaños de sensores a evaluar (escala logarítmica)
        int[] testSizes = {10, 100, 1000, 10000, 100000, 1000000};
        
        for (int S : testSizes) {
            try {
                // 1. Forzar recolección de basura para limpiar el Heap
                System.gc();
                Thread.sleep(100);

                long memStart = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

                // 2. Instanciar la Red Neuronal (capa oculta constante = 64, salidas = 2)
                NeuralNetwork net = new NeuralNetwork(S, 64, 2);

                long memEnd = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
                double memoryMB = (double) (memEnd - memStart) / (1024 * 1024);
                if (memoryMB < 0.05) memoryMB = (S * 64 * 8.0) / (1024 * 1024); // Fallback matemático si el GC limpia de más

                // Generar entradas aleatorias para el test
                double[] inputs = new double[S];
                for (int i = 0; i < S; i++) {
                    inputs[i] = Math.random();
                }

                // Ajustar las iteraciones para que los casos grandes terminen rápido
                int iterations;
                if (S <= 100) {
                    iterations = 10000;
                } else if (S <= 1000) {
                    iterations = 1000;
                } else if (S <= 10000) {
                    iterations = 100;
                } else if (S <= 100000) {
                    iterations = 10;
                } else {
                    iterations = 4;
                }

                // 3. Calentamiento (Warm-up)
                int warmup = Math.max(1, iterations / 10);
                for (int i = 0; i < warmup; i++) {
                    net.compute(inputs);
                }

                // 4. Medición del tiempo de ejecución
                long startNano = System.nanoTime();
                for (int i = 0; i < iterations; i++) {
                    net.compute(inputs);
                }
                long endNano = System.nanoTime();
                
                double avgTimeNs = (double)(endNano - startNano) / iterations;
                double avgTimeUs = avgTimeNs / 1000.0;

                // 5. Determinar el estado teórico de la memoria Caché
                double totalWeightsBytes = (S * 64 + 64 * 2) * 8.0; // 8 bytes por double
                String cacheState;
                if (totalWeightsBytes < 32 * 1024) {
                    cacheState = "L1 Caché Hit (Ultra Rápido)";
                } else if (totalWeightsBytes < 512 * 1024) {
                    cacheState = "L2 Caché Hit (Rápido)";
                } else if (totalWeightsBytes < 16 * 1024 * 1024) {
                    cacheState = "L3 Caché Hit (Moderado)";
                } else {
                    cacheState = "Cache Miss -> DRAM (Lento)";
                }

                System.out.printf("%-12d | %-12.2f us | %-12.3f MB | %-20s\n", S, avgTimeUs, memoryMB, cacheState);

            } catch (Exception e) {
                System.err.printf("Error al evaluar tamaño S = %d: %s\n", S, e.getMessage());
            }
        }
        System.out.println("==========================================================================");
        System.out.println("Nota: Observa cómo el tiempo por inferencia escala linealmente, pero sufre");
        System.out.println("un retraso porcentual mayor cuando pasa a estado 'Cache Miss -> DRAM'.");
        System.out.println("==========================================================================");
    }
}
