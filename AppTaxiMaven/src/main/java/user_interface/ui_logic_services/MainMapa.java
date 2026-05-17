package user_interface.ui_logic_services;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import javax.swing.*;

import org.jxmapviewer.viewer.*;

import java.util.HashSet;
import java.util.Set;


public class MainMapa {
    public static void main(String[] args) {

        // 1. Indispensable: Identificar tu aplicación
        System.setProperty("http.agent", "MiProyectoTaxis_v1");

        JFrame frame = new JFrame("Mapa de Taxis");
        JXMapViewer mapViewer = new JXMapViewer();

        // 2. Configurar el motor para usar HTTPS manualmente
        TileFactoryInfo info = new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org");
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        // 3. Optimizar la carga (usar varios hilos para descargar las imágenes)
        tileFactory.setThreadPoolSize(8);
        mapViewer.setTileFactory(tileFactory);

        // 1. Crear un conjunto de puntos (Waypoints)
        Set<Waypoint> waypoints = new HashSet<>();

        // 2. Crear una posición (Ejemplo: Plaza Cataluña, Barcelona)
        GeoPosition posTaxi1 = new GeoPosition(41.3870, 2.1700);

        // 3. Crear el Waypoint y añadirlo al conjunto
        waypoints.add(new DefaultWaypoint(posTaxi1));

        // 4. Crear el pintor encargado de dibujar los puntos en el mapa
        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);

        // 5. Asignar el pintor al mapa
        mapViewer.setOverlayPainter(waypointPainter);

        // Centrar en coordenadas (Ejemplo: Barcelona)
        GeoPosition bcn = new GeoPosition(41.3851, 2.1734);
        mapViewer.setAddressLocation(bcn);
        mapViewer.setZoom(5);

        frame.add(mapViewer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}