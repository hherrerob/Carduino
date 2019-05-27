package com.hector.carduino;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Intenta cargar una ubicación del vehículo
 * Muestra la ubicación en un mapa junto con los datos propios de ésta
 */
public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    /** Permiso que pedir para usar la geolocalización */
    private static final int REQUEST = 112;

    /** Mapa */
    private GoogleMap mMap;
    /** Displays de los datos */
    private TextView address, state, country;
    /** Botón de salida */
    private FloatingActionButton backButton;
    /** Estado del coche */
    private Status currentStatus;
    /** Configuración guardada */
    private Settings settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        this.backButton = findViewById(R.id.BACK_BUTTON);
        this.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsActivity.this.finish();
            }
        });
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipula el mapa cuando esté disponible.
     * Este callback se dispara cuando el mapa está listo
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.silver_map));
        set();
    }

    /**
     * Recoge vistas de los componentes
     * Comprueba que haya permisos
     * Carga una localización según el status, la configuración y la conexión
     */
    public void set() {
        this.settings = new Settings();
        this.settings.getPrefs(MapsActivity.this);
        this.address = findViewById(R.id.DIRECCION);
        this.state = findViewById(R.id.STATE);
        this.country = findViewById(R.id.PAIS);

        boolean isConnected = getIntent().getExtras().getBoolean("isConnected");
        this.currentStatus = (Status) getIntent().getExtras().getSerializable("currentStatus");

        if(isConnected) {
            // Comprobar permisos
            if(settings.is_usePhoneLoc()) {
                if (Build.VERSION.SDK_INT >= 23) {
                    String[] PERMISSIONS = {android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION};
                    if (!hasPermissions(this, PERMISSIONS)) {
                        ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST);
                    } else {
                        getLocation();
                    }
                } else {
                    getLocation();
                }
            } else {
                double lat = currentStatus.get_lat();
                double lon = currentStatus.get_lon();

                if(lat != 0 && lon != 0) {
                    saveLocation(lat, lon);
                    setLocation(lat, lon);
                } else {
                    useSavedLoc();
                }
            }
        } else {
            useSavedLoc();
        }
    }

    /**
     * Carga la última localización guardada
     */
    public void useSavedLoc() {
        settings.getPrefs(this);
        try {
            setLocation((double) settings.get_lastLat(), (double) settings.get_lastLong());
        } catch (NullPointerException e) {
        } catch (IndexOutOfBoundsException e) { }
    }

    /**
     * Pide los permisos de los necesarios para usar la localización del teléfono
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();
                }
            }
        }
    }

    /**
     * Comprueba si tiene permisos
     * @param context
     * @param permissions
     * @return true si tiene permisos, false si no
     */
    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Busca la situación del GPS del teléfono, si no puede, carga la última guardada
     */
    public void getLocation() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        @SuppressLint("MissingPermission")
        Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        try {
            double longitude = location.getLongitude();
            double latitude = location.getLatitude();

            saveLocation(latitude, longitude);
            setLocation(latitude, longitude);
        } catch (NullPointerException e) {
            useSavedLoc();
        }
    }

    /**
     * Guarda la última ubicación cargada
     * @param latitude (double) Latitud
     * @param longitude (double) Longitud
     */
    public void saveLocation(double latitude, double longitude) {
        this.settings = new Settings();
        this.settings.getPrefs(this);
        this.settings.set_lastLat((float) latitude);
        this.settings.set_lastLong((float) longitude);
        this.settings.savePrefs(this);
    }

    /**
     * Carga una ubicación
     * @param latitude (double) Latitud
     * @param longitude (double) Longitud
     */
    public void setLocation(double latitude, double longitude) {
        LatLng position = new LatLng(latitude, longitude);
        if(latitude != 0 && longitude != 0) {
            mMap.addMarker(new MarkerOptions().position(position).title(getString(R.string.map_yah)));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
            try {
                getAddress(latitude, longitude);
            } catch (IOException e) { }
        }
    }

    /**
     * Obtiene una dirección a partir de la longitud y latitud
     * @param latitude (double) Latitud
     * @param longitude (double) Longitud
     * @throws IOException
     */
    public void getAddress(double latitude, double longitude) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        addresses = geocoder.getFromLocation(latitude, longitude, 1);

        String address = addresses.get(0).getAddressLine(0).split(",")[0];
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();

        this.address.setText(address + ", " + knownName + ", " + postalCode);
        this.state.setText(city + ", " + state);
        this.country.setText(country);
    }
}
