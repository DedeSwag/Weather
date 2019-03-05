package mg.studio.weatherappdesign;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.String;


public class MainActivity extends AppCompatActivity {
    static String city_name = " ";
    static String secondTemp = " ";
    static String thirdTemp = " ";
    static String fourthTemp = " ";
    static String fifthTemp = " ";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnClick(null);
    }
    public void btnClick(View view) {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()){
            new DownloadUpdate().execute();
            Toast.makeText(getApplicationContext(),"Network connected",Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(getApplicationContext(),"No network connection",Toast.LENGTH_LONG).show();
    }

    private class DownloadUpdate extends AsyncTask<String, Void, String> {
        protected JSONArray JSON_Array;

        @Override
        protected String doInBackground(String... strings) {
            //String stringUrl = "https://mpianatra.com/Courses/forecast.json";
            String stringUrl = "http://api.openweathermap.org/data/2.5/forecast?q=Chongqing,cn&mode=json&APPID=aa3d744dc145ef9d350be4a80b16ecab";
            HttpURLConnection urlConnection = null;
            BufferedReader reader;
            try {
                URL url = new URL(stringUrl);

                // Create the request to get the information from the server, and open the connection
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
                    // Mainly needed for debugging
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                //Parse JSON data
                try{
                    JSONObject jsonObj = new JSONObject(buffer.toString());
                    String list = jsonObj.optString("list").toString();
                    String city = jsonObj.optString("city").toString();
                    JSONObject cityObj = new JSONObject(city);
                    city_name = cityObj.optString("name");

                    //There are arrays in list
                    JSON_Array = new JSONArray(list);

                    //Get the current temperature in the array which index is 0
                    int[][] array = new int[5][8];
                    for(int j=0; j<5; j++){
                        for(int i=0; i<8; i++)
                            array[j][i] = getTemperature(JSON_Array,i+8*j);
                        bubbleSort(array[j]);
                    }
                    /*for(int i=0; i<6; i++)
                        array[4][i] = getTemperature(JSON_Array,i+8*j);
                    bubbleSort(array[4]);*/

                    secondTemp = String.valueOf(array[1][0]) + "~" + String.valueOf(array[1][7]) + "°C";
                    thirdTemp = String.valueOf(array[2][0]) + "~" + String.valueOf(array[2][7]) + "°C";
                    fourthTemp = String.valueOf(array[3][0]) + "~" + String.valueOf(array[3][7]) + "°C";
                    fifthTemp = String.valueOf(array[4][3]) + "~" + String.valueOf(array[4][7]) + "°C";

                    String currentTemp = String.valueOf(array[0][0]) + "~" + String.valueOf(array[0][7]);
                    return currentTemp;
                }catch (JSONException e){
                    e.printStackTrace();
                }
                //return result;//return the data of temperature.
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (ProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPostExecute(String temperature) {
            //Update the temperature displayed
            ((TextView) findViewById(R.id.temperature_of_the_day)).setText(temperature);

            for(int i=0; i<5; i++){
                updateIconByDay(JSON_Array,i);
            }
            updateCityName(city_name);
            updateWeekday();
            updateDate();
            updateTemperature();
        }

    }
    /*** Function for updating the weather image ***/
    protected void updateIconByDay(JSONArray jsonArray, int index){
        try {
            JSONObject theDay = jsonArray.getJSONObject(index*8);
            String weather = theDay.optString("weather").toString();
            JSONArray weatherArray = new JSONArray(weather);
            JSONObject weather_detail = weatherArray.getJSONObject(0);
            String weatherCondition = weather_detail.optString("main");

            ImageView theView;

            switch (index){
                case 0:
                    theView = (ImageView)this.findViewById(R.id.weather_condition_1);
                    break;
                case 1:
                    theView = (ImageView)this.findViewById(R.id.weather_condition_2);
                    break;
                case 2:
                    theView = (ImageView)this.findViewById(R.id.weather_condition_3);
                    break;
                case 3:
                    theView = (ImageView)this.findViewById(R.id.weather_condition_4);
                    break;
                case 4:
                    theView = (ImageView)this.findViewById(R.id.weather_condition_5);
                    break;
                default:
                    theView = (ImageView)this.findViewById(R.id.weather_condition_1);
            }

            switch (weatherCondition){
                case "Clear":
                    theView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.sunny_small));
                    break;
                case "Clouds":
                    theView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.partly_sunny_small));
                    break;
                case "Rain":
                    theView.setImageDrawable(getApplicationContext().getResources().getDrawable(R.drawable.rainy_small));
                    break;
                default:
            }
        }catch (JSONException e){
            e.printStackTrace();
        }

    }
    /*** Function for changing weekday to number ***/
    public int turnWeekdayToNumber(String str){
        int number=0;
        switch (str){
            case "Sunday":
                number=7;
                break;
            case"Saturday":
                number=6;
                break;
            case"Friday":
                number=5;
                break;
            case"Thursday":
                number=4;
                break;
            case"Wednesday":
                number=3;
                break;
            case"Tuesday":
                number=2;
                break;
            case"Monday":
                number=1;
                break;
            default:
                break;
        }
        return number;
    }
    /*** Function for updating the TextView of the next four days ***/
    public void updateSpecificDay(int i){
        String[] weekArray = {"MON","TUE","WED","THU","FRI","SAT","SUN"};
        int weekday2=i;
        weekday2=weekday2%7;
        String weekday2string=weekArray[weekday2];
        TextView TVweekday2=(TextView)this.findViewById(R.id.weekday2);
        TVweekday2.setText(weekday2string);

        int weekday3=i+1;
        weekday3=weekday3%7;
        String weekday3string=weekArray[weekday3];
        TextView TVweekday3=(TextView)this.findViewById(R.id.weekday3);
        TVweekday3.setText(weekday3string);

        int weekday4=i+2;
        weekday4=weekday4%7;
        String weekday4string=weekArray[weekday4];
        TextView TVweekday4=(TextView)this.findViewById(R.id.weekday4);
        TVweekday4.setText(weekday4string);

        int weekday5=i+3;
        weekday5=weekday5%7;
        String weekday5string=weekArray[weekday5];
        TextView TVweekday5=(TextView)this.findViewById(R.id.weekday5);
        TVweekday5.setText(weekday5string);
    }
    /*** Function for updating the TextView of current day ***/
    public void updateWeekday(){
        Date curDate=new Date(System.currentTimeMillis());
        SimpleDateFormat format=new SimpleDateFormat("EEEE");
        String weekday1=format.format(curDate);
        int numberWeekday1=turnWeekdayToNumber(weekday1);
        updateSpecificDay(numberWeekday1);
        TextView TextWeekday=(TextView)this.findViewById(R.id.tv_weekday);
        TextWeekday.setText(weekday1);
    }
    /*** Function for updating the name of city ***/
    public void updateCityName(String cityName){
        TextView cityname=(TextView)this.findViewById(R.id.tv_location);
        cityname.setText(cityName);
    }
    /*** Function for updating the date ***/
    public void updateDate(){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date curDate=new Date(System.currentTimeMillis());
        String date=formatter.format(curDate);
        TextView TextTime=(TextView)this.findViewById(R.id.tv_date);
        TextTime.setText(date);
    }

    public int getTemperature(JSONArray jArray,int index){
        try{
            JSONObject currentday = jArray.getJSONObject(index);
            String main = currentday.optString("main").toString();
            JSONObject temp = new JSONObject(main);
            String currentTemp = temp.optString("temp");
            Double current_temp = Double.parseDouble(currentTemp);
            current_temp = current_temp - 273.15;
            int int_temp = current_temp.intValue();//Convert to Int
            return int_temp;
        }catch (JSONException e){
            e.printStackTrace();
        }
        return 0;
    }
    public void bubbleSort(int[] numbers) {
        int temp = 0;
        int size = numbers.length;
        for(int i = 0 ; i < size-1; i ++)
        {
            for(int j = 0 ;j < size-1-i ; j++)
            {
                if(numbers[j] > numbers[j+1])  //Change the position
                {
                    temp = numbers[j];
                    numbers[j] = numbers[j+1];
                    numbers[j+1] = temp;
                }
            }
        }
    }
    public void updateTemperature() {
        TextView TextTemp1 = (TextView) this.findViewById(R.id.temp_of_second_day);
        TextTemp1.setText(secondTemp);
        TextView TextTemp2 = (TextView) this.findViewById(R.id.temp_of_third_day);
        TextTemp2.setText(thirdTemp);
        TextView TextTemp3 = (TextView) this.findViewById(R.id.temp_of_fourth_day);
        TextTemp3.setText(fourthTemp);
        TextView TextTemp4 = (TextView) this.findViewById(R.id.temp_of_fifth_day);
        TextTemp4.setText(fifthTemp);
    }
}
