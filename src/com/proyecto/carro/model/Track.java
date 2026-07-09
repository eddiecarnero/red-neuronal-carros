package com.proyecto.carro.model;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Track {
    private final Polygon outerWall;
    private final Polygon innerWall;

    private final List<Line2D.Double> wallSegments;
    private final List<Line2D.Double> checkpoints;
    private final List<Point2D.Double> centerLine;

    private final int trackType; // 0: Óvalo, 1: Sinuoso, 2: Grand Prix, 3: Laberinto, 4: Personalizado
    private final List<Point2D.Double> basePoints;

    private final double centerX = 225.0;
    private final double centerY = 225.0;

    public Track(int trackType) {
        this.trackType = trackType;
        this.basePoints = new ArrayList<>();
        outerWall = new Polygon();
        innerWall = new Polygon();
        wallSegments = new ArrayList<>();
        checkpoints = new ArrayList<>();
        centerLine = new ArrayList<>();

        generateSplineTrack();
    }

    public List<Point2D.Double> getBasePoints() {
        return basePoints;
    }

    public void regenerate() {
        outerWall.reset();
        innerWall.reset();
        wallSegments.clear();
        checkpoints.clear();
        centerLine.clear();
        generateSplineTrack();
    }

    public static void saveCustomWaypoints(List<Point2D.Double> points) {
        try (PrintWriter pw = new PrintWriter(new FileWriter("custom_track.txt"))) {
            for (Point2D.Double p : points) {
                pw.println(p.x + "," + p.y);
            }
        } catch (IOException e) {
            System.err.println("Error al guardar los puntos de control personalizados: " + e.getMessage());
        }
    }

    public static List<Point2D.Double> loadCustomWaypoints() {
        List<Point2D.Double> points = new ArrayList<>();
        File file = new File("custom_track.txt");
        if (file.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        double x = Double.parseDouble(parts[0]);
                        double y = Double.parseDouble(parts[1]);
                        points.add(new Point2D.Double(x, y));
                    }
                }
            } catch (Exception e) {
                System.err.println("Error al cargar los puntos de control personalizados: " + e.getMessage());
            }
        }
        return points;
    }

    private void generateSplineTrack() {
        // Si basePoints ya está cargado (desde el editor en memoria), usarlo;
        // de lo contrario, cargar según trackType
        if (basePoints.isEmpty()) {
            switch (trackType) {
                case 0: // Óvalo Clásico (Fácil)
                    basePoints.add(new Point2D.Double(350, 120));
                    basePoints.add(new Point2D.Double(380, 225));
                    basePoints.add(new Point2D.Double(350, 330));
                    basePoints.add(new Point2D.Double(225, 350));
                    basePoints.add(new Point2D.Double(100, 330));
                    basePoints.add(new Point2D.Double(70, 225));
                    basePoints.add(new Point2D.Double(100, 120));
                    basePoints.add(new Point2D.Double(225, 100));
                    break;

                case 1: // Circuito Sinuoso (Medio)
                    int pts = 12;
                    for (int i = 0; i < pts; i++) {
                        double angle = i * (2.0 * Math.PI / pts);
                        double r = 145.0 + 25.0 * Math.sin(3.0 * angle);
                        basePoints.add(new Point2D.Double(centerX + r * Math.cos(angle), centerY + r * Math.sin(angle)));
                    }
                    break;

                case 2: // Grand Prix F1 (Difícil)
                    basePoints.add(new Point2D.Double(370, 90));   // Top Right loop
                    basePoints.add(new Point2D.Double(380, 200));  // Right straight down
                    basePoints.add(new Point2D.Double(290, 240));  // Inward turn (chicane start)
                    basePoints.add(new Point2D.Double(340, 295));  // Chicane quick right
                    basePoints.add(new Point2D.Double(380, 370));  // Bottom Right corner
                    basePoints.add(new Point2D.Double(220, 390));  // Bottom straightaway
                    basePoints.add(new Point2D.Double(170, 290));  // Internal winding hook
                    basePoints.add(new Point2D.Double(120, 290));  // Sharp hairpin turn
                    basePoints.add(new Point2D.Double(80, 370));   // Bottom Left corner
                    basePoints.add(new Point2D.Double(60, 220));   // Left wide sweep up
                    basePoints.add(new Point2D.Double(140, 160));  // Internal S-bend
                    basePoints.add(new Point2D.Double(80, 90));    // Top Left corner
                    basePoints.add(new Point2D.Double(220, 70));   // Top straightaway
                    break;

                case 3: // El Laberinto (Extremo - Zig-zag angular)
                    basePoints.add(new Point2D.Double(370, 90));
                    basePoints.add(new Point2D.Double(370, 180));
                    basePoints.add(new Point2D.Double(200, 180));
                    basePoints.add(new Point2D.Double(200, 270));
                    basePoints.add(new Point2D.Double(370, 270));
                    basePoints.add(new Point2D.Double(370, 360));
                    basePoints.add(new Point2D.Double(80, 360));
                    basePoints.add(new Point2D.Double(80, 90));
                    break;

                case 4: // Circuito Personalizado (Custom)
                    List<Point2D.Double> loaded = loadCustomWaypoints();
                    if (!loaded.isEmpty()) {
                        basePoints.addAll(loaded);
                    } else {
                        // Usar óvalo por defecto si no existe el archivo aún
                        basePoints.add(new Point2D.Double(350, 120));
                        basePoints.add(new Point2D.Double(380, 225));
                        basePoints.add(new Point2D.Double(350, 330));
                        basePoints.add(new Point2D.Double(225, 350));
                        basePoints.add(new Point2D.Double(100, 330));
                        basePoints.add(new Point2D.Double(70, 225));
                        basePoints.add(new Point2D.Double(100, 120));
                        basePoints.add(new Point2D.Double(225, 100));
                    }
                    break;

                default: // Usar óvalo por defecto
                    basePoints.add(new Point2D.Double(350, 120));
                    basePoints.add(new Point2D.Double(380, 225));
                    basePoints.add(new Point2D.Double(350, 330));
                    basePoints.add(new Point2D.Double(225, 350));
                    basePoints.add(new Point2D.Double(100, 330));
                    basePoints.add(new Point2D.Double(70, 225));
                    basePoints.add(new Point2D.Double(100, 120));
                    basePoints.add(new Point2D.Double(225, 100));
                    break;
            }
        }

        int N = basePoints.size();
        int stepsPerSegment = 10;

        // 1. Interpolar puntos de control usando Catmull-Rom
        for (int i = 0; i < N; i++) {
            Point2D.Double p0 = basePoints.get((i - 1 + N) % N);
            Point2D.Double p1 = basePoints.get(i);
            Point2D.Double p2 = basePoints.get((i + 1) % N);
            Point2D.Double p3 = basePoints.get((i + 2) % N);

            for (int step = 0; step < stepsPerSegment; step++) {
                double t = (double) step / stepsPerSegment;
                centerLine.add(getCatmullRomPoint(p0, p1, p2, p3, t));
            }
        }

        // 2. Generar límites interiores y exteriores usando curvas de desplazamiento
        List<Point2D.Double> outerPoints = new ArrayList<>();
        List<Point2D.Double> innerPoints = new ArrayList<>();
        int numCenterPoints = centerLine.size();
        
        // Pista ligeramente más ancha para Óvalo/Sinuoso para facilitar, más estrecha para GP/Laberinto
        double halfWidth = (trackType <= 1) ? 30.0 : 27.5; 

        for (int i = 0; i < numCenterPoints; i++) {
            Point2D.Double curr = centerLine.get(i);
            Point2D.Double next = centerLine.get((i + 1) % numCenterPoints);
            Point2D.Double prev = centerLine.get((i - 1 + numCenterPoints) % numCenterPoints);

            double tx = next.x - prev.x;
            double ty = next.y - prev.y;
            double len = Math.sqrt(tx * tx + ty * ty);
            if (len > 0) {
                tx /= len;
                ty /= len;
            }

            double nx = -ty;
            double ny = tx;

            double ox = curr.x + halfWidth * nx;
            double oy = curr.y + halfWidth * ny;
            double ix = curr.x - halfWidth * nx;
            double iy = curr.y - halfWidth * ny;

            outerPoints.add(new Point2D.Double(ox, oy));
            innerPoints.add(new Point2D.Double(ix, iy));

            outerWall.addPoint((int) ox, (int) oy);
            innerWall.addPoint((int) ix, (int) iy);
        }

        // 3. Descomponer límites en segmentos Line2D
        for (int i = 0; i < numCenterPoints; i++) {
            Point2D.Double oCurr = outerPoints.get(i);
            Point2D.Double oNext = outerPoints.get((i + 1) % numCenterPoints);
            wallSegments.add(new Line2D.Double(oCurr.x, oCurr.y, oNext.x, oNext.y));

            Point2D.Double iCurr = innerPoints.get(i);
            Point2D.Double iNext = innerPoints.get((i + 1) % numCenterPoints);
            wallSegments.add(new Line2D.Double(iCurr.x, iCurr.y, iNext.x, iNext.y));
        }

        // 4. Generar checkpoints (20 para todos los tipos de pista)
        int numCheckpoints = 20;
        int step = numCenterPoints / numCheckpoints;
        for (int j = 0; j < numCheckpoints; j++) {
            int index = j * step;
            if (index < innerPoints.size() && index < outerPoints.size()) {
                Point2D.Double ip = innerPoints.get(index);
                Point2D.Double op = outerPoints.get(index);
                checkpoints.add(new Line2D.Double(ip.x, ip.y, op.x, op.y));
            }
        }
    }

    private Point2D.Double getCatmullRomPoint(Point2D.Double p0, Point2D.Double p1, Point2D.Double p2, Point2D.Double p3, double t) {
        double t2 = t * t;
        double t3 = t2 * t;

        double x = 0.5 * ((2.0 * p1.x) +
                (-p0.x + p2.x) * t +
                (2.0 * p0.x - 5.0 * p1.x + 4.0 * p2.x - p3.x) * t2 +
                (-p0.x + 3.0 * p1.x - 3.0 * p2.x + p3.x) * t3);

        double y = 0.5 * ((2.0 * p1.y) +
                (-p0.y + p2.y) * t +
                (2.0 * p0.y - 5.0 * p1.y + 4.0 * p2.y - p3.y) * t2 +
                (-p0.y + 3.0 * p1.y - 3.0 * p2.y + p3.y) * t3);

        return new Point2D.Double(x, y);
    }

    public Point2D.Double getStartPoint() {
        if (centerLine.isEmpty()) {
            return new Point2D.Double(centerX, centerY);
        }
        return centerLine.get(0);
    }

    public double getStartAngle() {
        if (centerLine.size() < 2) {
            return 0.0;
        }
        Point2D.Double p1 = centerLine.get(0);
        Point2D.Double p2 = centerLine.get(1);
        return Math.atan2(p2.y - p1.y, p2.x - p1.x);
    }

    public Polygon getOuterWall() {
        return outerWall;
    }

    public Polygon getInnerWall() {
        return innerWall;
    }

    public List<Line2D.Double> getWallSegments() {
        return wallSegments;
    }

    public List<Line2D.Double> getCheckpoints() {
        return checkpoints;
    }

    public void draw(Graphics2D g2d) {
        Color trackFloorColor = new Color(49, 50, 68);     // Surface0 (dark lane)
        Color wallColor = new Color(166, 173, 200);        // Subtext0 (light walls)
        Color checkpointColor = new Color(166, 227, 161, 40); // Translucent green

        double halfWidth = (trackType <= 1) ? 30.0 : 27.5;

        // 1. Pintar la pista
        g2d.setColor(trackFloorColor);
        g2d.setStroke(new BasicStroke((float) (halfWidth * 2), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        
        java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
        if (!centerLine.isEmpty()) {
            path.moveTo(centerLine.get(0).x, centerLine.get(0).y);
            for (int i = 1; i < centerLine.size(); i++) {
                path.lineTo(centerLine.get(i).x, centerLine.get(i).y);
            }
            path.closePath();
            g2d.draw(path);
        }

        // 2. Dibujar muros de contención
        g2d.setColor(wallColor);
        g2d.setStroke(new BasicStroke(2.5f));
        for (Line2D.Double wall : wallSegments) {
            g2d.draw(wall);
        }

        // 3. Dibujar checkpoints
        g2d.setStroke(new BasicStroke(1.5f));
        for (int i = 0; i < checkpoints.size(); i++) {
            Line2D.Double cp = checkpoints.get(i);
            if (i == 0) {
                g2d.setColor(new Color(249, 226, 175, 120)); // Línea de salida dorada
            } else {
                g2d.setColor(checkpointColor);
            }
            g2d.draw(cp);

            double mx = (cp.x1 + cp.x2) / 2.0;
            double my = (cp.y1 + cp.y2) / 2.0;
            g2d.setColor(new Color(166, 173, 200, 100));
            g2d.setFont(new Font("Segoe UI", Font.PLAIN, 8));
            g2d.drawString(String.valueOf(i), (int)mx - 3, (int)my + 3);
        }
    }
}
