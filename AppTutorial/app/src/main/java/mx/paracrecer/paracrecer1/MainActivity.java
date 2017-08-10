package mx.paracrecer.paracrecer1;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    // UI
    private ImageView imgLogo;
    private ImageView imgPerfil;
    private ImageView imgPass;
    private EditText inputPerfil;
    private EditText inputPass;
    private Button boton;
    private ProgressDialog progress;

    // System
    private Boolean authorize = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // UI
        loadView();

        boton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v){
                Log.d("onClick", "Click...");

                userValidation();
            }
        });
    }

    private void userValidation(){

        // Start progress UI
        progress = new ProgressDialog(MainActivity.this);
        progress.setIndeterminate(true);
        progress.setMessage("Autenticando...");
        progress.show();

        final Thread t = new Thread() {
            @Override
            public void run() {

                // Get value
                String correo = inputPerfil.getText().toString();
                String password = inputPass.getText().toString();

                // Login
                loginSinAsync(correo, password);

                // Stop progress
                progress.dismiss();

                // Change Activity
                if(authorize){

                    try{

                        //Intent intent = new Intent(MainActivity.this, Ubicacion.class);
                        Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                        intent.putExtra("miCorreo", correo);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();

                    }catch(Exception e){
                        Log.d("what",e.getMessage());
                    }
                }
            }
        };
        t.start();
    }

    private void login(){

        AsyncTask.execute(new Runnable() {

            @Override
            public void run() {

                String stringUrl = "http://107.170.206.68:41062/www/pc/gps/login.php";

                try {

                    //URL
                    URL url = new URL(stringUrl);
                    //Connection
                    HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
                    // Method
                    myConnection.setRequestMethod("POST");
                    // Headers
                    myConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    myConnection.setRequestProperty("charset", "utf-8");

                    // Create the data
                    String myData = "correo=ginppian@gmail.com&password=1234";

                    // Enable writing
                    myConnection.setDoOutput(true);
                    myConnection.setInstanceFollowRedirects(false);

                    // Write the data
                    myConnection.getOutputStream().write(myData.getBytes(StandardCharsets.UTF_8));

                    // Recive
                    BufferedReader streamReader = new BufferedReader(new InputStreamReader(myConnection.getInputStream(), "UTF-8"));
                    StringBuilder responseStrBuilder = new StringBuilder();

                    // Deserialize
                    String inputStr;
                    while ((inputStr = streamReader.readLine()) != null)
                        responseStrBuilder.append(inputStr);
                    Log.d("json..: ", responseStrBuilder.toString());
                    JSONObject json = new JSONObject(responseStrBuilder.toString());

                    // Get Json'data In String
                    String sera = recurseKeys(json, "success");


                } catch (IOException e) {
                    Log.d("what",e.getMessage());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void loginSinAsync(String correo, String password){

        String stringUrl = "http://107.170.206.68:41062/www/pc/gps/login.php";

        try {

            //URL
            URL url = new URL(stringUrl);
            //Connection
            HttpURLConnection myConnection = (HttpURLConnection) url.openConnection();
            // Method
            myConnection.setRequestMethod("POST");
            // Headers
            myConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            myConnection.setRequestProperty("charset", "utf-8");

            // Create the data
            //String myData = "correo=ginppian@gmail.com&password=1234";
            String myData = "correo="+correo+"&"+"password="+password;

            // Enable writing
            myConnection.setDoOutput(true);
            myConnection.setInstanceFollowRedirects(false);

            // Write the data
            myConnection.getOutputStream().write(myData.getBytes(StandardCharsets.UTF_8));

            // Recive
            BufferedReader streamReader = new BufferedReader(new InputStreamReader(myConnection.getInputStream(), "UTF-8"));
            StringBuilder responseStrBuilder = new StringBuilder();

            // Deserialize
            String inputStr;
            while ((inputStr = streamReader.readLine()) != null)
                responseStrBuilder.append(inputStr);
            Log.d("json..: ", responseStrBuilder.toString());
            JSONObject json = new JSONObject(responseStrBuilder.toString());

            // Get Json'data In String
            String jsonAuthorized = recurseKeys(json, "success");

            if(jsonAuthorized == "true"){
                authorize = true;
            } else {
                authorize = false;
            }

        } catch (IOException e) {
            Log.d("what",e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static String recurseKeys(JSONObject jObj, String findKey) throws JSONException {
        String finalValue = "";
        if (jObj == null) {
            return "";
        }

        Iterator<String> keyItr = jObj.keys();
        Map<String, String> map = new HashMap<>();

        while(keyItr.hasNext()) {
            String key = keyItr.next();
            map.put(key, jObj.getString(key));
        }

        for (Map.Entry<String, String> e : (map).entrySet()) {
            String key = e.getKey();
            if (key.equalsIgnoreCase(findKey)) {
                return jObj.getString(key);
            }

            // read value
            Object value = jObj.get(key);

            if (value instanceof JSONObject) {
                finalValue = recurseKeys((JSONObject)value, findKey);
            }
        }

        // key is not found
        return finalValue;
    }

    private void loadView(){

        imgLogo = (ImageView) findViewById(R.id.imageView);
        imgPerfil = (ImageView) findViewById(R.id.imageView2);
        imgPass = (ImageView) findViewById(R.id.imageView3);
        inputPerfil = (EditText) findViewById(R.id.editText);
        inputPass = (EditText) findViewById(R.id.editText2);
        boton = (Button) findViewById(R.id.button);

    }

}




