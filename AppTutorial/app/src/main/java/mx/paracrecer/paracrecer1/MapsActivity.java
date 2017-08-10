package mx.paracrecer.paracrecer1;

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
