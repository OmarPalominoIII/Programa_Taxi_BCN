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
            System.err.println("Service request has not been completed");
        }
    }

    //En esta parte clasificamos y hacemos una lista del estado de cada servicio
    public void classifyServices(){
        ArrayList<ServiceRequest> completed = new ArrayList<>();
        ArrayList<ServiceRequest> inProgress = new ArrayList<>();
        ArrayList<ServiceRequest> pending = new ArrayList<>();

        for (ServiceRequest serviceRequest : attendedServices){
            switch (serviceRequest.getServiceStatus()){
                case COMPLETED:
                    completed.add(serviceRequest);
                    break;

                case IN_PROGRESS:
                    inProgress.add(serviceRequest);
                    break;

                case PENDING:
                    pending.add(serviceRequest);
                    break;

            }
        }
        System.out.println("\n========== SERVICE CLASSIFICATION ==========");
        System.out.println("COMPLETED: " + completed.size());
        for (ServiceRequest serviceRequest : completed){
            System.out.println(serviceRequest.toString());
        }
        System.out.println("INPROGRESS: " + inProgress.size());
        for (ServiceRequest serviceRequest : inProgress){
            System.out.println(serviceRequest.toString());
        }
        System.out.println("PENDING: " + pending.size());
        for (ServiceRequest serviceRequest : pending){
            System.out.println(serviceRequest.toString());
        }

        System.out.println("==================================");


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

    public double calculateSingleServiceRouteTime(Taxi taxi, ServiceRequest request){
        int rowdiff = Math.abs(taxi.getPosition().getRow() - request.getCustomerPosition().getRow());
        int coldiff = Math.abs(taxi.getPosition().getColumn() - request.getCustomerPosition().getColumn());

        return (double) (rowdiff + coldiff) * 2;
    }


    //En la siguiente clase calculamos el tiempo de ruta que tardara el taxi en hacer el recorrido
    //Utilizamos distancia de Manhattan y asumimos que por cada bloque tarde 2 minutos en recorrerlo

    public double calculateAverageEstimatedRoutedTime(){
        if (attendedServices.isEmpty()){
            System.out.println("No attended services to calculate.");
            return 0;
        }

        final int minutesPerBlock = 2;
        long totalMinutes = 0;
        int count = 0;

        for (ServiceRequest serviceRequest : attendedServices){
            if (serviceRequest.getTaxi() != null && serviceRequest.getCustomerPosition() != null &&
                    serviceRequest.getTaxi().getPosition() != null){

                /* Calculamos la diferencia de filas que hay entre la ubicación del taxi y la ubicación del Cliente
                El math.abs lo ponemos para que el valor que salga siempre lo imprima en
                positivo debido a que una distancia no puede ser negativa
                 */

                int rowDiff = Math.abs(serviceRequest.getTaxi().getPosition().getRow() - serviceRequest.getCustomerPosition().getRow());
                int colDiff = Math.abs(serviceRequest.getTaxi().getPosition().getColumn() - serviceRequest.getCustomerPosition().getColumn());

                totalMinutes += (long)(rowDiff + colDiff) *  minutesPerBlock;
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
    public void printReport() {
        System.out.println("\n");
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║          TAXIS BCN - FULL REPORT         ║");
        System.out.println("  ╠══════════════════════════════════════════╣");
        System.out.printf("   ║  Total services recorded:                ║%n", attendedServices.size());
        System.out.println("  ╚══════════════════════════════════════════╝");

        classifyServices();

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
