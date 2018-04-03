package marvinrobert.stockexchangegh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class FirstActivity extends AppCompatActivity {
    private String TAG = FirstActivity.class.getSimpleName();
    private DrawerLayout mDrawerLayout;
    private MenuItem logout;
    private FirebaseAuth auth;
    private static String API_URL = "https://dev.kwayisi.org/apis/gse/live";
    private ProgressDialog progressDialog;
    private Intent chartIntent;
    private ListView listView;

    ArrayList<HashMap<String, String>> stockList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
        chartIntent = new Intent(this,Chart.class);
        listView = findViewById(R.id.list);
//        logoutBt = findViewById(R.id.logoutBt);

        auth = FirebaseAuth.getInstance();


        mDrawerLayout = findViewById(R.id.drawer_layout);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        mDrawerLayout.addDrawerListener(
                new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(View drawerView, float slideOffset) {
                        // Respond when the drawer's position changes
                    }

                    @Override
                    public void onDrawerOpened(View drawerView) {
                        // Respond when the drawer is opened
                    }

                    @Override
                    public void onDrawerClosed(View drawerView) {
                        // Respond when the drawer is closed
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {
                        // Respond when the drawer motion state changes
                    }
                }
        );

//        logoutBt.setOnClickListener(do_Logout());

//        Spinner spinner = (Spinner) findViewById(R.id.stock_spinner);
//        // Create an ArrayAdapter using the string array and a default spinner layout
//        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.planets_array, android.R.layout.simple_spinner_item);
//        // Specify the layout to use when the list of choices appears
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        // Apply the adapter to the spinner
//        spinner.setAdapter(adapter);

        stockList = new ArrayList<>();
        new GetData().execute();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void do_Logout() {
        auth.signOut();
        Intent intent = new Intent(FirstActivity.this, Login.class);
        startActivity(intent);
        finish();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private  class GetData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            progressDialog = new ProgressDialog(FirstActivity.this);
            progressDialog.setMessage("Fetching data...");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler httpHandler = new HttpHandler();

            // Making a request to url and getting response
            String httpResponse = httpHandler.makeServiceCall(API_URL);

            Log.e(TAG, "Response from url: " + httpResponse);

            if (httpResponse != null) {
                try {
                    JSONArray httpResponseArray = new JSONArray(httpResponse);

                    // looping through stock names
                    for (int i = 0; i < httpResponseArray.length(); i++) {

                        JSONObject c = httpResponseArray.getJSONObject(i);

                        String name = c.getString("name");
                        Double price = c.getDouble("price");
                        Double change = c.getDouble("change");
                        Double volume=c.getDouble("volume");

                        // tmp hash map for single contact
                        HashMap<String, String> stock = new HashMap<>();

                        // adding each child node to HashMap key => value

                        stock.put("name", name);
                        stock.put("price",Double.toString(price));
                        stock.put("change",Double.toString(change));
                        stock.put("volume",Double.toString(volume));


                        // adding contact to contact list
                        stockList.add(stock);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (progressDialog.isShowing())
                progressDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    FirstActivity.this, stockList,
                    R.layout.list_view, new String[]{"name","price","change","volume"}, new int[]{R.id.name,R.id.price,R.id.change,R.id.volume});

            listView.setAdapter(adapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the selected item text from ListView
                    TextView clickedView = view.findViewById(R.id.name);
                    TextView clickedView2 = view.findViewById(R.id.volume);
                    TextView clickedView3 = view.findViewById(R.id.change);

                    String data = (String)clickedView.getText();
                    String data2 = (String)clickedView2.getText();
                    String data3 = (String)clickedView3.getText();

                    chartIntent.putExtra("name",data);
                    chartIntent.putExtra("volume",data2);
                    chartIntent.putExtra("change",data3);

                    startActivity(chartIntent);
                }
            });
        }
    }
}
