package io.github.pangziqiang.misharenolimit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String PREFS = "mishare_nolimit";
    private static final String KEY_REBOOT_REQUESTED = "reboot_requested";

    private TextView statusText;
    private Button rebootButton;
    private Button laterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusText = findViewById(R.id.status_text);
        rebootButton = findViewById(R.id.reboot_button);
        laterButton = findViewById(R.id.later_button);

        SharedPreferences prefs = getSharedPreferences(PREFS, MODE_PRIVATE);
        boolean rebootRequested = prefs.getBoolean(KEY_REBOOT_REQUESTED, false);

        if (!rebootRequested) {
            showRebootUi();
        } else {
            statusText.setVisibility(View.GONE);
            rebootButton.setVisibility(View.GONE);
            laterButton.setVisibility(View.GONE);
        }

        rebootButton.setOnClickListener(v -> {
            getSharedPreferences(PREFS, MODE_PRIVATE)
                    .edit()
                    .putBoolean(KEY_REBOOT_REQUESTED, true)
                    .apply();
            rebootDevice();
        });

        laterButton.setOnClickListener(v -> hideRebootUi());
    }

    private void showRebootUi() {
        statusText.setVisibility(View.VISIBLE);
        rebootButton.setVisibility(View.VISIBLE);
        laterButton.setVisibility(View.VISIBLE);
    }

    private void hideRebootUi() {
        statusText.setVisibility(View.GONE);
        rebootButton.setVisibility(View.GONE);
        laterButton.setVisibility(View.GONE);
    }

    private void rebootDevice() {
        statusText.setText(R.string.ui_reboot_requested);
        rebootButton.setEnabled(false);
        laterButton.setEnabled(false);

        new Thread(() -> {
            boolean ok = runSuReboot();
            runOnUiThread(() -> {
                if (ok) {
                    hideRebootUi();
                } else {
                    getSharedPreferences(PREFS, MODE_PRIVATE)
                            .edit()
                            .putBoolean(KEY_REBOOT_REQUESTED, false)
                            .apply();
                    rebootButton.setEnabled(true);
                    laterButton.setEnabled(true);
                    statusText.setText(R.string.ui_reboot_failed);
                }
            });
        }).start();
    }

    private boolean runSuReboot() {
        try {
            Process p = new ProcessBuilder("su", "-c", "reboot").redirectErrorStream(true).start();
            int code = p.waitFor();
            return code == 0;
        } catch (Exception e) {
            try {
                Process p = new ProcessBuilder("reboot").redirectErrorStream(true).start();
                int code = p.waitFor();
                return code == 0;
            } catch (Exception e2) {
                runOnUiThread(() -> statusText.setText(R.string.ui_root_missing));
                return false;
            }
        }
    }
}
