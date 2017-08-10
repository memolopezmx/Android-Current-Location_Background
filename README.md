Android Get Current Location In Background
=======

<p align="center">
	<img src="https://github.com/ginppian/Android-Current-Location_Background/blob/master/imgs/img2.png" width="270" height="480">
		<img src="https://github.com/ginppian/Android-Current-Location_Background/blob/master/imgs/img3.png" width="270" height="480">
			<img src="https://github.com/ginppian/Android-Current-Location_Background/blob/master/imgs/img1.png" width="270" height="480">


</p>



## Descripción

<p align="justify">
	Este tutorial es una continuación de una versión <a href="https://github.com/ginppian/Android-Get_Location">anterior</a> donde implementamos la localización en background.	
</p>

## Desarrollo

<p align="justify">
	Para lograr la ejecución en segundo plano tenemos que hacer uso de un servicio que implemente a la clase <i>Location</i>.
</p>

* Agregamos lo siguiente al archivo *AndroidManifest.xml*

```java
<service android:name=".MyService" android:process=":my_service" />
```

<p align="justify">De tal manera que quede así</p>

```java
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="mx.paracrecer.paracrecer1">

    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.DOWNLOAD_WITHOUT_NOTIFICATION" />

    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <!--
         The ACCESS_COARSE/FINE_LOCATION permissions are not required to use
         Google Maps Android API v2, but you must specify either coarse or fine
         location permissions for the 'MyLocation' functionality. 
    -->

    <meta-data
        android:name="com.google.android.geo.API_KEY"
        android:value="@string/google_maps_key" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity android:name=".MainActivity"
            android:screenOrientation="portrait">

            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
|                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity android:name=".Ubicacion" />
        <!--
             The API key for Google Maps-based APIs is defined as a string resource.
             (See the file "res/values/google_maps_api.xml").
             Note that the API key is linked to the encryption key used to sign the APK.
             You need a different API key for each encryption key, including the release key that is used to
             sign the APK for publishing.
             You can define the keys for the debug and release targets in src/debug/ and src/release/. 
        -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="@string/google_maps_key" />

        <activity
            android:name=".MapsActivity"
            android:label="@string/title_activity_maps"
            android:screenOrientation="portrait" />

        <service android:name=".MyService" android:process=":my_service" />
    </application>

</manifest>
```

* Creamos nuestro servicio implementando de manera anidada *LocationListener*

```java
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class MyService extends Service {
    private static final String TAG = "BOOMBOOMTESTGPS";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 0;
    public MyService() {
    }

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            Log.e(TAG, "LocationListener " + provider);
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            Log.e(TAG, "onLocationChanged: " + location);
            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider)
        {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider)
        {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            Log.e(TAG, "onStatusChanged: " + provider);
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[] {
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.e(TAG, "onStartCommand");
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        Log.e(TAG, "onCreate");
        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "network provider does not exist, " + ex.getMessage());
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }
    }

    @Override
    public void onDestroy()
    {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
        if (mLocationManager != null) {
            for (int i = 0; i < mLocationListeners.length; i++) {
                try {
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                } catch (Exception ex) {
                    Log.i(TAG, "fail to remove location listners, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager() {
        Log.e(TAG, "initializeLocationManager");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }
    /*
    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }*/
}
```

* Implementamos en nuestros mapas

