package com.proyecto.carro.model;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.List;

public class Car implements Serializable, Comparable<Car> {
    private static final long serialVersionUID = 1L;

    // Car physics parameters
    private double x;
    private double y;
    private double theta; // Orientation in radians
    private double speed;

    private final double maxSpeed = 4.5;
    private final double minSpeed = -1.5;
    private final double friction = 0.985;
    
    // Save last frame position to check checkpoint crossings
    private double prevX;
    private double prevY;

    // Car dimensions
    private final double width = 18.0;
    private final double height = 10.0;

    private boolean alive;
    private int score; // Checkpoints crossed
    private int framesAlive;
    private int currentCheckpoint;
    private double fitness;

    // Sensor parameters
    private final int numSensors;
    private final double rayLength = 150.0;
    private final double[] sensorReadings;
    private final Point2D.Double[] sensorPoints;

    private final NeuralNetwork brain;

    public Car(double startX, double startY, double startAngle, int numSensors) {
        this.numSensors = numSensors;
        this.sensorReadings = new double[numSensors];
        this.sensorPoints = new Point2D.Double[numSensors];
        
        // Brain inputs: numSensors + 1 (for speed)
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
     * Executes the LIDAR raycasting against all wall segments.
     */
    public void updateSensors(List<Line2D.Double> walls) {
        for (int i = 0; i < numSensors; i++) {
            // Distribute ray angles dynamically in a front-facing arc [-90 deg, +90 deg]
            double angleOffset = 0.0;
            if (numSensors > 1) {
                angleOffset = -Math.PI / 2.0 + i * Math.PI / (numSensors - 1);
            }
            double rayAngle = theta + angleOffset;

            double rx2 = x + rayLength * Math.cos(rayAngle);
            double ry2 = y + rayLength * Math.sin(rayAngle);

            // Find closest intersection with walls
            double closestT = 1.0; // 1.0 means no intersection within ray length
            
            for (Line2D.Double wall : walls) {
                double t = getLineIntersection(x, y, rx2, ry2, wall.x1, wall.y1, wall.x2, wall.y2);
                if (t < closestT) {
                    closestT = t;
                }
            }

            sensorReadings[i] = closestT;
            
            // Calculate coordinates of intersection point
            double ix = x + (rayLength * closestT) * Math.cos(rayAngle);
            double iy = y + (rayLength * closestT) * Math.sin(rayAngle);
            sensorPoints[i] = new Point2D.Double(ix, iy);
        }
    }

    /**
     * Standard line segment intersection helper.
     * Ray segment: (x1, y1) -> (x2, y2)
     * Wall segment: (x3, y3) -> (x4, y4)
     * Returns: t value [0.0..1.0] representing intersection depth along the ray.
     */
    private double getLineIntersection(double x1, double y1, double x2, double y2,
                                       double x3, double y3, double x4, double y4) {
        double dx1 = x2 - x1;
        double dy1 = y2 - y1;
        double dx2 = x4 - x3;
        double dy2 = y4 - y3;

        double det = dx1 * dy2 - dy1 * dx2;
        if (Math.abs(det) < 0.0001) {
            return 1.0; // Parallel lines
        }

        double t = ((x3 - x1) * dy2 - (y3 - y1) * dx2) / det;
        double u = ((x3 - x1) * dy1 - (y3 - y1) * dx1) / det;

        if (t >= 0.0 && t <= 1.0 && u >= 0.0 && u <= 1.0) {
            return t;
        }
        return 1.0;
    }

    /**
     * AI update (sensors -> brain -> physics).
     */
    public void update(List<Line2D.Double> walls) {
        if (!alive) return;

        framesAlive++;

        // 1. Update distance sensor readings
        updateSensors(walls);

        // 2. Build brain inputs: [sensors..., normalizedSpeed]
        double[] inputs = new double[numSensors + 1];
        for (int i = 0; i < numSensors; i++) {
            inputs[i] = sensorReadings[i];
        }
        inputs[numSensors] = speed / maxSpeed;

        // 3. Compute brain outputs
        double[] outputs = brain.compute(inputs);
        double steerInput = outputs[0]; // [-1.0..1.0]
        double accelInput = outputs[1]; // [-1.0..1.0]

        // 4. Update physics
        applyMovement(steerInput, accelInput, true);
    }

    /**
     * Manual human keyboard controls.
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

        // Apply Acceleration
        if (accelInput > 0.0) {
            speed += 0.12 * accelInput;
        } else if (accelInput < 0.0) {
            speed += 0.08 * accelInput; // braking / reverse
        }

        // Apply Friction drag
        speed *= friction;

        // Cap speed limits
        if (speed > maxSpeed) speed = maxSpeed;
        if (speed < minSpeed) speed = minSpeed;

        // Enforce a minimum speed for AI cars so they don't get stuck standing still
        if (isAI && speed < 0.8) {
            speed = 0.8;
        }

        // Apply Steering (turning rate scales with speed to avoid spinning in place)
        double steeringScale = Math.min(1.0, Math.abs(speed) / 1.5);
        double steerAngle = 0.06 * steerInput * steeringScale;
        theta += steerAngle;

        // Move coordinates
        x += speed * Math.cos(theta);
        y += speed * Math.sin(theta);
    }

    /**
     * Check if the car collides with any of the wall segments.
     * Uses point-to-segment distance.
     */
    public void checkCollisions(List<Line2D.Double> walls) {
        if (!alive) return;

        double collisionRadius = 5.0; // Bounding radius of the car
        for (Line2D.Double wall : walls) {
            if (wall.ptSegDist(x, y) < collisionRadius) {
                setAlive(false);
                return;
            }
        }
    }

    /**
     * Verifies if the car crossed its next target checkpoint.
     */
    public void checkCheckpoint(List<Line2D.Double> checkpoints) {
        if (!alive) return;

        int nextCpIndex = currentCheckpoint;
        Line2D.Double checkpoint = checkpoints.get(nextCpIndex);

        // Check if segment (prevX, prevY) -> (x, y) intersects the checkpoint line
        Line2D.Double movement = new Line2D.Double(prevX, prevY, x, y);
        if (movement.intersectsLine(checkpoint)) {
            score++;
            currentCheckpoint = (currentCheckpoint + 1) % checkpoints.size();
            framesAlive += 150; // Give a survival frames bonus for progress!
        }
    }

    public void calculateFitness() {
        // High reward for checkpoints crossed, with frames alive as secondary tiebreaker
        this.fitness = (score * 1500.0) + framesAlive;
    }

    @Override
    public int compareTo(Car other) {
        return Double.compare(other.fitness, this.fitness); // Descending order
    }
}
