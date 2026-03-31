package com.taxibcn.core;

import com.taxibcn.UI.Menu;

import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;

public class TaxiBCN {

    public static void executar() {

        Scanner sc = new Scanner(System.in);

        int total_taxis = 10;
        int cantidad_taxi_adaptats = total_taxis / 10;
        int cantidad_taxi_normal = total_taxis - cantidad_taxi_adaptats;

        ArrayList<Integer> taxi_id = new ArrayList<>();

        for (int i = 0; i < total_taxis; i++) {
            taxi_id.add(i);
        }

        HashMap<Integer, String> disponibilitat_taxi = new HashMap<>();
        String lliure = "lliure";
        String ocupat = "ocupat";
        String descans = "descans";

        for (int i = 0; i < total_taxis; i++) {
            disponibilitat_taxi.put(i, lliure);
        }

        System.out.println(
                        "______________________" +
                        "\n       MENÚ           " +
                        "\n  1) TAXI NORMAL      " +
                        "\n  2) TAXI ADAPTAT     " +
                        "\n______________________");

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
            Menu.imprimirOpciones();

            opcio = sc.nextInt();
            sc.nextLine();

            switch (opcio) {
                case 1:
                    idServei[serveiCount] = serveiCount + 1;

                    System.out.print("Fila inici: ");
                    coordInici[serveiCount][0] = sc.nextInt();
                    System.out.print("Columna inici: ");
                    coordInici[serveiCount][1] = sc.nextInt();
                    sc.nextLine();

                    System.out.print("Data i hora inici: ");
                    dataHoraInici[serveiCount] = sc.nextLine();

                    estat[serveiCount] = "EN_ESPERA";
                    dataHoraArribada[serveiCount] = "-";
                    dataHoraFi[serveiCount] = "-";

                    serveiCount++;
                    break;

                case 2:
                    System.out.print("ID servei: ");
                    int idArr = sc.nextInt();
                    sc.nextLine();

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
                    System.out.println("Sortint...");
                    break;
            }

        } while (opcio != 5);
    }
}
