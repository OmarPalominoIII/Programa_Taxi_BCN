package manager;

import dao.impl.ServiceRequestImplDAO;
import models.*;

import java.time.Duration;
import java.util.ArrayList;

/**
 * Handles report generation and statistical calculations
 * over the list of attended (completed) service requests.
 */

import java.time.LocalDateTime;
import java.sql.SQLException;

public class ReportManager {

    private static final double AVERAGE_TAXI_SPEED_KMH = 35.0;


    private static final int EARTH_RADIUS_KM = 6371;

    private ArrayList<ServiceRequest> attendedServices;
    private ServiceRequestImplDAO serviceRequestsDAO;

    public ReportManager(ServiceRequestImplDAO serviceRequestsDAO) {
        this.serviceRequestsDAO = serviceRequestsDAO;
        this.attendedServices = new ArrayList<>();
        try {
            this.attendedServices = serviceRequestsDAO.findByStatus(ServiceStatus.COMPLETED);
        } catch (SQLException e) {
            System.err.println("[ReportManager] Error al cargar histórico: " + e.getMessage());
            this.attendedServices = new ArrayList<>();
        }
    }

    public ArrayList<ServiceRequest> getAttendedServices() {
        return attendedServices;
    }

    public void addAttendedService(ServiceRequest serviceRequest) {
        if (serviceRequest.getServiceStatus() == ServiceStatus.COMPLETED){
            attendedServices.add(serviceRequest);
        } else {
            System.err.println("Service request has not been completed, cannot be added");
        }
    }

    private double calculateHaversineMinutes(Position from, Position to) {
        double latRad1 = Math.toRadians(from.getLatitude());
        double latRad2 = Math.toRadians(to.getLatitude());
        double deltaLat = Math.toRadians(from.getLatitude() - to.getLatitude());
        double deltaLon = Math.toRadians(from.getLongitude() - to.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(latRad1) * Math.cos(latRad2) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // Distancia real en kilómetros en línea recta sobre la superficie terrestre
        double distanceKm = EARTH_RADIUS_KM * c;

        // Tiempo en horas = Distancia / Velocidad.
        // Multiplicamos por 60 para transformarlo directamente a minutos reales.
        return (distanceKm / AVERAGE_TAXI_SPEED_KMH) * 60;
    }

    // Calcula el tiempo estimado que tardará un taxi específico en llegar al cliente
    public double calculateSingleServiceRouteTime(Taxi taxi, ServiceRequest request){
        return calculateHaversineMinutes(taxi.getPosition(), request.getCustomerPosition());
    }

    // Tiempo promedio calculado a través de la distancia Haversine de todos los viajes completados
    public double calculateAverageEstimatedRoutedTime(){
        if (attendedServices.isEmpty()){
            System.out.println("No attended services to calculate.");
            return 0;
        }

        double totalMinutes = 0;
        int count = 0;

        for (ServiceRequest serviceRequest : attendedServices){
            if (serviceRequest.getTaxi() != null && serviceRequest.getCustomerPosition() != null && serviceRequest.getTaxi().getPosition() != null){
                totalMinutes += calculateHaversineMinutes(serviceRequest.getTaxi().getPosition(), serviceRequest.getCustomerPosition());
                count++;
            }
        }
        double average = count > 0 ? totalMinutes / count : 0;
        System.out.println("\n  Average estimated route time: " + average + " minutes (over " + count + " services)\n");
        return average;
    }

    // --- El resto de tus métodos de análisis de tiempos se mantienen intactos ---

    public double calculateAverageWaitingTime(){
        if (attendedServices.isEmpty()){
            System.out.println("[ReportManager] No attended services to calculate.");
            return 0;
        }
        long totalMinutes = 0;
        int count = 0;

        for (ServiceRequest serviceRequest : attendedServices){
            if (serviceRequest.getRequestTime() != null){
                long minutes = Duration.between(serviceRequest.getRequestTime(), LocalDateTime.now()).toMinutes();
                totalMinutes += Math.abs(minutes);
                count++;
            }
        }
        double average = count > 0 ? (double) totalMinutes / count : 0;
        System.out.println("\n  Average waiting time: " + average + " minutes (over " + count + " services)\n");
        return average;
    }

    public double calculateAverageArrivalTime(){
        if (attendedServices.isEmpty()){
            System.out.println("No attended services to calculate.");
            return 0;
        }
        long totalMinutes = 0;
        int count = 0;

        for (ServiceRequest serviceRequest : attendedServices){
            if(serviceRequest.getTaxi() != null && serviceRequest.getRequestTime() != null){
                long minutes = Duration.between(serviceRequest.getRequestTime(), LocalDateTime.now()).toMinutes();
                totalMinutes += Math.abs(minutes);
                count ++;
            }
        }
        double average = count > 0 ? (double) totalMinutes / count : 0;
        System.out.println("\n  Average arrival time: " + average + " minutes (over " + count + " services)\n");
        return average;
    }

    public void classifyServices(ArrayList<ServiceRequest> activeServices, ArrayList<ServiceRequest> waitingList){
        System.out.println("\n========== SERVICE CLASSIFICATION ==========");
        System.out.println("COMPLETED: " + attendedServices.size());
        for (ServiceRequest sr : attendedServices) {
            System.out.println(" " +  sr.toString());
        }
        System.out.println("IN_PROGRESS: " + activeServices.size());
        for (ServiceRequest sr : activeServices) {
            System.out.println(" " + sr.toString());
        }
        System.out.println("PENDING    : " + waitingList.size());
        for (ServiceRequest sr : waitingList) {
            System.out.println(" " + sr.toString());
        }
        System.out.println("============================================");
    }

    public void printReport(ArrayList<ServiceRequest> actives, ArrayList<ServiceRequest> waiting) {
        int total = actives.size() + waiting.size() + attendedServices.size();

        System.out.println("\n");
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║          TAXIS BCN - FULL REPORT         ║");
        System.out.println("  ╠══════════════════════════════════════════╣");
        System.out.printf("   ║  Total services recorded: %-14d ║%n", total);
        System.out.println("  ╚══════════════════════════════════════════╝");

        classifyServices(actives, waiting);

        double avgWaiting = calculateAverageWaitingTime();
        double avgRoute = calculateAverageEstimatedRoutedTime();
        double avgArrival = calculateAverageArrivalTime();

        System.out.println("\n  --- SUMMARY ---");
        System.out.println("  Avg. waiting list time  : " + avgWaiting + " min");
        System.out.println("  Avg. estimated route    : " + avgRoute + " min");
        System.out.println("  Avg. arrival time       : " + avgArrival + " min");
        System.out.println("  ----------------");
    }
}
