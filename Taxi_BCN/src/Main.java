import java.util.Scanner;
import java.util.ArrayList;
import java.util.HashMap;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        // Determinamos la cantidad de taxis normales y adaptados

        int total_taxis = 10;
        int cantidad_taxi_adaptats = total_taxis/10;
        int cantidad_taxi_normal = total_taxis - cantidad_taxi_adaptats;


        // Creamos el id para cada taxi.
        ArrayList<Integer> taxi_id = new ArrayList<Integer>();


        for (int i = 0; i < total_taxis; i++){
            if (i < cantidad_taxi_normal){
                taxi_id.add(i);
            }else {
                taxi_id.add(i);
            }
        }


        // creamos un hashmap con taxi_id como key y disponibilidad como value, el value cambia dependiendo la franja


        HashMap<Integer, String> disponibilitat_taxi = new HashMap<Integer, String>();
        String lliure = "lliure";
        String ocupat = "ocupat";
        String descans = "descans";


        for (int i = 0; i < total_taxis; i++){
            disponibilitat_taxi.put(i, lliure);
        }
        System.out.println("______________________");
        System.out.println("       MENÚ           ");
        System.out.println("  1) TAXI NORMAL      ");
        System.out.println("  2) TAXI ADAPTAT     ");
        System.out.println("______________________");
        double hora = sc.nextInt();


        // franja horario en 24 horas


        if (hora < 19) {
            for (int i = 0; i < total_taxis; i++) {
                if (i < (total_taxis / 2) + 1) {
                    disponibilitat_taxi.put(i, lliure);
                }else {
                    disponibilitat_taxi.put(i, descans);
                }
            }
        } else if (hora > 19) {
            for (int i = 0; i < total_taxis; i++){
                if (i < (total_taxis / 2) - 1){
                    disponibilitat_taxi.put(i, descans);
                }else {
                    disponibilitat_taxi.put(i, lliure);
                }
            }
        }


        /* Tamaño Mapa


        int[][] mapa_taxi = new int[6][10];


        for (int i = 0; i < mapa_taxi.length; i++){
          for (int j = 0; j < mapa_taxi[0].length; j++){
            System.out.print("* ");
            }
            System.out.println();
        }


*/


    }
}