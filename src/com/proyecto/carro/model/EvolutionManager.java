package com.proyecto.carro.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class EvolutionManager implements Serializable {
    private static final long serialVersionUID = 1L;

    private final int populationSize; // Size of each population (total / 2)
    private List<Car> populationA;     // Algoritmo A: Genético Clásico (Verde/Celeste)
    private List<Car> populationB;     // Algoritmo B: Hill Climbing / Mutación Pura (Rojo/Rosa)
    private Track track;
    private int trackType;

    // Start coordinates (pointing forward, dynamically calculated from track)
    private double startX;
    private double startY;
    private double startAngle;

    // GA parameters for Algorithm A
    private int generationA;
    private int highScoreA;
    private double bestFitnessA;
    private Car bestAllTimeA;

    // GA parameters for Algorithm B
    private int generationB;
    private int highScoreB;
    private double bestFitnessB;
    private Car bestAllTimeB;

    private int numSensors;
    private double mutationRate = 0.05;
    private final double elitismRate = 0.05; // 5% Elites

    private final Random rand = new Random();

    public EvolutionManager(int totalPopulationSize, int numSensors) {
        this.populationSize = totalPopulationSize / 2; // Split 100 into 50 A and 50 B
        this.numSensors = numSensors;
        this.trackType = 0; // Default to Oval track
        this.track = new Track(trackType);
        
        java.awt.geom.Point2D.Double start = track.getStartPoint();
        this.startX = start.x;
        this.startY = start.y;
        this.startAngle = track.getStartAngle();
        
        this.generationA = 1;
        this.generationB = 1;
        this.highScoreA = 0;
        this.highScoreB = 0;
        this.bestFitnessA = 0.0;
        this.bestFitnessB = 0.0;

        initPopulation();
    }

    private void initPopulation() {
        populationA = new ArrayList<>();
        populationB = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            populationA.add(new Car(startX, startY, startAngle, numSensors));
            populationB.add(new Car(startX, startY, startAngle, numSensors));
        }
        bestAllTimeA = populationA.get(0);
        bestAllTimeB = populationB.get(0);
    }

    public void resetSimulation(int newNumSensors, int newTrackType) {
        this.numSensors = newNumSensors;
        this.trackType = newTrackType;
        this.track = new Track(trackType);
        
        java.awt.geom.Point2D.Double start = track.getStartPoint();
        this.startX = start.x;
        this.startY = start.y;
        this.startAngle = track.getStartAngle();
        
        this.generationA = 1;
        this.generationB = 1;
        this.highScoreA = 0;
        this.highScoreB = 0;
        this.bestFitnessA = 0.0;
        this.bestFitnessB = 0.0;
        
        initPopulation();
    }

    public void resetPositionsOnly() {
        for (Car car : populationA) {
            car.reset(startX, startY, startAngle);
        }
        for (Car car : populationB) {
            car.reset(startX, startY, startAngle);
        }
    }

    public List<Car> getPopulationA() {
        return populationA;
    }

    public List<Car> getPopulationB() {
        return populationB;
    }

    public Track getTrack() {
        return track;
    }

    public int getGenerationA() { return generationA; }
    public int getGenerationB() { return generationB; }
    
    public int getHighScoreA() { return highScoreA; }
    public int getHighScoreB() { return highScoreB; }
    
    public double getBestFitnessA() { return bestFitnessA; }
    public double getBestFitnessB() { return bestFitnessB; }
    
    public Car getBestAllTimeA() { return bestAllTimeA; }
    public Car getBestAllTimeB() { return bestAllTimeB; }

    public int getNumSensors() {
        return numSensors;
    }

    public double getMutationRate() {
        return mutationRate;
    }

    public void setMutationRate(double rate) {
        this.mutationRate = rate;
    }

    public boolean isGenerationOverA() {
        for (Car car : populationA) {
            if (car.isAlive()) return false;
        }
        return true;
    }

    public boolean isGenerationOverB() {
        for (Car car : populationB) {
            if (car.isAlive()) return false;
        }
        return true;
    }

    /**
     * Updates physics, sensor rays, checkpoints, and collisions for both populations.
     */
    public void update() {
        // 1. Update Population A
        for (Car car : populationA) {
            if (car.isAlive()) {
                car.update(track.getWallSegments());
                car.checkCollisions(track.getWallSegments());
                car.checkCheckpoint(track.getCheckpoints());
            }
        }

        // 2. Update Population B
        for (Car car : populationB) {
            if (car.isAlive()) {
                car.update(track.getWallSegments());
                car.checkCollisions(track.getWallSegments());
                car.checkCheckpoint(track.getCheckpoints());
            }
        }
    }

    /**
     * Algoritmo A: Algoritmo Genético Clásico.
     * Operadores: Selección por Torneo (k=5), Cruce Uniforme, Mutación Gaussiana (5%), Elitismo (5%).
     * Complejidad: O(N log N + N * G) por ordenar la población.
     */
    public void evolveA() {
        // Sort A by fitness descending
        Collections.sort(populationA);

        // Record historical bests
        Car genBest = populationA.get(0);
        if (genBest.getFitness() > bestFitnessA) {
            bestFitnessA = genBest.getFitness();
            bestAllTimeA = new Car(startX, startY, startAngle, numSensors);
            bestAllTimeA.getBrain().copyWeightsFrom(genBest.getBrain());
            bestAllTimeA.calculateFitness();
        }

        for (Car car : populationA) {
            if (car.getScore() > highScoreA) {
                highScoreA = car.getScore();
            }
        }

        // Elitism (5%)
        List<Car> nextGen = new ArrayList<>();
        int eliteCount = Math.max(1, (int) (populationSize * elitismRate));

        for (int i = 0; i < eliteCount; i++) {
            Car elite = populationA.get(i);
            Car child = new Car(startX, startY, startAngle, numSensors);
            child.getBrain().copyWeightsFrom(elite.getBrain());
            nextGen.add(child);
        }

        // Crossover and mutation
        while (nextGen.size() < populationSize) {
            Car parent1 = selectTournamentA();
            Car parent2 = selectTournamentA();

            NeuralNetwork childBrain = parent1.getBrain().crossover(parent2.getBrain());
            childBrain.mutate(mutationRate);

            Car child = new Car(startX, startY, startAngle, numSensors);
            child.getBrain().copyWeightsFrom(childBrain);
            nextGen.add(child);
        }

        populationA = nextGen;
        for (Car car : populationA) {
            car.reset(startX, startY, startAngle);
        }
        generationA++;
    }

    private Car selectTournamentA() {
        int tournamentSize = 5;
        Car best = null;
        for (int i = 0; i < tournamentSize; i++) {
            Car candidate = populationA.get(rand.nextInt(populationSize));
            if (best == null || candidate.getFitness() > best.getFitness()) {
                best = candidate;
            }
        }
        return best;
    }

    /**
     * Algoritmo B: Hill Climbing / Mutación Pura.
     * Operadores: Selección del mejor por escaneo lineal O(N), clonación asexual y mutación gaussiana (12% tasa, 100% de hijos).
     * Complejidad: O(N * G) ya que evita ordenar la población.
     */
    public void evolveB() {
        // Find best individual (linear scan O(N))
        Car bestCarB = populationB.get(0);
        for (Car car : populationB) {
            if (car.getFitness() > bestCarB.getFitness()) {
                bestCarB = car;
            }
        }

        // Record historical bests
        if (bestCarB.getFitness() > bestFitnessB) {
            bestFitnessB = bestCarB.getFitness();
            bestAllTimeB = new Car(startX, startY, startAngle, numSensors);
            bestAllTimeB.getBrain().copyWeightsFrom(bestCarB.getBrain());
            bestAllTimeB.calculateFitness();
        }

        for (Car car : populationB) {
            if (car.getScore() > highScoreB) {
                highScoreB = car.getScore();
            }
        }

        // Fill next generation
        List<Car> nextGen = new ArrayList<>();

        // Elite: 1 clone of the best parent in population B
        Car elite = new Car(startX, startY, startAngle, numSensors);
        elite.getBrain().copyWeightsFrom(bestCarB.getBrain());
        nextGen.add(elite);

        // Clones with high mutation
        while (nextGen.size() < populationSize) {
            Car child = new Car(startX, startY, startAngle, numSensors);
            child.getBrain().copyWeightsFrom(bestCarB.getBrain());
            child.getBrain().mutate(0.12); // High mutation rate for exploratory steps
            nextGen.add(child);
        }

        populationB = nextGen;
        for (Car car : populationB) {
            car.reset(startX, startY, startAngle);
        }
        generationB++;
    }

    /**
     * Asynchronous background evolution logic for both populations.
     */
    public void evolveMultipleGenerations(int gens, Runnable onProgress) {
        int targetGenA = generationA + gens;
        int targetGenB = generationB + gens;

        while (generationA < targetGenA || generationB < targetGenB) {
            update();

            if (isGenerationOverA() && generationA < targetGenA) {
                evolveA();
                if (onProgress != null) {
                    onProgress.run();
                }
            }

            if (isGenerationOverB() && generationB < targetGenB) {
                evolveB();
                if (onProgress != null) {
                    onProgress.run();
                }
            }
        }
    }
}
