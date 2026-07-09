package com.proyecto.carro.gui;

import com.proyecto.carro.model.Car;
import com.proyecto.carro.model.EvolutionManager;
import com.proyecto.carro.model.Track;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

public class GamePanel extends JPanel {

    private EvolutionManager manager;
    private Car humanCar;
    private boolean showAll = true;
    private boolean humanMode = false;

    // Track Editor Variables
    private boolean editorMode = false;
    private Point2D.Double selectedWaypoint = null;
    private int selectedIndex = -1;
    private final int waypointRadius = 7;

    // Pan & Zoom Variables
    private double zoomFactor = 1.0;
    private double offsetX = 0.0;
    private double offsetY = 0.0;
    private Point dragStartPoint = null;

    // Theme Colors (Catppuccin Mocha)
    private final Color bgDark = new Color(30, 30, 46);

    // Color systems for both algorithms
    private final Color carSwarmA = new Color(137, 220, 235, 45); // Cyan translucent (Swarm A)
    private final Color carBestA = new Color(137, 180, 250);       // Blue solid (Best A)
    private final Color carSwarmB = new Color(245, 194, 231, 45); // Pink translucent (Swarm B)
    private final Color carBestB = new Color(243, 139, 168);       // Red solid (Best B)
    private final Color carHumanColor = new Color(203, 166, 247);  // Purple (Human)

    private final Color sensorNormalA = new Color(137, 220, 235, 70);
    private final Color sensorWarningA = new Color(243, 139, 168, 200);

    private final Color sensorNormalB = new Color(245, 194, 231, 70);
    private final Color sensorWarningB = new Color(243, 139, 168, 200);

    public GamePanel(EvolutionManager manager) {
        this.manager = manager;
        setPreferredSize(new Dimension(450, 450));
        setBackground(bgDark);
        setupMouseListeners();
        setupZoomListeners();
    }

    public void setManager(EvolutionManager manager) {
        this.manager = manager;
    }

    public void setHumanCar(Car humanCar) {
        this.humanCar = humanCar;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
        repaint();
    }

    public void setHumanMode(boolean humanMode) {
        this.humanMode = humanMode;
        repaint();
    }

    public void setEditorMode(boolean editorMode) {
        this.editorMode = editorMode;
        repaint();
    }

    public boolean isEditorMode() {
        return editorMode;
    }

    /**
     * Resets the zoom and panning translation offsets to default.
     */
    public void resetCamera() {
        zoomFactor = 1.0;
        offsetX = 0.0;
        offsetY = 0.0;
        repaint();
    }

    private void setupZoomListeners() {
        addMouseWheelListener(e -> {
            double oldZoom = zoomFactor;
            double scaleVal = 1.1;

            if (e.getWheelRotation() < 0) {
                // Zoom In
                zoomFactor = Math.min(4.0, zoomFactor * scaleVal);
            } else {
                // Zoom Out
                zoomFactor = Math.max(0.4, zoomFactor / scaleVal);
            }

            // Adjust translation offsets so the zoom centers on the mouse cursor
            Point mousePt = e.getPoint();
            double mouseWorldX = (mousePt.x - offsetX) / oldZoom;
            double mouseWorldY = (mousePt.y - offsetY) / oldZoom;

            offsetX = mousePt.x - mouseWorldX * zoomFactor;
            offsetY = mousePt.y - mouseWorldY * zoomFactor;

            repaint();
        });
    }

