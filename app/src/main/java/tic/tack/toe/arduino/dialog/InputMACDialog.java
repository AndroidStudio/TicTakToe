package tic.tack.toe.arduino.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

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

        final EditText macEditText = findViewById(R.id.macEditText);
        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String mac = macEditText.getText().toString();
                if (!TextUtils.isEmpty(mac)) {
                    GameSettings.getInstance().setMacAddress(mac);
                    dismiss();

                    Intent intent = new Intent(context, ScanActivity.class);
                    context.startActivity(intent);
                    context.finish();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        context.finish();
    }
}


