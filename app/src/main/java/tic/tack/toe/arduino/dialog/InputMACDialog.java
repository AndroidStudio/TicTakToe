package tic.tack.toe.arduino.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import tic.tack.toe.arduino.MainActivity;
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

        final RadioGroup radioGroup = findViewById(R.id.radio_group);
        findViewById(R.id.saveButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int checkedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                String mac = null;
                switch (checkedRadioButtonId) {
                    case R.id.radio_01:
                        RadioButton radioButton_1 = findViewById(checkedRadioButtonId);
                        mac = radioButton_1.getText().toString();
                        break;
                    case R.id.radio_02:
                        RadioButton radioButton_2 = findViewById(checkedRadioButtonId);
                        mac = radioButton_2.getText().toString();
                        break;
                }

                dismiss();
                if (TextUtils.isEmpty(mac)) {
                    Intent intent = new Intent(context, MainActivity.class);
                    context.startActivity(intent);
                    context.finish();
                    return;
                }

                GameSettings.getInstance().setMacAddress(mac);
                Intent intent = new Intent(context, ScanActivity.class);
                context.startActivity(intent);
                context.finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.context.finish();
        System.exit(0);
    }
}


