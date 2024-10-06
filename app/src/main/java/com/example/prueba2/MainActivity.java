package com.example.prueba2;
import android.content.Context;
import android.content.DialogInterface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private EditText etNombre, etRUT, etDescripcion;
    private TextView tvHora;
    private Spinner spinnerLaboratorios;
    private Button btnGrabar;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean isVertical = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etNombre = findViewById(R.id.etNombre);
        etRUT = findViewById(R.id.etRUT);
        etDescripcion = findViewById(R.id.etDescripcion);
        tvHora = findViewById(R.id.tvHora);
        spinnerLaboratorios = findViewById(R.id.spinnerLaboratorios);
        btnGrabar = findViewById(R.id.btnGrabar);

        //fecha y hora local de Chile
        mostrarHoraLocalChile();

        //sensor del acelerómetro
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        btnGrabar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validaRut(etRUT.getText().toString())) {
                    mostrarDialogoGrabacion();
                } else {
                    Toast.makeText(MainActivity.this, "RUT inválido", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //fecha y hora local de Chile
    private void mostrarHoraLocalChile() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        TimeZone timeZone = TimeZone.getTimeZone("America/Santiago");
        sdf.setTimeZone(timeZone);
        String horaChile = sdf.format(calendar.getTime());
        tvHora.setText(horaChile);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float z = event.values[2];

        //inclinacion del dispositivo
        if (z > 9 && !isVertical) {
            isVertical = true;
            mostrarDialogoGrabacion();
        } else if (z < 9) {
            isVertical = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private void mostrarDialogoGrabacion() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Confirmar registro")
                .setMessage("¿Está seguro que desea grabar el incidente?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(MainActivity.this, "Incidente grabado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private boolean validaRut(String rut) {
        if (rut.length() < 8) return false;
        String[] rutParts = rut.split("-");
        if (rutParts.length != 2) return false;
        String cuerpo = rutParts[0];
        String dv = rutParts[1].toLowerCase();

        int suma = 0, factor = 2;
        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(cuerpo.charAt(i)) * factor;
            factor = factor == 7 ? 2 : factor + 1;
        }
        int mod = 11 - (suma % 11);
        String dvEsperado = mod == 11 ? "0" : mod == 10 ? "k" : String.valueOf(mod);

        return dvEsperado.equals(dv);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
}
