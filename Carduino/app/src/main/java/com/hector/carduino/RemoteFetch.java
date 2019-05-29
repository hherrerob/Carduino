package com.hector.carduino;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;
import android.content.Context;

/**
 * Maneja las peticiones a la API del tiempo
 */
public class RemoteFetch {

    /**
     * Dirección de la API
     */
    private static final String OPEN_WEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/weather?";

    /**
     *
     * @param context(Context) Desde donde se llama al método
     * @param lat(double) Latitud de la ciudad
     * @param lon(double) Longitud de la ciudad
     * @return (JSONObject) Respuesta de la API
     */
    public static JSONObject getJSON(Context context, double lat, double lon){
        try {
            URL url = new URL(OPEN_WEATHER_MAP_API + "lat=" + lat + "&lon=" + lon);

            HttpURLConnection connection =
                    (HttpURLConnection)url.openConnection();

            connection.addRequestProperty("x-api-key",
                    context.getString(R.string.weather_api_key));

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            StringBuffer json = new StringBuffer(1024);
            String tmp;
            while((tmp = reader.readLine()) != null)
                json.append(tmp).append("\n");
            reader.close();

            JSONObject data = new JSONObject(json.toString());

            if(data.getInt("cod") != 200){
                return null;
            }

            return data;
        }catch(Exception e){
            return null;
        }
    }
}