    private void setupMouseListeners() {
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (manager == null) return;

                // Reset camera offset on middle-click (scroll-wheel click)
                if (SwingUtilities.isMiddleMouseButton(e)) {
                    resetCamera();
                    return;
                }

                List<Point2D.Double> basePoints = manager.getTrack().getBasePoints();

                // Convert screen mouse coordinates to world coordinates
                double worldX = (e.getX() - offsetX) / zoomFactor;
                double worldY = (e.getY() - offsetY) / zoomFactor;
                Point2D.Double worldPt = new Point2D.Double(worldX, worldY);

                // 1. Check if clicked on a waypoint (only active in editor mode)
                selectedIndex = -1;
                selectedWaypoint = null;

                if (editorMode) {
                    for (int i = 0; i < basePoints.size(); i++) {
                        Point2D.Double pt = basePoints.get(i);
                        if (worldPt.distance(pt) < (waypointRadius / zoomFactor) + 6) {
                            selectedIndex = i;
                            selectedWaypoint = pt;
                            break;
                        }
                    }

                    // 2. Right-click deletes a waypoint (keep at least 4 for splines)
                    if (SwingUtilities.isRightMouseButton(e) && selectedIndex != -1) {
                        if (basePoints.size() > 4) {
                            basePoints.remove(selectedIndex);
                            manager.getTrack().regenerate();
                            selectedWaypoint = null;
                            selectedIndex = -1;
                            repaint();
                        } else {
                            JOptionPane.showMessageDialog(GamePanel.this,
                                "El circuito debe tener al menos 4 puntos de control para formar la pista.",
                                "No se puede eliminar", JOptionPane.WARNING_MESSAGE);
                        }
                        return;
                    }

                    // 3. Double-click adds a new waypoint at double-clicked coordinates
                    if (e.getClickCount() == 2 && !SwingUtilities.isRightMouseButton(e) && selectedIndex == -1) {
                        Point2D.Double newPt = new Point2D.Double(worldX, worldY);
                        if (basePoints.isEmpty()) {
                            basePoints.add(newPt);
                        } else {
                            int closestIdx = 0;
                            double minDist = Double.MAX_VALUE;
                            for (int i = 0; i < basePoints.size(); i++) {
                                double d = newPt.distance(basePoints.get(i));
                                if (d < minDist) {
                                    minDist = d;
                                    closestIdx = i;
                                }
                            }
                            basePoints.add((closestIdx + 1) % (basePoints.size() + 1), newPt);
                        }
                        manager.getTrack().regenerate();
                        repaint();
                        return;
                    }
                }

                // 4. Start Panning (right-click always, or left-click when not dragging waypoints)
                if (SwingUtilities.isRightMouseButton(e) || selectedWaypoint == null) {
                    dragStartPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                selectedWaypoint = null;
                selectedIndex = -1;
                dragStartPoint = null;
            }
        };

