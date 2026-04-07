package report_view_logic;

import project_models.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;

/**
 * Handles report generation and statistical calculations
 * over the list of attended (completed) service requests.
 */

public class ReportManager {

    private static final int MINUTES_PER_BLOCK = 2;

    private ArrayList<ServiceRequest> attendedServices;

    public ReportManager() {
        this.attendedServices = new ArrayList<>();
    }

    public ArrayList<ServiceRequest> getAttendedServices() {
        return attendedServices;
    }

    //Con esto vamos a añadir cualquier servicio que se haya COMPLETADO a una lista

    public void addAttendedService(ServiceRequest serviceRequest) {
        if (serviceRequest.getServiceStatus() == ServiceStatus.COMPLETED){
            attendedServices.add(serviceRequest);
        }
        else {
            System.err.println("Service request has not been completed, cannot be added");
        }
    }

    //En esta parte clasificamos y hacemos una lista del estado de cada servicio

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


    //Voy a calcular el Tiempo Promedio de Espera en una lista de espera
    public double calculateAverageWaitingTime(){
        if (attendedServices.isEmpty()){
            System.out.println("[ReportManager] No attended services to calculate.");
            return 0;
        }
        long totalMinutes = 0;
        int count = 0;

        for (ServiceRequest serviceRequest : attendedServices){
            if (serviceRequest.getRequestTime() != null){
                long minutes = Duration.between(serviceRequest.getRequestTime(), LocalTime.now()).toMinutes();
                totalMinutes += Math.abs(minutes);
                count++;
            }
        }
        double average;
        if (count > 0) {
            average = (double) totalMinutes / count;
        } else {
            average = 0;
        }
        System.out.println("\n  Average waiting time: " + average + " minutes (over " + count + " services)\n");
        return average;
    }

    private double calculateManhattanMinutes(Position from, Position to){
        //Calculamos la diferencia de filas que hay entre la ubicación del taxi y la ubicación del Cliente.
        //El math.abs lo ponemos para que el valor que salga siempre lo imprima en positivo debido a que una distancia no puede ser negativa
        int rowDiff = Math.abs(from.getRow() - to.getRow());
        //Mismo cálculo pero con las columnas
        int colDiff = Math.abs(from.getColumn() - to.getColumn());
        return (double)(rowDiff + colDiff) * MINUTES_PER_BLOCK;
    }

    public double calculateSingleServiceRouteTime(Taxi taxi, ServiceRequest request){
        return calculateManhattanMinutes(taxi.getPosition(), request.getCustomerPosition());
    }

    //En la siguiente clase calculamos el tiempo de ruta que tardara el taxi en hacer el recorrido
    //Utilizamos problema de Manhattan y asumimos que por cada bloque tarde 2 minutos en recorrerlo

    public double calculateAverageEstimatedRoutedTime(){
        if (attendedServices.isEmpty()){
            System.out.println("No attended services to calculate.");
            return 0;
        }

        long totalMinutes = 0;
        int count = 0;

        for (ServiceRequest serviceRequest : attendedServices){
            if (serviceRequest.getTaxi() != null && serviceRequest.getCustomerPosition() != null && serviceRequest.getTaxi().getPosition() != null){

                totalMinutes += (long) calculateManhattanMinutes(serviceRequest.getTaxi().getPosition(), serviceRequest.getCustomerPosition());
                count++;
            }
        }
        double average = count > 0 ? (double) totalMinutes / count : 0;

        System.out.println("\n  Average estimated route time: " + average + " minutes (over " + count + " services)\n");

        return average;
    }

    //Ahora en esta clase se calcula el tiempo promedio que tardara el taxi en llegar al cliente desde que ha sido asignado.
    public double calculateAverageArrivalTime(){
        if (attendedServices.isEmpty()){
            System.out.println("No attended services to calculate.");
            return 0;
        }
        long totalMinutes = 0;
        int count = 0;

        for (ServiceRequest serviceRequest : attendedServices){
            if(serviceRequest.getTaxi() != null && serviceRequest.getRequestTime() != null){
                long minutes = Duration.between(serviceRequest.getRequestTime(), LocalTime.now()).toMinutes();
                totalMinutes += Math.abs(minutes);
                count ++;
            }
        }
        double average;
        if (count > 0) {
            average = (double) totalMinutes / count;
        } else {
            average = 0;
        }
        System.out.println("\n  Average arrival time: " + average + " minutes (over " + count + " services)\n");

        return average;
    }

    //Ahora hacemos el PRINT REPORT
    public void printReport(ArrayList<ServiceRequest> activeServices, ArrayList<ServiceRequest> waitingList) {

        int total = activeServices.size() + waitingList.size() + attendedServices.size();

        System.out.println("\n");
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║          TAXIS BCN - FULL REPORT         ║");
        System.out.println("  ╠══════════════════════════════════════════╣");
        System.out.printf("   ║  Total services recorded: "+ total+"     ║%n");
        System.out.println("  ╚══════════════════════════════════════════╝");

        classifyServices(activeServices, waitingList);

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
