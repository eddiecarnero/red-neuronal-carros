package com.proyecto.carro.model;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;

public class Car implements Serializable, Comparable<Car> {
    private static final long serialVersionUID = 1L;

    // Parámetros físicos del carro
    private double x;
    private double y;
    private double theta; // Orientación en radianes
    private double speed;

    private final double maxSpeed = 4.5;
    private final double minSpeed = -1.5;
    private final double friction = 0.985;
    
    // Guardar la posición del último fotograma para comprobar cruces de checkpoints
    private double prevX;
    private double prevY;

    // Dimensiones del carro
    private final double width = 18.0;
    private final double height = 10.0;

    private boolean alive;
    private int score; // Checkpoints cruzados
    private int framesAlive;
    private int currentCheckpoint;
    private double fitness;

    // Parámetros de los sensores
    private final int numSensors;
    private final double rayLength = 150.0;
    private final double[] sensorReadings;
    private final Point2D.Double[] sensorPoints;

    private final NeuralNetwork brain;

    public Car(double startX, double startY, double startAngle, int numSensors) {
        this.numSensors = numSensors;
        this.sensorReadings = new double[numSensors];
        this.sensorPoints = new Point2D.Double[numSensors];
        
        // Entradas del cerebro: numSensors + 1 (para la velocidad)
        this.brain = new NeuralNetwork(numSensors + 1, 10, 2); 
        
        reset(startX, startY, startAngle);
    }