        MouseMotionAdapter motionAdapter = new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // 1. Handle Waypoint Dragging (Left-click drag in editor mode)
                if (editorMode && selectedWaypoint != null && !SwingUtilities.isRightMouseButton(e)) {
                    double worldX = (e.getX() - offsetX) / zoomFactor;
                    double worldY = (e.getY() - offsetY) / zoomFactor;

                    // Constrain coordinates to fit safely inside the panel boundary
                    selectedWaypoint.x = Math.max(10, Math.min(440, worldX));
                    selectedWaypoint.y = Math.max(10, Math.min(440, worldY));

                    manager.getTrack().regenerate();
                    repaint();
                }
                // 2. Handle Canvas Panning (Dragging)
                else if (dragStartPoint != null) {
                    double dx = e.getX() - dragStartPoint.x;
                    double dy = e.getY() - dragStartPoint.y;

                    offsetX += dx;
                    offsetY += dy;

                    dragStartPoint = e.getPoint();
                    repaint();
                }
            }
        };

        addMouseListener(mouseAdapter);
        addMouseMotionListener(motionAdapter);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (manager == null) return;
        Track track = manager.getTrack();

        // Save default transformation matrix
        AffineTransform oldTransform = g2d.getTransform();

        // Apply custom panning and zoom factor transformations
        g2d.translate(offsetX, offsetY);
        g2d.scale(zoomFactor, zoomFactor);

        // 1. Draw track floor and boundaries
        track.draw(g2d);

        // 2. If in Editor Mode, draw waypoint handles and skip drawing cars
        if (editorMode) {
            List<Point2D.Double> basePoints = track.getBasePoints();
            g2d.setStroke(new BasicStroke(1.5f));
            for (int i = 0; i < basePoints.size(); i++) {
                Point2D.Double pt = basePoints.get(i);
                if (i == 0) {
                    g2d.setColor(new Color(249, 226, 175)); // Gold for start point
                } else {
                    g2d.setColor(new Color(250, 179, 135)); // Peach/Orange for other waypoints
                }
                g2d.fillOval((int)(pt.x - waypointRadius), (int)(pt.y - waypointRadius), waypointRadius * 2, waypointRadius * 2);
                g2d.setColor(Color.WHITE);
                g2d.drawOval((int)(pt.x - waypointRadius), (int)(pt.y - waypointRadius), waypointRadius * 2, waypointRadius * 2);

                // Draw waypoint numbering
                g2d.setFont(new Font("Segoe UI", Font.BOLD, 10));
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(i), (int)pt.x - 3, (int)pt.y - 10);
            }
            // Restore default transform and return
            g2d.setTransform(oldTransform);
            return;
        }

        if (humanMode) {
            // Render manual play
            if (humanCar != null && humanCar.isAlive()) {
                drawSensorRays(g2d, humanCar, sensorNormalA, sensorWarningA);
                drawCar(g2d, humanCar, carHumanColor);
            }
        } else {
            // Render AI populations
            List<Car> popA = manager.getPopulationA();
            List<Car> popB = manager.getPopulationB();

            // Find current best alive car for Pop A
            Car bestA = null;
            for (Car car : popA) {
                if (car.isAlive()) {
                    if (bestA == null || car.getFitness() > bestA.getFitness()) {
                        bestA = car;
                    }
                }
            }
            if (bestA == null && !popA.isEmpty()) {
                bestA = popA.get(0);
            }

            // Find current best alive car for Pop B
            Car bestB = null;
            for (Car car : popB) {
                if (car.isAlive()) {
                    if (bestB == null || car.getFitness() > bestB.getFitness()) {
                        bestB = car;
                    }
                }
            }
            if (bestB == null && !popB.isEmpty()) {
                bestB = popB.get(0);
            }

            // 3. Draw Sensors for best alive leaders
            if (bestA != null && bestA.isAlive()) {
                drawSensorRays(g2d, bestA, sensorNormalA, sensorWarningA);
            }
            if (bestB != null && bestB.isAlive()) {
                drawSensorRays(g2d, bestB, sensorNormalB, sensorWarningB);
            }

            // 4. Draw Swarms (A in translucent Cyan, B in translucent Pink)
            if (showAll) {
                for (Car car : popA) {
                    if (car.isAlive() && car != bestA) {
                        drawCar(g2d, car, carSwarmA);
                    }
                }
                for (Car car : popB) {
                    if (car.isAlive() && car != bestB) {
                        drawCar(g2d, car, carSwarmB);
                    }
                }
            }

            // 5. Draw Best Cars (A in solid Blue, B in solid Pink)
            if (bestA != null && bestA.isAlive()) {
                drawCar(g2d, bestA, carBestA);
            }
            if (bestB != null && bestB.isAlive()) {
                drawCar(g2d, bestB, carBestB);
            }
        }

        // Restore default transformation matrix
        g2d.setTransform(oldTransform);
    }

    private void drawCar(Graphics2D g2d, Car car, Color bodyColor) {
        double cx = car.getX();
        double cy = car.getY();
        double theta = car.getTheta();
        double w = car.getWidth();
        double h = car.getHeight();

        AffineTransform old = g2d.getTransform();

        g2d.translate(cx, cy);
        g2d.rotate(theta);

        g2d.setColor(bodyColor);
        g2d.fillRect((int)(-w / 2.0), (int)(-h / 2.0), (int)w, (int)h);

        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1.0f));
        g2d.drawRect((int)(-w / 2.0), (int)(-h / 2.0), (int)w, (int)h);

        g2d.setColor(new Color(30, 30, 46, 200));
        g2d.fillRect((int)(w * 0.15), (int)(-h * 0.35), (int)(w * 0.2), (int)(h * 0.7));

        g2d.setColor(new Color(249, 226, 175));
        g2d.fillRect((int)(w / 2.0 - 2), (int)(-h / 2.0 + 1), 2, 2);
        g2d.fillRect((int)(w / 2.0 - 2), (int)(h / 2.0 - 3), 2, 2);

        g2d.setColor(Color.BLACK);
        g2d.fillRect((int)(-w * 0.4), (int)(-h / 2.0 - 2), (int)(w * 0.25), 2);
        g2d.fillRect((int)(w * 0.15), (int)(-h / 2.0 - 2), (int)(w * 0.25), 2);
        g2d.fillRect((int)(-w * 0.4), (int)(h / 2.0), (int)(w * 0.25), 2);
        g2d.fillRect((int)(w * 0.15), (int)(h / 2.0), (int)(w * 0.25), 2);

        g2d.setTransform(old);
    }

    private void drawSensorRays(Graphics2D g2d, Car car, Color normalColor, Color warningColor) {
        Point2D.Double[] points = car.getSensorPoints();
        double[] readings = car.getSensorReadings();
        double cx = car.getX();
        double cy = car.getY();

        g2d.setStroke(new BasicStroke(1.2f));

        for (int i = 0; i < car.getNumSensors(); i++) {
            Point2D.Double p = points[i];
            if (p == null) continue;

            if (readings[i] < 0.25) {
                g2d.setColor(warningColor);
            } else {
                g2d.setColor(normalColor);
            }

            g2d.draw(new Line2D.Double(cx, cy, p.x, p.y));

            if (readings[i] < 1.0) {
                g2d.setColor(Color.WHITE);
                g2d.fillOval((int) (p.x - 2), (int) (p.y - 2), 4, 4);
            }
        }
    }
}
