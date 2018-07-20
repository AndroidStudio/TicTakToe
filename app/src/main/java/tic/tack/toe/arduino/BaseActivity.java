package tic.tack.toe.arduino;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONObject;

import tic.tack.toe.arduino.dialog.MessageDialog;
import tic.tack.toe.arduino.sockets.MessageListener;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.viewmodel.SocketViewModel;

@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity implements MessageListener {

    private static final int REQUEST_LOCATION_ACCESS = 1000;
    protected static final int REQUEST_ENABLE_BT = 1001;

    private SocketViewModel mViewModel;
    private AlertDialog mMessageDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mViewModel = ViewModelProviders.of(this)
                .get(SocketViewModel.class);
        this.mViewModel.addMessageListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            this.finish();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION},
                REQUEST_LOCATION_ACCESS);
    }

    @SuppressWarnings("all")
    protected boolean checkPermissions() {
        int accessFineLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        int accessCoarseLocation = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION);
        return accessFineLocation == PackageManager.PERMISSION_GRANTED
                && accessCoarseLocation == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_ACCESS:
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.mViewModel.removeMessageListener(this);
    }

    protected void setMessage(JSONObject message) {
        this.mViewModel.sendMessage(message.toString());
    }

    @Override
    public void onMessage(String message) {
        try {
            JSONObject responseObject = new JSONObject(message);
            String type = responseObject.getString(SocketConstants.TYPE);
            switch (type) {
                case SocketConstants.EXIT_GAME:
                    this.exitGame();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void exitGame() {
        this.hideMessageDialog();

        this.mMessageDialog = MessageDialog.displayDialog(this,
                "Gracz 2 opuscił grę");
        this.mMessageDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                Intent intent = new Intent(BaseActivity.this,
                        InitDeviceActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void hideMessageDialog() {
        if (this.mMessageDialog != null && this.mMessageDialog.isShowing()) {
            this.mMessageDialog.dismiss();
        }
    }

    public void disconnectClient() {
        this.mViewModel.disconnectClient();
    }
}
