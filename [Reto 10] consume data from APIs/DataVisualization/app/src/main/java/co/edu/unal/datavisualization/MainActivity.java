package co.edu.unal.datavisualization;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private static final String[] departamentos = {
            "Amazonas", "Antioquia", "Arauca", "Atlántico",
            "Bogotá D.C.", "Bolívar", "Boyacá", "Caldas",
            "Caquetá", "Casanare", "Cauca", "Cesar",
            "Chocó", "Córdoba", "Cundinamarca", "Guainía",
            "Guaviare", "Huila", "La Guajira", "Magdalena",
            "Meta", "Nariño", "Norte de Santander", "Putumayo",
            "Quindío", "Risaralda", "San Andrés y Providencia", "Santander",
            "Sucre", "Tolima", "Valle del Cauca", "Vaupés", "Vichada"
    };

    /*private static final String[][] departamentos_values = { {"AMAZONAS"},
            {"ANTIOQUIA"}, {"ARAUCA"}, {"ATLANTICO"}, {"BOGOTA"}, {"BOLIVAR"},
            {"BOYACA"}, {"CALDAS"}, {"CAQUETA"}, {"CASANARE"}, {"CAUCA"},
            {"CESAR"}, {"CHOCO"}, {"CORDOBA"}, {"CUNDINAMARCA"}, {"GUAINIA"},
            {"GUAVIARE"}, {"HUILA"}, {"LA GUAJIRA"}, {"MAGDALENA"}, {"META"},
            {"NARIÑO"}, {"NORTE DE SANTANDER"}, {"PUTUMAYO"}, {"QUINDIO"}, {"RISARALDA"},
            {"SAN ANDRES Y PROVIDENCIA"}, {"SANTANDER"}, {"SUCRE"}, {"TOLIMA"}, {"VALLE DEL CAUCA"},
            {"VAUPES"}, {"VICHADA"}
    };*/

    private static final String[] departamentos_values = { "AMAZONAS",
            "ANTIOQUIA", "ARAUCA", "ATLANTICO", "BOGOTA", "BOLIVAR",
            "BOYACA", "CALDAS", "CAQUETA", "CASANARE", "CAUCA",
            "CESAR", "CHOCO", "CORDOBA", "CUNDINAMARCA", "GUAINIA",
            "GUAVIARE", "HUILA", "LA GUAJIRA", "MAGDALENA", "META",
            "NARIÑO", "NORTE DE SANTANDER", "PUTUMAYO", "QUINDIO", "RISARALDA",
            "SAN ANDRES Y PROVIDENCIA", "SANTANDER", "SUCRE", "TOLIMA", "VALLE DEL CAUCA",
            "VAUPES", "VICHADA"
    };

    private static final String[] categoria = {
            "CEREALES",
            "FIBRAS",
            "FLORES Y FOLLAJES",
            "FORESTALES",
            "FRUTALES",
            "HONGOS",
            "HORTALIZAS",
            "LEGUMINOSAS",
            "OLEAGINOSAS",
            "OTROS PERMANENTES",
            "OTROS TRANSITORIOS",
            "PLANTAS AROMATICAS, CONDIMENTARIAS Y MEDICINALES",
            "TUBERCULOS Y PLATANOS"
    };

    private final String[] data = {"AMAZONAS","CEREALES"};

    Context context;
    ProgressDialog pd;
    Button busquedaButton, departamentoButton, categoriaButton;
    ArrayList<String> dataOut = new ArrayList<>();

    private static final int CONNECTION_TIMEOUT = 60000;
    private static final int DATARETRIEVAL_TIMEOUT = 60000;

    private double sumProduct = 0;
    private double sumArea = 1;
    private double rendimiento = 0;

    private static final String uribase = "https://www.datos.gov.co/resource/2pnw-mmge.json?";
    private TextView resultado;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner sDepartamento = (Spinner) findViewById(R.id.spinner);
        Spinner sCategoria = (Spinner) findViewById(R.id.spinner2);
        resultado = (TextView) findViewById(R.id.resultado);

        // Application of the Array to the Spinners
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, departamentos);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sDepartamento.setAdapter(spinnerArrayAdapter);

        ArrayAdapter<String> spinnerArrayAdapter2 = new ArrayAdapter<String>(this,   android.R.layout.simple_spinner_item, categoria);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item); // The drop down view
        sCategoria.setAdapter(spinnerArrayAdapter2);

        sDepartamento.setOnItemSelectedListener(this);
        sCategoria.setOnItemSelectedListener(this);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(v -> new JsonTask().execute());

    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        switch (parent.getId()){
            case R.id.spinner2:
                data[1] = categoria[pos];
                break;
            case R.id.spinner:
                data[0] = departamentos_values[pos];
                break;
        }

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
    }

    public void getMethod(View view) {
        // Do something in response to button click
        Log.d("bandera",data[0]+" "+data[1]);
    }

    public static JSONArray requestWebService(String serviceUrl) {
        disableConnectionReuseIfNecessary();

        HttpURLConnection urlConnection = null;
        try {
            // create connection
            URL urlToRequest = new URL(serviceUrl);
            urlConnection = (HttpURLConnection)  urlToRequest.openConnection();
            urlConnection.setConnectTimeout(CONNECTION_TIMEOUT);
            urlConnection.setReadTimeout(DATARETRIEVAL_TIMEOUT);

            // handle issues
            int statusCode = urlConnection.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // handle unauthorized (if service requires user login)
                System.out.println("Error de autorización");
            }

            else if (statusCode != HttpURLConnection.HTTP_OK) {
                // handle any other errors, like 404, 500,..
                System.out.println("Error Miscelaneo");
            }

            // create JSON object from content
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return new JSONArray(getResponseText(in));
        }

        catch (MalformedURLException e) { System.out.println("URL is invalid"); }
        catch (SocketTimeoutException e) { System.out.println("data retrieval or connection timed out"); }
        catch (IOException e) { System.out.println("could not read response body"); }
        catch (JSONException e) { System.out.println("response body is no valid JSON string"); }
        finally { if (urlConnection != null) { urlConnection.disconnect(); } }
        return null;
    }

    private static void disableConnectionReuseIfNecessary() {
        // see HttpURLConnection API doc
        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
            System.setProperty("http.keepAlive", "false");
        }
    }

    private static String getResponseText(InputStream inStream) {
        // very nice trick from
        // http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
        return new Scanner(inStream).useDelimiter("\\A").next();
    }

    public void getData(String uribase, String query) {
        JSONArray serviceResult = requestWebService(uribase + query);
        dataOut.clear();

        try {
            String _sumProduct = serviceResult.getJSONObject(0).getString("sum_producci_n_t").toString();
            String _sumArea = serviceResult.getJSONObject(0).getString("sum_rea_cosechada_ha").toString();

            sumProduct = Double.parseDouble(_sumProduct);
            sumArea = Double.parseDouble(_sumArea);

            Log.d("sumProduct",_sumProduct);
            Log.d("sumProduct",_sumArea);

        }

        catch (JSONException e) { Log.e("Error", e.toString()); }
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();
            pd = new ProgressDialog(MainActivity.this);
            pd.setMessage("Please wait");
            pd.setCancelable(false);
            pd.show();
        }

        protected String doInBackground(String... params) {
            //String query ="departamento="+data[0]+"&"+"grupo_de_cultivo="+data[1];
            String query = "$select=sum(producci_n_t),sum(rea_cosechada_ha)&$where=departamento=%22"+data[0]+"%22%20and%20grupo_de_cultivo=%22"+data[1]+"%22";
            try { //getData(uribase, query);
                getData(uribase,query);
                 }
            catch (Throwable e) { e.printStackTrace(); }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (pd.isShowing()) { pd.dismiss(); }
            rendimiento = sumProduct/sumArea;
            resultado.setText("Producto en toneladas de "+ String.format("%.2f",sumProduct)+ ", en un área cosechada dada en hectareas de "+String.format("%.2f",sumArea) + ", obteniendo así un rendimiento de " + String.format("%.2f",rendimiento) + " toneladas por hectarea.");

        }
    }
}