import database.DatabaseManager;
import logic_services.ServiceManager;
import report_view_logic.ReportManager;
import menu_view_views.LogicMenu;
import project_models.*;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // ── Base de datos ─────────────────────────────────
        DatabaseManager db = new DatabaseManager();
        try {
            db.conectar();
            db.crearTablas();
        } catch (SQLException e) {
            System.err.println("[DB] Error al iniciar la base de datos: " + e.getMessage());
        }
        // ─────────────────────────────────────────────────

        ReportManager reportManager = new ReportManager();
        ConsoleMessenger messenger = new ConsoleMessenger();
        ServiceManager serviceManager = new ServiceManager(reportManager, messenger);

        precarregarFlota(serviceManager, db);

        System.out.println("\n\n");
        System.out.println("╔==================================================╗");
        System.out.println("║                                                  ║");
        System.out.println("║          PROGRAM OF TAXI MANAGEMENT              ║");
        System.out.println("║                                                  ║");
        System.out.println("║                    SCRUM II                      ║");
        System.out.println("║                                                  ║");
        System.out.println("╚==================================================╝");
        System.out.println("\n           Press[ENTER] to start... ");
        sc.nextLine();

        int opcion = 0;
        boolean continuar = true;

        System.out.println("=== Welcome to the BCN Taxi Management System ===");

        while (continuar) {
            System.out.print(LogicMenu.imprimirOpciones());

            try {
                opcion = Integer.parseInt(sc.nextLine());

                switch (opcion) {
                    case 1 -> registrarNouServei(serviceManager, sc, db);
                    case 2 -> gestionarArribadaTaxi(serviceManager, sc);
                    case 3 -> finalitzarServeiActiu(serviceManager, sc);
                    case 4 -> mostrarEstatSistema(serviceManager, reportManager);
                    case 5 -> {
                        System.out.println("Leaving the system... Have a nice trip!");
                        db.desconectar();
                        continuar = false;
                    }
                    default -> System.out.println("Invalid option, choose from 1 to 5");
                }

            } catch (NumberFormatException e) {
                System.out.println("ERROR: Please enter a valid number (5 to exit)");
            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
            }
        }
    }

    private static void precarregarFlota(ServiceManager sm, DatabaseManager db) {
        Driver conductor1 = new Driver("Mario", "DAM", 18, "12345678X", "TX-999");
        Driver conductor2 = new Driver("Luigi", "ASIX", 58, "77777777A", "TX-999");

        Taxi t1 = new Taxi("B-1234-BCN", "Negre/Groc", 4, conductor1, new Position(0, 0), TaxiType.STANDARD);
        Taxi t2 = new Taxi("B-5555-APP", "Negre/Groc", 6, conductor2, new Position(5, 5), TaxiType.ADAPTED);

        sm.getTaxis().add(t1);
        sm.getTaxis().add(t2);

        try {
            db.insertarTaxi(t1);
            db.insertarTaxi(t2);
        } catch (Exception e) {
            System.err.println("[DB] Error al guardar la flota: " + e.getMessage());
        }

        System.out.println("[INFO] Fleet initialized with 2 vehicles (Lenovo ThinkCentre Server OK)");
    }

    /**
     * Captura les dades del client i la posició per crear una sol·licitud
     */
    private static void registrarNouServei(ServiceManager sm, Scanner sc, DatabaseManager db) {
        System.out.println("\n--- New Service Registration ---");
        String nom = campoObligatorio(sc, "Name: ");
        String lastname = campoObligatorio(sc, "Lastname: ");
        String dni = campoObligatorio(sc, "DNI: ");

        System.out.print("Location row: ");
        int row = Integer.parseInt(sc.nextLine());
        System.out.print("Location column: ");
        int col = Integer.parseInt(sc.nextLine());

        System.out.print("Taxi type (1: Standard, 2: Adapted): ");
        int tipusIn = Integer.parseInt(sc.nextLine());
        TaxiType tipus = (tipusIn == 2) ? TaxiType.ADAPTED : TaxiType.STANDARD;

        Customer client = new Customer(nom, lastname, 20, dni, "600000000");
        ServiceRequest peticio = new ServiceRequest((int)(Math.random()*1000), client, new Position(row, col), tipus);

        sm.registeredService(peticio);

        try {
            db.insertarServicio(peticio);
        } catch (Exception e) {
            System.err.println("[DB] Error al guardar el servicio: " + e.getMessage());
        }
    }

    private static void finalitzarServeiActiu(ServiceManager sm, Scanner sc) {
        // 1. Validació preventiva: Si no hi ha serveis, no cal demanar dades
        if (sm.getActiveServices().isEmpty()) {
            System.out.println("There are no active services to end");
            return;
        }

        try {
            System.out.println("\n--- End service ---");
            System.out.println("Choose the service code to end: ");
            for (ServiceRequest s : sm.getActiveServices()) {
                System.out.println("- ID: " + s.getServiceCode() + " | Customer: " + s.getCustomer().getFirstName());
            }

            System.out.print("Code ID: ");
            int idCerca = Integer.parseInt(sc.nextLine());

            ServiceRequest serveiTrobat = null;
            for (ServiceRequest s : sm.getActiveServices()) {
                if (s.getServiceCode() == idCerca) {
                    serveiTrobat = s;
                    break;
                }
            }

            if (serveiTrobat != null) {
                System.out.print("End destination row: ");
                int fFi = Integer.parseInt(sc.nextLine());
                System.out.print("End destination column: ");
                int cFi = Integer.parseInt(sc.nextLine());

                Position posicioFinal = new Position(fFi, cFi);

                sm.endService(serveiTrobat, posicioFinal);

            } else {
                System.out.println("ERROR: No service found with the ID: " + idCerca);
            }

        } catch (NumberFormatException e) {
            System.out.println("ERROR: Invalid input. Please enter a number.");
        }
    }

    private static void mostrarEstatSistema(ServiceManager sm, ReportManager rm) {
        System.out.println("\n--- CURRENT SYSTEM STATUS ---");
        System.out.println("Active service: " + sm.getActiveServices().size());
        System.out.println("Waiting list: " + sm.getWaitingList().size());

        System.out.println("\nGenerating Statistical Report...");
        rm.printReport(sm.getActiveServices(), sm.getWaitingList());
    }

    /**
     * Mètode d'utilitat per assegurar que no ens passen Strings buits.
     */
    private static String campoObligatorio(Scanner sc, String mensaje) {
        String valor;
        do {
            System.out.print(mensaje);
            valor = sc.nextLine().trim();
            if (valor.isEmpty()) System.out.println("This fields is required");
        } while (valor.isEmpty());
        return valor;
    }

    private static void gestionarArribadaTaxi(ServiceManager sm, Scanner sc) {
        System.out.println("\n--- Mark the arrival of the taxi ---");

        // Validem si hi ha feina a fer abans de demanar dades
        if (sm.getActiveServices().isEmpty()) {
            System.out.println("There are no active services to manage");
            return;
        }

        try {
            System.out.println("Current road services: ");
            for (ServiceRequest s : sm.getActiveServices()) {
                System.out.println("- ID: " + s.getServiceCode() + " | Customer: " + s.getCustomer().getFirstName());
            }

            System.out.print("Enter the service ID where the taxi arrived: ");
            int idArribada = Integer.parseInt(sc.nextLine());

            sm.marcarArribada(idArribada);

        } catch (NumberFormatException e) {
            System.out.println("Error: The ID entered is not a valid numeric format");
        }
    }

}