package utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class LightSensorManager implements SensorEventListener {


    // Umbral de luz, mas o menos la mitad
    private static final float LIGHT_DARK = 100.0f;  // Umbral para cambiar a modo oscuro
    private static final float LIGHT_LIGHT = 150.0f;// Umbral para cambiar a modo Blanco
    private static final long CHANGE_DELAY_MS = 5000;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private OnThemeChangeListener themeChangedListener;
    private Context context;
    private boolean isDarkMode;
    private long lastChangeTime = 0;

    private boolean isChangingTheme = false;

    public interface OnThemeChangeListener {
        void onThemeChanged(boolean isDarkMode);

    }

    public LightSensorManager(Context context, OnThemeChangeListener listener) {

        this.context = context;
        this.themeChangedListener = listener;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        SharedPreferences preferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        isDarkMode = preferences.getBoolean("is_dark_mode", false);

        lastChangeTime = System.currentTimeMillis();

        if (lightSensor == null) {
            Log.e("LIGHT", "Sensor de luz no disponible en este dispositivo");
        } else {
            Log.d("LIGHT", "Sensor de luz inicializado");
        }

    }

    public void startListening() {
        if (lightSensor != null && sensorManager != null) {
            boolean registered = sensorManager.registerListener(
                    this,
                    lightSensor,
                    SensorManager.SENSOR_DELAY_NORMAL
            );

            if (registered) {
                Log.d("LIGHT_SENSOR", "Listener registrado");
                // Resetear la bandera cuando se vuelve a registrar
                isChangingTheme = false;
            } else {
                Log.e("LIGHT_SENSOR", " Error al registrar listener");
            }
        }
    }

    public void stopListening() {
        if (lightSensor != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            float lightLevel = event.values[0];
            if (isChangingTheme){
                return;
            }
            boolean sholdBeDark;

            if (isDarkMode) {
                sholdBeDark = lightLevel < LIGHT_LIGHT;
            } else {
                sholdBeDark = lightLevel < LIGHT_DARK;
            }
            long currentTime = System.currentTimeMillis();
            boolean enoughTimePassed = (currentTime - lastChangeTime) > CHANGE_DELAY_MS;


            if (sholdBeDark != isDarkMode && enoughTimePassed) {
                Log.d("LIGHT_SENSOR", "═════════════════════════════════");
                Log.d("LIGHT_SENSOR", "CAMBIO DE TEMA DETECTADO");
                Log.d("LIGHT_SENSOR", "  Nivel de luz: " + lightLevel + " lux");
                Log.d("LIGHT_SENSOR", "  Modo anterior: " + (isDarkMode ? "OSCURO" : "CLARO"));
                Log.d("LIGHT_SENSOR", "  Modo nuevo: " + (sholdBeDark ? "OSCURO" : "CLARO"));
                Log.d("LIGHT_SENSOR", "═════════════════════════════════");

                isDarkMode = sholdBeDark;
                lastChangeTime = currentTime;

                // Guardar el nuevo tema
                SharedPreferences preferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
                preferences.edit().putBoolean("is_dark_mode", isDarkMode).apply();

                // Notificar al listener
                if (themeChangedListener != null) {
                    themeChangedListener.onThemeChanged(isDarkMode);
                }
            }
        }
    }


    public static boolean getSavedTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
        return prefs.getBoolean("is_dark_mode", false);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
