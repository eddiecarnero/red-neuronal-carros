package com.proyecto.carro.model;

import java.io.Serializable;
import java.util.Random;

public class NeuralNetwork implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int inputSize;
    private final int hiddenSize;
    private final int outputSize;

    public double[][] weights1; // Entrada -> Oculta
    public double[] biases1;    // Sesgos de la capa oculta
    public double[][] weights2; // Oculta -> Salida
    public double[] biases2;    // Sesgos de la capa de salida

    private static final Random rand = new Random();

    public NeuralNetwork(int inputSize, int hiddenSize, int outputSize) {
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.outputSize = outputSize;

        weights1 = new double[inputSize][hiddenSize];
        biases1 = new double[hiddenSize];
        weights2 = new double[hiddenSize][outputSize];
        biases2 = new double[outputSize];

        randomize();
    }

    public void randomize() {
        randomizeMatrix(weights1);
        randomizeArray(biases1);
        randomizeMatrix(weights2);
        randomizeArray(biases2);
    }

    private void randomizeMatrix(double[][] m) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                // Inicialización Xavier/Glorot
                m[i][j] = (rand.nextDouble() * 2.0 - 1.0) * Math.sqrt(2.0 / inputSize);
            }
        }
    }

    private void randomizeArray(double[] a) {
        for (int i = 0; i < a.length; i++) {
            a[i] = rand.nextDouble() * 2.0 - 1.0;
        }
    }

    // Paso de inferencia (Feedforward)
    public double[] compute(double[] inputs) {
        // Entrada -> Oculta
        double[] hidden = new double[hiddenSize];
        for (int j = 0; j < hiddenSize; j++) {
            double sum = biases1[j];
            for (int i = 0; i < inputSize; i++) {
                sum += inputs[i] * weights1[i][j];
            }
            hidden[j] = Math.tanh(sum); // Activación tangente hiperbólica
        }

        // Oculta -> Salida
        double[] outputs = new double[outputSize];
        for (int j = 0; j < outputSize; j++) {
            double sum = biases2[j];
            for (int i = 0; i < hiddenSize; i++) {
                sum += hidden[i] * weights2[i][j];
            }
            outputs[j] = Math.tanh(sum); // Activación tangente hiperbólica (salidas en rango [-1.0, 1.0])
        }

        return outputs;
    }

    // Cruce genético de dos cerebros (Crossover)
    public NeuralNetwork crossover(NeuralNetwork partner) {
        NeuralNetwork child = new NeuralNetwork(inputSize, hiddenSize, outputSize);

        crossoverMatrix(this.weights1, partner.weights1, child.weights1);
        crossoverArray(this.biases1, partner.biases1, child.biases1);
        crossoverMatrix(this.weights2, partner.weights2, child.weights2);
        crossoverArray(this.biases2, partner.biases2, child.biases2);

        return child;
    }

    private void crossoverMatrix(double[][] m1, double[][] m2, double[][] dest) {
        for (int i = 0; i < m1.length; i++) {
            for (int j = 0; j < m1[i].length; j++) {
                dest[i][j] = rand.nextBoolean() ? m1[i][j] : m2[i][j];
            }
        }
    }

    private void crossoverArray(double[] a1, double[] a2, double[] dest) {
        for (int i = 0; i < a1.length; i++) {
            dest[i] = rand.nextBoolean() ? a1[i] : a2[i];
        }
    }

    // Mutar pesos y sesgos aplicando ruido gaussiano
    public void mutate(double mutationRate) {
        mutateMatrix(weights1, mutationRate);
        mutateArray(biases1, mutationRate);
        mutateMatrix(weights2, mutationRate);
        mutateArray(biases2, mutationRate);
    }

    private void mutateMatrix(double[][] m, double rate) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[i].length; j++) {
                if (rand.nextDouble() < rate) {
                    m[i][j] += rand.nextGaussian() * 0.2;
                    m[i][j] = Math.max(-3.0, Math.min(3.0, m[i][j])); // Limitar el rango de pesos
                }
            }
        }
    }

    private void mutateArray(double[] a, double rate) {
        for (int i = 0; i < a.length; i++) {
            if (rand.nextDouble() < rate) {
                a[i] += rand.nextGaussian() * 0.2;
                a[i] = Math.max(-3.0, Math.min(3.0, a[i]));
            }
        }
    }

    public NeuralNetwork copy() {
        NeuralNetwork clone = new NeuralNetwork(inputSize, hiddenSize, outputSize);
        for (int i = 0; i < weights1.length; i++) {
            System.arraycopy(weights1[i], 0, clone.weights1[i], 0, weights1[i].length);
        }
        System.arraycopy(biases1, 0, clone.biases1, 0, biases1.length);
        for (int i = 0; i < weights2.length; i++) {
            System.arraycopy(weights2[i], 0, clone.weights2[i], 0, weights2[i].length);
        }
        System.arraycopy(biases2, 0, clone.biases2, 0, biases2.length);
        return clone;
    }

    public void copyWeightsFrom(NeuralNetwork other) {
        for (int i = 0; i < weights1.length; i++) {
            System.arraycopy(other.weights1[i], 0, this.weights1[i], 0, weights1[i].length);
        }
        System.arraycopy(other.biases1, 0, this.biases1, 0, biases1.length);
        for (int i = 0; i < weights2.length; i++) {
            System.arraycopy(other.weights2[i], 0, this.weights2[i], 0, weights2[i].length);
        }
        System.arraycopy(other.biases2, 0, this.biases2, 0, biases2.length);
    }

    public int getInputSize() {
        return inputSize;
    }
}
