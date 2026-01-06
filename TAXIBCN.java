import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;

public class TAXIBCN {
    static final int SIZE = 16;
    static class posicion {
        int row;
        int col;
        posicion(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
    static class Taxi {
        String name;
        posicion position;
        Taxi(String name, posicion position) {
            this.name = name;
            this.position = position;
        }
    }
    static class servicio {
        String name;
        posicion position;
        servicio(String name, posicion position) {
            this.name = name;
            this.position = position;
        }
    }
    static class CityMap {
        String[][] map;
        ArrayList<Taxi> taxis;
        ArrayList<servicio> services;
        CityMap() {
            map = new String[SIZE][SIZE];
            taxis = new ArrayList<>();
            services = new ArrayList<>();
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    map[i][j] = ".";
                }
            }
        }
        void anyadirtaxi(Taxi taxi) {
            taxis.add(taxi);
            map[taxi.position.row][taxi.position.col] = taxi.name;
        }
        void anyadirservicio(servicio s) {
            services.add(s);
            map[s.position.row][s.position.col] = s.name;
        }
        void actualizarMapa() {
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    map[i][j] = ".";
                }
            }
            for (Taxi t : taxis) {
                map[t.position.row][t.position.col] = t.name;
            }
            for (servicio s : services) {
                map[s.position.row][s.position.col] = s.name;
            }
        }
        void dibujarmapa() {
            actualizarMapa();
            System.out.println("\nMapa de la ciudad:");
            for (int i = 0; i < SIZE; i++) {
                for (int j = 0; j < SIZE; j++) {
                    System.out.print(String.format("%-6s", map[i][j]));
                }
                System.out.println();
            }
        }
    }
    public static void executar() {
        Scanner sc = new Scanner(System.in);
        int total_taxis = 10;
        int cantidad_taxi_adaptats = total_taxis / 10;
        int cantidad_taxi_normal = total_taxis - cantidad_taxi_adaptats;
        HashMap<Integer, String> disponibilitat_taxi = new HashMap<>();
        String lliure = "lliure";
        String ocupat = "ocupat";
        String descans = "descans";
        for (int i = 0; i < total_taxis; i++) {
            disponibilitat_taxi.put(i, lliure);
        }
        System.out.println("______________________");
        System.out.println("       MENÚ           ");
        System.out.println("  1) TAXI NORMAL      ");
        System.out.println("  2) TAXI ADAPTAT     ");
        System.out.println("______________________");
        double hora = sc.nextInt();
        if (hora < 19) {
            for (int i = 0; i < total_taxis; i++) {
                if (i < (total_taxis / 2) + 1) {
                    disponibilitat_taxi.put(i, lliure);
                } else {
                    disponibilitat_taxi.put(i, descans);
                }
            }
        } else {
            for (int i = 0; i < total_taxis; i++) {
                if (i < (total_taxis / 2) - 1) {
                    disponibilitat_taxi.put(i, descans);
                } else {
                    disponibilitat_taxi.put(i, lliure);
                }
            }
        }
        CityMap city = new CityMap();
        for (int i = 0; i < total_taxis; i++) {
            Taxi t = new Taxi("T" + (i + 1), new posicion(i % SIZE, i % SIZE));
            city.anyadirtaxi(t);
        }
        final int MAX_SERVEIS = 100;
        int[] idServei = new int[MAX_SERVEIS];
        String[] dataHoraInici = new String[MAX_SERVEIS];
        String[] dataHoraArribada = new String[MAX_SERVEIS];
        String[] dataHoraFi = new String[MAX_SERVEIS];
        int[][] coordInici = new int[MAX_SERVEIS][2];
        int[][] coordFi = new int[MAX_SERVEIS][2];
        String[] estat = new String[MAX_SERVEIS];
        int serveiCount = 0;
        int opcio;
        do {
            System.out.println("\n--- Menú Gestió Serveis ---");
            System.out.println("1. Crear servei");
            System.out.println("2. Marcar arribada del taxi");
            System.out.println("3. Finalitzar servei");
            System.out.println("4. Mostrar serveis");
            System.out.println("5. Mostrar mapa");
            System.out.println("6. Sortir");
            System.out.print("Opció: ");
            opcio = sc.nextInt();
            sc.nextLine();
            switch (opcio) {
                case 1:
                    idServei[serveiCount] = serveiCount + 1;

                    int filaInicio, colInicio;

                    while (true){
                        System.out.print("Fila inici:");
                        filaInicio = sc.nextInt();
                        if (filaInicio>= 0 && filaInicio < SIZE){
                            break;
                        }else {
                            System.out.println("Fins tan lluny no arribem, ho sentim.");
                        }
                    }

                    while (true){
                        System.out.print("Columna inici:");
                        colInicio = sc.nextInt();
                        if (filaInicio>= 0 && filaInicio < SIZE){
                            break;
                        }else {
                            System.out.println("Fins tan lluny no arribem, ho sentim.");
                        }
                    }

                    sc.nextLine();

                    coordInici[serveiCount][0] = filaInicio;
                    coordInici[serveiCount][1] = colInicio;

                    System.out.print("Data i hora inici: ");
                    dataHoraInici[serveiCount] = sc.nextLine();

                    estat[serveiCount] = "EN_ESPERA";
                    dataHoraArribada[serveiCount] = "-";
                    dataHoraFi[serveiCount] = "-";

                    servicio s = new servicio("S" + (serveiCount + 1), new posicion(filaInicio, colInicio));
                    city.anyadirservicio(s);
                    serveiCount++;
                    break;
                case 2:
                    System.out.print("ID servei: ");
                    int idArr = sc.nextInt();
                    sc.nextLine();
                    System.out.print("Hora d'arribada: ");
                    dataHoraArribada[idArr - 1] = sc.nextLine();
                    estat[idArr - 1] = "EN_SERVEI";
                    break;
                case 3:
                    System.out.print("ID servei: ");
                    int idFi = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Fila final: ");
                    coordFi[idFi - 1][0] = sc.nextInt();
                    System.out.print("Columna final: ");
                    coordFi[idFi - 1][1] = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Hora final: ");
                    dataHoraFi[idFi - 1] = sc.nextLine();
                    estat[idFi - 1] = "FINALITZAT";
                    break;
                case 4:
                    for (int i = 0; i < serveiCount; i++) {
                        System.out.println(
                                "ID: " + idServei[i] +
                                        " Estat: " + estat[i] +
                                        " Inici: " + dataHoraInici[i] +
                                        " Arribada: " + dataHoraArribada[i] +
                                        " Fi: " + dataHoraFi[i]
                        );
                    }
                    break;
                case 5:
                    city.dibujarmapa();
                    break;
                case 6:
                    System.out.println("Sortint...");
                    break;
            }
        } while (opcio != 6);
    }
}
