package marvinrobert.stockexchangegh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class Chart extends AppCompatActivity {
    private String TAG = Chart.class.getSimpleName();

    private PieChart pieChart;

    String ticker;

    static Float volume;
    static Float price;
    static Float shares;
    static Float capital;
    static Float dps;
    static Float eps;
    static Float change;

    static String address;
    static String email;
    static String facsimile;
    static String industry;
    static String name;
    static String sector;
    static String telephone;
    static String website;

    private static String url;

    private ProgressDialog progressDialog;
    private Intent chartIntent;
    private ListView listView;

    ArrayList<HashMap<String, String>> stockList;
    private String API_URL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        listView = findViewById(R.id.list);

        Bundle firstActivityData = getIntent().getExtras();

        ticker = firstActivityData.getString("name");
        url = "https://dev.kwayisi.org/apis/gse/equities/" + ticker;
        API_URL = "https://dev.kwayisi.org/apis/gse/live";
        if(firstActivityData == null) {
            return;
        }
        change = Float.parseFloat(firstActivityData.getString("change"));
        volume = Float.parseFloat(firstActivityData.getString("volume"));

        stockList = new ArrayList<>();
//        new GetData().execute();
        new GetJsonData().execute();
    }

    private void setupPieChart()
    {
        pieChart = findViewById(R.id.pieChart);
        pieChart.setBackgroundColor(Color.WHITE);
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5,10,5,5);
        pieChart.setDragDecelerationFrictionCoef(0.99f);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(60f);

        ArrayList<PieEntry> values = new ArrayList<>();
        if((shares == null) | (capital == null)) {
            return;
        }
        values.add(new PieEntry(shares,"Total Issued Shares"));
        values.add(new PieEntry(capital,"Market Capital(GH Cedis)"));

        Description description = new Description();
        description.setText("");
        description.setTextSize(15);
        pieChart.setDescription(description);

        PieDataSet dataSet = new PieDataSet(values,"");
        dataSet.setSelectionShift(5f);
        dataSet.setSliceSpace(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);

        PieData data = new PieData(dataSet);
        //data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(13f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();

        pieChart.animateY(5000, Easing.EasingOption.EaseInOutCubic);

        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
    }

    public class MyAxisValueFormatter implements IAxisValueFormatter {
        private String[] mValues;
        public MyAxisValueFormatter(String [] values) {
            this.mValues = values;
        }
        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mValues[(int)value];
        }
    }

    private class GetJsonData extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(Chart.this);
            progressDialog.setMessage("Retrieving Data........");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler httpHandler = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = httpHandler.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    JSONObject c = jsonObj.getJSONObject("company");
                    address = c.getString("address");
                    email = c.getString("email");
                    facsimile = c.getString("facsimile");
                    industry = c.getString("industry");
                    name = c.getString("name");
                    sector = c.getString("sector");
                    telephone = c.getString("telephone");
                    website = c.getString("website");

                    capital = (float)jsonObj.getDouble("capital");
                    price = (float)jsonObj.getDouble("price");
                    shares = (float)jsonObj.getDouble("shares");

                    String temp;

                    temp = jsonObj.getString("eps");
                    if(temp == "null") {
                        eps = 0f;
                    }
                    else {
                        eps = (float)jsonObj.getDouble("eps");
                    }

                    temp = jsonObj.getString("dps");
                    if(temp == "null") {
                        dps = 0f;
                    }
                    else {
                        dps = (float)jsonObj.getDouble("dps");
                    }

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!", Toast.LENGTH_LONG).show();
                    }
                });
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            ListAdapter adapter = new SimpleAdapter(
                    Chart.this, stockList,
                    R.layout.list_view, new String[]{"name","price","change","volume"}, new int[]{R.id.name,R.id.price,R.id.change,R.id.volume});

            listView.setAdapter(adapter);
            setupPieChart();
            setupText();
        }
    }

    private void setupText() {
        TextView companyname = findViewById(R.id.companyName);
        companyname.setText(name);

        TextView companyaddress = findViewById(R.id.address);
        companyaddress.setText(address);

        TextView companyemail = findViewById(R.id.email);
        companyemail.setText(email);

        TextView companyfacsimile = findViewById(R.id.fascimile);
        companyfacsimile.setText(facsimile);

        TextView companyindustry = findViewById(R.id.industry);
        companyindustry.setText(industry);

        TextView companysector = findViewById(R.id.sector);
        companysector.setText(sector);

        TextView companytelephone = findViewById(R.id.telephone);
        companytelephone.setText(telephone);

        TextView companywebsite = findViewById(R.id.website);
        companywebsite.setText(website);
    }

}