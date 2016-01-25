package br.com.ejcm.weathercast;

import android.app.LauncherActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;

/**
 * A placeholder fragment containing a simple view.
 */
public class ListForecastFragment extends Fragment{
    private static final String LOG_TAG = ListForecastFragment.class.getName();
    public static final String DETAIL = "qualquer coisa";
    private ArrayAdapter <String> mForecastAdapter;
    public ListForecastFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
//        String[] data = {
//                "Mon 6/23 - Sunny - 31/17",
//                "Tue 6/24 - Foggy - 21/8",
//                "Wed 6/25 - Cloudy - 22/17",
//                "Thurs 6/26 - Rainy - 18/11",
//                "Fri 6/27 - Foggy - 21/10",
//                "Sat 6/28 - TRAPPED IN WEATHERSTATION - 23/18",
//                "Sun 6/29 - Sunny - 20/7"
//        };
        mForecastAdapter = new ArrayAdapter<String>(
                getActivity(),
                R.layout.forecast_lis_item,
                R.id.list_item_text_view);

        View rootView = inflater.inflate(R.layout.fragment_list_forecast, container, false);
        ListView list = (ListView) rootView.findViewById(R.id.list_forecast);
        list.setAdapter(mForecastAdapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView lv, View v, int position, long id) {

                Intent it = new Intent(getContext(),ItemDescription.class);
                it.putExtra(DETAIL, ((TextView) v).getText());
                startActivity(it);

            }
        });

        return rootView;
    }

    public void onStart(){
        super.onStart();
        FetchWeatherTask fwt = new FetchWeatherTask();
        String location = Utility.getPreferredLocation(getActivity());
        String format = Utility.getPrefredTemperatureFormat(getActivity());
        fwt.execute(location, format);

    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        private String formatDate(long dateInMillis) {
            DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
            return df.format(new Date(dateInMillis));
        }

        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays) {
            String[] result = new String[numDays];
            final String OWM_DATE = "dt";
            try {
                JSONObject forecast = new JSONObject(forecastJsonStr);
                JSONArray list = forecast.getJSONArray("list");
                for (int i = 0; i < numDays; i++){

                    JSONObject day = list.getJSONObject(i);

                    long dateInMillis = day.getLong(OWM_DATE)*1000;
                    String date = formatDate(dateInMillis);

                    JSONObject temp = day.getJSONObject("temp");
                    String min = temp.getString("min");
                    String max = temp.getString("max");
                    String desc = day.getJSONArray("weather")
                            .getJSONObject(0)
                            .getString("main");
                    StringBuilder sb = new StringBuilder();
                    sb
                            .append(date)
                            .append("\n")
                            .append(desc)
                            .append(" - ")
                            .append("Min: " + min + " ºC")
                            .append(" /")
                            .append("Max: " + max + " ºC");
                    result[i]= sb.toString();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;

            String location = params[0];
            String format = "json";
            String units = params[1];
            int numDays = 14;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL =
                        "http://api.openweathermap.org/data/2.5/forecast/daily?";
                final String QUERY_PARAM = "q";
                final String FORMAT_PARAM = "mode";
                final String UNITS_PARAM = "units";
                final String DAYS_PARAM = "cnt";
                final String APPID_PARAM = "appid";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, location)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, units)
                        .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                        .appendQueryParameter(APPID_PARAM, "671e5480c49c7f91289cb8f72d91dd8d")
                        .build();

                URL url = new URL(builtUri.toString());
                //Log.v(LOG_TAG, url.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                forecastJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error " + e, e);
//            e.printStackTrace();
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return getWeatherDataFromJson(forecastJsonStr, numDays);

        }

        @Override
        protected void onPostExecute(String [] result){
            mForecastAdapter.clear();
            for (String forecast:result)
                mForecastAdapter.add(forecast);
        }

    }
}