    public void reset(double startX, double startY, double startAngle) {
        this.x = startX;
        this.y = startY;
        this.prevX = startX;
        this.prevY = startY;
        this.theta = startAngle;
        this.speed = 0.0;
        
        this.alive = true;
        this.score = 0;
        this.framesAlive = 0;
        this.currentCheckpoint = 0;
        this.fitness = 0.0;

        for (int i = 0; i < numSensors; i++) {
            sensorReadings[i] = 1.0;
            sensorPoints[i] = new Point2D.Double(x, y);
        }
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getTheta() {
        return theta;
    }

    public double getSpeed() {
        return speed;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public boolean isAlive() {
        return alive;
    }

    public int getScore() {
        return score;
    }

    public int getFramesAlive() {
        return framesAlive;
    }

    public double getFitness() {
        return fitness;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
        if (!alive) {
            calculateFitness();
        }
    }

    public NeuralNetwork getBrain() {
        return brain;
    }

    public int getNumSensors() {
        return numSensors;
    }

    public Point2D.Double[] getSensorPoints() {
        return sensorPoints;
    }

    public double[] getSensorReadings() {
        return sensorReadings;
    }

    /**
     * Ejecuta el trazado de rayos LIDAR contra todos los segmentos de pared.
     */
    public void updateSensors(List<Line2D.Double> walls) {
        for (int i = 0; i < numSensors; i++) {
            // Distribuir los ángulos de los rayos dinámicamente en un arco frontal [-90 grados, +90 grados]
            double angleOffset = 0.0;
            if (numSensors > 1) {
                angleOffset = -Math.PI / 2.0 + i * Math.PI / (numSensors - 1);
            }
            double rayAngle = theta + angleOffset;

            double rx2 = x + rayLength * Math.cos(rayAngle);
            double ry2 = y + rayLength * Math.sin(rayAngle);

            // Buscar la intersección más cercana con las paredes
            double closestT = 1.0; // 1.0 significa que no hay intersección dentro del rango del rayo
            
            for (Line2D.Double wall : walls) {
                double t = getLineIntersection(x, y, rx2, ry2, wall.x1, wall.y1, wall.x2, wall.y2);
                if (t < closestT) {
                    closestT = t;
                }
            }

            sensorReadings[i] = closestT;
            
            // Calcular las coordenadas del punto de intersección
            double ix = x + (rayLength * closestT) * Math.cos(rayAngle);
            double iy = y + (rayLength * closestT) * Math.sin(rayAngle);
            sensorPoints[i] = new Point2D.Double(ix, iy);
        }
    }

    /**
     * Ayudante estándar de intersección de segmentos de línea.
     * Segmento de rayo: (x1, y1) -> (x2, y2)
     * Segmento de pared: (x3, y3) -> (x4, y4)
     * Retorna: el valor t [0.0..1.0] que representa la profundidad de intersección a lo largo del rayo.
     */
    private double getLineIntersection(double x1, double y1, double x2, double y2,
                                       double x3, double y3, double x4, double y4) {
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;
        double dx2 = x4 - x3;
        double dy2 = y4 - y3;

        double det = dx1 * dy2 - dy1 * dx2;
        if (Math.abs(det) < 0.0001) {
            return 1.0; // Líneas paralelas
        }

        double t = ((x3 - x1) * dy2 - (y3 - y1) * dx2) / det;
        double u = ((x3 - x1) * dy1 - (y3 - y1) * dx1) / det;

        if (t >= 0.0 && t <= 1.0 && u >= 0.0 && u <= 1.0) {
            return t;
        }
        return 1.0;
    }

    /**
     * Actualización de la IA (sensores -> cerebro -> física).
     */
    public void update(List<Line2D.Double> walls) {
        if (!alive) return;

        framesAlive++;

        // 1. Actualizar las lecturas de los sensores de distancia
        updateSensors(walls);

        // 2. Construir las entradas del cerebro: [sensores..., velocidadNormalizada]
        double[] inputs = new double[numSensors + 1];
        for (int i = 0; i < numSensors; i++) {
            inputs[i] = sensorReadings[i];
        }
        inputs[numSensors] = speed / maxSpeed;

        // 3. Calcular las salidas del cerebro
        double[] outputs = brain.compute(inputs);
        double steerInput = outputs[0]; // Giro [-1.0..1.0]
        double accelInput = outputs[1]; // Aceleración [-1.0..1.0]

        // 4. Actualizar la física
        applyMovement(steerInput, accelInput, true);
    }

    /**
     * Controles manuales del teclado humano.
     */
    public void updateManual(double steerInput, double accelInput, List<Line2D.Double> walls) {
        if (!alive) return;
        framesAlive++;
        updateSensors(walls);
        applyMovement(steerInput, accelInput, false);
    }

    private void applyMovement(double steerInput, double accelInput, boolean isAI) {
        prevX = x;
        prevY = y;

        // Aplicar aceleración
        if (accelInput > 0.0) {
            speed += 0.12 * accelInput;
        } else if (accelInput < 0.0) {
            speed += 0.08 * accelInput; // Frenado / Marcha atrás
        }

        // Aplicar arrastre de fricción
        speed *= friction;

        // Limitar los extremos de velocidad
        if (speed > maxSpeed) speed = maxSpeed;
        if (speed < minSpeed) speed = minSpeed;

        // Forzar una velocidad mínima para los carros de IA para que no se queden atascados inmóviles
        if (isAI && speed < 0.8) {
            speed = 0.8;
        }

        // Aplicar dirección (la tasa de giro escala con la velocidad para evitar girar sobre el eje inmóvil)
        double steeringScale = Math.min(1.0, Math.abs(speed) / 1.5);
        double steerAngle = 0.06 * steerInput * steeringScale;
        theta += steerAngle;

        // Mover coordenadas
        x += speed * Math.cos(theta);
        y += speed * Math.sin(theta);
    }

    /**
     * Comprueba si el carro colisiona con alguno de los segmentos de pared.
     * Utiliza la distancia de punto a segmento.
     */
    public void checkCollisions(List<Line2D.Double> walls) {
        if (!alive) return;

        double collisionRadius = 5.0; // Radio de colisión del carro
        for (Line2D.Double wall : walls) {
            if (wall.ptSegDist(x, y) < collisionRadius) {
                setAlive(false);
                return;
            }
        }
    }

    /**
     * Verifica si el carro cruzó su próximo checkpoint objetivo.
     */
    public void checkCheckpoint(List<Line2D.Double> checkpoints) {
        if (!alive) return;

        int nextCpIndex = currentCheckpoint;
        Line2D.Double checkpoint = checkpoints.get(nextCpIndex);

        // Comprobar si el segmento (prevX, prevY) -> (x, y) se intersecta con la línea del checkpoint
        Line2D.Double movement = new Line2D.Double(prevX, prevY, x, y);
        if (movement.intersectsLine(checkpoint)) {
            score++;
            currentCheckpoint = (currentCheckpoint + 1) % checkpoints.size();
            framesAlive += 150; // Dar un bono de fotogramas de supervivencia por progreso!
        }
    }

    public void calculateFitness() {
        // Alta recompensa por checkpoints cruzados, con fotogramas de vida como desempate secundario
        this.fitness = (score * 1500.0) + framesAlive;
    }

    @Override
    public int compareTo(Car other) {
        return Double.compare(other.fitness, this.fitness); // Orden descendente
    }
}