```java
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    // Share Info
    private Bundle bundle;

    // Google Maps
    private GoogleMap mMap;
    private Marker marcador;
    double lat = 0.0;
    double lng = 0.0;

    // Usr
    String correo = "";

    // Location
    private LocationManager mLocationManager;
    private LocationListener mLocationListener;
    private static double currentLat = 0;
    private static double currentLon = 0;
    private CircleOptions circle;
    double radiusInMeters = 50.0;
    int strokeColor = 0x116ED1; //red outline
    int shadeColor = 0x44116ED1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        loadView();
    }

    private void loadView() {

        // Get Var MainActivity
        bundle = getIntent().getExtras();
        this.correo = bundle.getString("miCorreo").toString();

        // Set title ActionBar
        //getActionBar().setTitle(correo);
        //getSupportActionBar().setTitle(correo);

        // Empezamos el servicio
        onStartService();
    }

    // Block Back Button
    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    // Block Back
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Agregamos boton de ubicación en el mapa para que el usuario pueda mostrar su ubicación por el mismo
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                    mMap.setMyLocationEnabled(true);

        } else {

            // Show rationale and request permission.
            Toast.makeText(
                    getBaseContext(),
                    "Por favor habilite los permisos de ubicación para esta aplicación",
                    Toast.LENGTH_SHORT).
                    show();

        }
    }

    // Empezamos el servicio
    private void onStartService(){

        addListenerLocation();
    }

    private void addListenerLocation() {

        // Obtenemos la localización
        mLocationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        // Mantenemos escuchando algun cambio de ubicación
        mLocationListener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {

                // Obtenemos las coordenadas
                currentLat = location.getLatitude();
                currentLon = location.getLongitude();

                // Creamos un nuevo objeto coordenada y actualizamos el mapa para el usuario
                LatLng coordenadas = new LatLng(currentLat, currentLon);
                CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 17);
                mMap.animateCamera(miUbicacion);

                circle = new CircleOptions().center(coordenadas).radius(radiusInMeters).fillColor(shadeColor).strokeColor(strokeColor).strokeWidth(3);
                mMap.addCircle(circle);

                // Mostramos un mensaje al usuario de la última ubicación
                Toast.makeText(getBaseContext(), currentLat + "-" + currentLon, Toast.LENGTH_SHORT).show();

                // Enviamos los datos a la DB de tiempo Real
                actualizarUbicacionDBTiempoReal(location);

            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("location", "onStatusChanged");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("location", "onProviderEnabled");


            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("location", "onProviderDisabled");

            }
        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        // Siempre que se actualice la ubicación llamamos al objeto que está escuchando
        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 500, 10, mLocationListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(mLocationListener);
    }

    private void actualizarUbicacionDBTiempoReal(Location location) {
        if (location != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();

            // Obtenemos las coordenadas
            lat = location.getLatitude();
            lng = location.getLongitude();

            // Agregamos el marcador
            //agregarMarcador(lat, lng);

            // Formato de cadena a las coordenadas
            String sLat = String.format("%f", lat);
            String sLng = String.format("%f", lng);

            // Obtenemos la fecha
            DateFormat df = new SimpleDateFormat("dd-MM-yyyy,HH:mm");
            String date = df.format(Calendar.getInstance().getTime());

            // Obtenemos el usuario
            String[] splitCorreo = this.correo.split("@");
            String usuario = splitCorreo[0];

            // Obtenemos la ruta
            String pathLat = "usuarios/"+usuario+"/"+date+"/"+"lat";
            String pathLng = "usuarios/"+usuario+"/"+date+"/"+"lng";

            // Ejecutamos el cambio en Firebase

            database.getReference(pathLat).setValue(sLat);
            database.getReference(pathLng).setValue(sLng);
        }
    }

    // Aun que no los usemos nos pide implementarlos por la definición

    @Override
    public void onLocationChanged(Location location) {
        Log.d("location", "onLocationChanged");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("location", "onStatusChanged");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("location", "onProviderEnabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("location", "onProviderDisabled");
    }

}
```

## Fuente

* <a href="http://www.magicsite.cn/a104-89869-android">Best way to get user GPS location in background in Android</a>
* <a href="https://stackoverflow.com/questions/28535703/best-way-to-get-user-gps-location-in-background-in-android">Best way to get user GPS location in background in Android</a>
* <a href="https://stackoverflow.com/questions/14478179/background-service-with-location-listener-in-android">Background service with location listener in android</a>

## Testing

* <a href="https://elandroidelibre.elespanol.com/2016/07/aplicaciones-enganar-gps-movil.html">GPS falso para WhatsApp</a>

## Otra Posibilidad

* <a href="https://www.youtube.com/watch?v=espgyDyIm5Q">GcmNetworkManager Scheduling Task on Android - The easiest way </a>