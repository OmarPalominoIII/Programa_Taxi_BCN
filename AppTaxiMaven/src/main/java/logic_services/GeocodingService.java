package logic_services;

import org.json.JSONArray;
import org.json.JSONObject;
import project_models.Position;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class GeocodingService {
    public static Position getCoordenates(String address){
        try {
            String addressEncoded = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String urlApi = "https://nominatim.openstreetmap.org/search?q=" + addressEncoded;

            URL url = new URL(urlApi);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            conn.setRequestProperty("User-Agent", "AppGestionTaxis_v1.0");

            Scanner sc = new Scanner(url.openStream());
            StringBuilder response = new StringBuilder();
            while (sc.hasNext()){
                response.append(sc.nextLine());
            }
            sc.close();

            // 3. Extraer latitud y longitud del JSON
            JSONArray jsonArray = new JSONArray(response.toString());
            if (jsonArray.length() > 0) {
                JSONObject obj = jsonArray.getJSONObject(0);
                double lat = obj.getDouble("lat");
                double lon = obj.getDouble("lon");
                return new Position(lat, lon);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
        }
    }

