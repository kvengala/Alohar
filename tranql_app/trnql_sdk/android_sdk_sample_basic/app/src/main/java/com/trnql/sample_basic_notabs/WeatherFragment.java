package com.trnql.sample_basic_notabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.trnql.smart.base.SmartFragment;
import com.trnql.smart.weather.WeatherEntry;

/**
 * @author Akhil Indurti
 * @version 1.0
 * @since 5/4/15, 12:23 PM
 */
public class WeatherFragment extends SmartFragment {
TextView weather_feels;
TextView weather_humidity;
TextView weather_wind;
TextView weather_rain;
TextView weather_forecast;
TextView weather_sunrise;
TextView weather_sunset;
TextView weather_uv;


@Override
public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
  View view = inflater.inflate(R.layout.frag_weather, container, false);
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
protected void smartWeatherChange(WeatherEntry value) {
  weather_feels.setText(String.format("Feels Like:   %s", value.getFeelsLikeAsString()));
  weather_humidity.setText(String.format("Humidity:   %s", value.getHumidityAsString()));
  weather_wind.setText(String.format("Wind:   %s", value.getHumidityAsString()));
  weather_rain.setText(String.format("Rain:   %s", value.getRainAsString()));
  weather_forecast.setText(String.format("Forecast:   %s", value.getForecastAsString()));
  weather_sunrise.setText(String.format("Sunrise:   %s", value.getSunriseAsString()));
  weather_sunset.setText(String.format("Sunset:   %s", value.getSunsetAsString()));
  weather_uv.setText(String.format("UV Index:   %s", value.getUVIndexAsString()));
}

public static WeatherFragment getInstance() {
  return new WeatherFragment();
}
}//end class WeatherFragment
