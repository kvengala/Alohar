package com.trnql_devpost.kashyap.callbusy;

import android.os.Bundle;
        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.TextView;
        import com.trnql.smart.base.SmartFragment;
        import com.trnql.smart.weather.WeatherEntry;

public class weather extends SmartFragment {
    TextView weather_feels;
    TextView weather_humidity;
    TextView weather_wind;
    TextView weather_rain;
    TextView weather_forecast;
    TextView weather_sunrise;
    TextView weather_sunset;
    TextView weather_uv;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_weather, container, false);
        weather_feels = (TextView) view.findViewById(R.id.weather_feels);
        weather_humidity = (TextView) view.findViewById(R.id.weather_humidity);
        weather_wind = (TextView) view.findViewById(R.id.weather_wind);
        weather_rain = (TextView) view.findViewById(R.id.weather_rain);
        weather_forecast = (TextView) view.findViewById(R.id.weather_forecast);
        weather_sunrise = (TextView) view.findViewById(R.id.weather_sunrise);
        weather_sunset = (TextView) view.findViewById(R.id.weather_sunset);
        weather_uv = (TextView) view.findViewById(R.id.weather_uv);
        return view;
    }

    @Override
    protected void smartWeatherChange(WeatherEntry weather) {
        weather_feels.setText("Feels Like:   %s" + weather.getFeelsLikeAsString());
        weather_humidity.setText("Humidity:   %s" + weather.getHumidityAsString());
        weather_wind.setText("Wind:   %s" + weather.getWindAsString());
        weather_rain.setText("Rain:   %s" + weather.getRainAsString());
        weather_forecast.setText("Forecast:   %s" + weather.getForecastAsString());
        weather_sunrise.setText("Sunrise:   %s" + weather.getSunriseAsString());
        weather_sunset.setText("Sunset:   %s" + weather.getSunsetAsString());
        weather_uv.setText("UV Index:   %s" + weather.getUVIndexAsString());
    }
}