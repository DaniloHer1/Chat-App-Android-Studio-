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
    private static float LIGHT = 50.0f;

    private SensorManager sensorManager;
    private Sensor lightSensor;
    private OnThemeChangeListener themeChangedListener;
    private Context context;
    private boolean isDarkMode;


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

        if (lightSensor == null) {
            Log.e("LIGHT", "Sensor de luz no disponible en este dispositivo");
        } else {
            Log.d("LIGHT", "Sensor de luz inicializado");
        }

    }

    public void startListening() {
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, sensorManager.SENSOR_DELAY_NORMAL);
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
            boolean sholdBeDark = lightLevel < LIGHT;

            if (sholdBeDark != isDarkMode) {
                isDarkMode = sholdBeDark;

                SharedPreferences preferences = context.getSharedPreferences("theme", Context.MODE_PRIVATE);
                preferences.edit().putBoolean("is_dark_mode", isDarkMode).apply();

                if (themeChangedListener!=null){
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
