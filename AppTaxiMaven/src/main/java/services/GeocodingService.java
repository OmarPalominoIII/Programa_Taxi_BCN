package services;

import org.json.JSONArray;
import org.json.JSONObject;
import models.Position;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GeocodingService {
    public static Position getCoordinates(String address) { // Renombrado a getCoordinates en inglés si quieres mantener la consistencia
        try {
            String addressEncoded = URLEncoder.encode(address, StandardCharsets.UTF_8);

            // 🌟 CORRECCIÓN AQUÍ: Se añade "&format=json" al final de la URL
            String urlApi = "https://nominatim.openstreetmap.org/search?q=" + addressEncoded + "&format=json";

            URL url = new URL(urlApi);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Identificación obligatoria frente a la API
            conn.setRequestProperty("User-Agent", "AppGestionTaxis_v1.0");

            // Leer la respuesta del servidor
            Scanner sc = new Scanner(conn.getInputStream()); // Es más seguro usar conn.getInputStream() configurada la cabecera
            StringBuilder response = new StringBuilder();
            while (sc.hasNext()){
                response.append(sc.nextLine());
            }
            sc.close();

            // Extraer latitud y longitud del JSON
            JSONArray jsonArray = new JSONArray(response.toString());
            if (jsonArray.length() > 0) {
                JSONObject obj = jsonArray.getJSONObject(0);

                // Nominatim devuelve "lat" y "lon" como Strings dentro del JSON,
                // pasarlos a Double usando Double.parseDouble evita errores de casteo.
                double lat = Double.parseDouble(obj.getString("lat"));
                double lon = Double.parseDouble(obj.getString("lon"));

                return new Position(lat, lon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    }

