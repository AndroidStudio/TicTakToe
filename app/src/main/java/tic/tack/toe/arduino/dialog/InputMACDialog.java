package tic.tack.toe.arduino.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RadioGroup;
import android.widget.Toast;

import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.ScanActivity;
import tic.tack.toe.arduino.game.GameSettings;

public class InputMACDialog extends Dialog {

    private final Activity context;

    public InputMACDialog(Activity context) {
        super(context, R.style.DialogStyle);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_mac_dialog);

        RadioGroup radioGroup = findViewById(R.id.radio_group);
        findViewById(R.id.saveButton).setOnClickListener(v -> {
            int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
            String mac = null;
            switch (checkedRadioButtonId) {
                case R.id.radio_01:
                    mac = "C4:2B:2D:00:FF:1D";
                    break;
                case R.id.radio_02:
                    mac = "D7:90:F4:FE:A4:55";
                    break;
            }

            dismiss();
            if (TextUtils.isEmpty(mac)) {
                Toast.makeText(context, "Proszę wybrać koszulkę", Toast.LENGTH_LONG).show();
                return;
            }

            GameSettings.getInstance().setMacAddress(mac, context);
            Intent intent = new Intent(context, ScanActivity.class);
            context.startActivity(intent);
            context.finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.context.finish();
        System.exit(0);
    }
}


