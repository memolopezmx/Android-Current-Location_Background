package mx.paracrecer.paracrecer1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;

public class Ubicacion extends AppCompatActivity {

    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ubicacion);

        loadView();
    }

    private void loadView(){

        // Get Var MainActivity
        bundle = getIntent().getExtras();
        String correo = bundle.getString("miCorreo").toString();

        // Set title ActionBar
        getSupportActionBar().setTitle(correo);
    }

    // Block Back Button
    @Override
    public void onBackPressed() {
        moveTaskToBack(false);
    }

    // Block Back
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode== KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

}
