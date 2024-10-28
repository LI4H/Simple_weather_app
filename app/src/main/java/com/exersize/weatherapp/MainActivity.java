package com.exersize.weatherapp;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    private EditText user_field;
    private Button main_btn;
    private TextView result_info;
    private String apiKey = "24378a7498a723a65b0863eabc68ff05";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user_field = findViewById(R.id.user_field);
        main_btn = findViewById(R.id.main_btn);
        result_info = findViewById(R.id.result_info);

        main_btn.setOnClickListener(view -> {
            String city = user_field.getText().toString().trim();
            if (city.isEmpty()) {
                Toast.makeText(MainActivity.this, R.string.no_user_input, Toast.LENGTH_LONG).show();
            } else {
                String geoUrl = "https://api.openweathermap.org/geo/1.0/direct?q=" + city + "&limit=1&appid=" + apiKey;
                new GetCoordinatesTask().execute(geoUrl);
            }
        });
    }

    private class GetCoordinatesTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            result_info.setText("Получение координат...");
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONArray jsonArray = new JSONArray(result);
                if (jsonArray.length() > 0) {
                    JSONObject location = jsonArray.getJSONObject(0);
                    double lat = location.getDouble("lat");
                    double lon = location.getDouble("lon");
                    String weatherUrl = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + apiKey + "&units=metric&lang=ru";
                    new GetWeatherTask(location.getString("name")).execute(weatherUrl);
                } else {
                    result_info.setText("Город не найден");
                }
            } catch (JSONException e) {
                e.printStackTrace();
                result_info.setText("Ошибка обработки данных");
            }
        }
    }

    private class GetWeatherTask extends AsyncTask<String, String, String> {

        private String cityName;

        public GetWeatherTask(String cityName) {
            this.cityName = cityName;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            result_info.setText("Получение данных о погоде...");
        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuilder buffer = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }

                return buffer.toString();

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) connection.disconnect();
                try {
                    if (reader != null) reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try {
                JSONObject jsonObject = new JSONObject(result);

                // Извлекаем нужные данные
                JSONObject main = jsonObject.getJSONObject("main");
                double temperature = main.getDouble("temp");
                double feelsLike = main.getDouble("feels_like");
                int humidity = main.getInt("humidity");

                JSONObject weather = jsonObject.getJSONArray("weather").getJSONObject(0);
                String description = weather.getString("description");

                int clouds = jsonObject.getJSONObject("clouds").getInt("all");
                double windSpeed = jsonObject.getJSONObject("wind").getDouble("speed");

                // Форматируем вывод
                String weatherInfo = String.format(
                        "%s:\nТемпература: %.1f°C\nОщущается как: %.1f°C\nОблачность: %d%%\n" +
                                "Описание: %s\nВлажность: %d%%\nСкорость ветра: %.1f м/с",
                        cityName, temperature, feelsLike, clouds, description, humidity, windSpeed
                );

                result_info.setText(weatherInfo);

            } catch (JSONException e) {
                e.printStackTrace();
                result_info.setText("Ошибка обработки данных о погоде");
            }
        }
    }
}
