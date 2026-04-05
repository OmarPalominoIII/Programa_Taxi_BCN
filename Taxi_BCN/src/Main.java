import logic_services.ServiceManager;
import report_view_logic.ReportManager;
import menu_view_views.LogicMenu;
import project_models.*;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        ReportManager reportManager = new ReportManager();
        ConsoleMessenger messenger = new ConsoleMessenger();
        ServiceManager serviceManager = new ServiceManager(reportManager, messenger);

        precarregarFlota(serviceManager);

        int opcion = 0;
        boolean continuar = true;

        System.out.println("=== Benvingut al Sistema de Gestió Taxi BCN ===");

        while (continuar) {
            System.out.print(LogicMenu.imprimirOpciones());

            try {
                opcion = Integer.parseInt(sc.nextLine());

                switch (opcion) {
                    case 1 -> registrarNouServei(serviceManager, sc);
                    case 2 -> gestionarArribadaTaxi(serviceManager, sc);
                    case 3 -> finalitzarServeiActiu(serviceManager, sc);
                    case 4 -> mostrarEstatSistema(serviceManager, reportManager);
                    case 5 -> {
                        System.out.println("Sortint del sistema... Bona ruta!");
                        continuar = false;
                    }
                    default -> System.out.println("Opció no vàlida, tria del 1 al 5.");
                }

            } catch (NumberFormatException e) {
                System.out.println("ERROR: Si us plau, introdueix un número vàlid (5 per sortir).");
            } catch (Exception e) {
                System.out.println("S'ha produït un error inesperat: " + e.getMessage());
            }
        }
    }

    /**
     * Mètode per omplir el sistema amb dades inicials i poder fer proves ràpides.
     */
    private static void precarregarFlota(ServiceManager sm) {
        Driver conductor1 = new Driver("Mario", "Guerrero", 20, "12345678X", "TX-999");
        Driver conductor2 = new Driver("Luigi", "Luigi", 25, "00000009A", "TX-999");

        Taxi t1 = new Taxi("B-1234-BCN", "Negre/Groc", 4, conductor1, new Position(0, 0), TaxiType.STANDARD);
        Taxi t2 = new Taxi("B-5555-APP", "Negre/Groc", 6, conductor2, new Position(5, 5), TaxiType.ADAPTED);

        sm.getTaxis().add(t1);
        sm.getTaxis().add(t2);

        System.out.println("[INFO] Flota inicialitzada amb 2 vehicles.");
    }

    /**
     * Captura les dades del client i la posició per crear una sol·licitud
     */
    private static void registrarNouServei(ServiceManager sm, Scanner sc) {
        System.out.println("\n--- Registre de Nou Servei ---");
        String nom = campoObligatorio(sc, "Nom del client: ");
        String dni = campoObligatorio(sc, "DNI: ");

        System.out.print("Fila de recollida: ");
        int row = Integer.parseInt(sc.nextLine());
        System.out.print("Columna de recollida: ");
        int col = Integer.parseInt(sc.nextLine());

        System.out.print("Tipus de taxi (1: Standard, 2: Adaptat): ");
        int tipusIn = Integer.parseInt(sc.nextLine());
        TaxiType tipus = (tipusIn == 2) ? TaxiType.ADAPTED : TaxiType.STANDARD;

        Customer client = new Customer(nom, "Usuari", 20, dni, "600000000");
        ServiceRequest peticio = new ServiceRequest((int)(Math.random()*1000), client, new Position(row, col), tipus);

        sm.registeredService(peticio);
    }

    private static void gestionarArribadaTaxi(ServiceManager sm, Scanner sc) {
        System.out.println("\n--- Marcar Arribada del Taxi ---");

        // Validem si hi ha feina a fer abans de demanar dades
        if (sm.getActiveServices().isEmpty()) {
            System.out.println("No hi ha serveis actius per gestionar.");
            return;
        }

        try {
            // Llistem els serveis per facilitar la feina a l'usuari
            System.out.println("Serveis actuals de camí:");
            for (ServiceRequest s : sm.getActiveServices()) {
                // Aprofitem els mètodes getServiceCode i getCustomer que ja tenies
                System.out.println("- ID: " + s.getServiceCode() + " | Client: " + s.getCustomer().getFirstName());
            }

            System.out.print("Introdueix l'ID del servei on el taxi ha arribat: ");
            int idArribada = Integer.parseInt(sc.nextLine());

            // Deleguem la lògica de canvi d'estat al ServiceManager
            sm.marcarArribada(idArribada);

        } catch (NumberFormatException e) {
            System.out.println("Error: L'ID introduït no és un format numèric vàlid.");
        }
    }

    private static void finalitzarServeiActiu(ServiceManager sm, Scanner sc) {
        if (sm.getActiveServices().isEmpty()) {
            System.out.println("No hi ha serveis actius per finalitzar.");
            return;
        }

        System.out.println("Triï el codi del servei a finalitzar:");
        for (ServiceRequest s : sm.getActiveServices()) {
            System.out.println("- ID: " + s.getServiceCode() + " | Client: " + s.getCustomer().getFirstName());
        }

        int id = Integer.parseInt(sc.nextLine());
        System.out.println("Servei " + id + " finalitzat correctament.");
    }

    private static void mostrarEstatSistema(ServiceManager sm, ReportManager rm) {
        System.out.println("\n--- ESTAT ACTUAL DEL SISTEMA ---");
        System.out.println("Serveis Actius: " + sm.getActiveServices().size());
        System.out.println("Llista d'Espera: " + sm.getWaitingList().size());

        System.out.println("\nGenerant Informe Estadístic...");
        rm.printReport();
    }

    /**
     * Mètode d'utilitat per assegurar que no ens passen Strings buits.
     */
    private static String campoObligatorio(Scanner sc, String mensaje) {
        String valor;
        do {
            System.out.print(mensaje);
            valor = sc.nextLine().trim();
            if (valor.isEmpty()) System.out.println("Aquest camp és obligatori.");
        } while (valor.isEmpty());
        return valor;
    }
}