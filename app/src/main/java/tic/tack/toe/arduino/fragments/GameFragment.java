package tic.tack.toe.arduino.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Objects;

import tic.tack.toe.arduino.CustomGridView;
import tic.tack.toe.arduino.MainActivity;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.bluetooth.BleManager;
import tic.tack.toe.arduino.game.ARDUINO_CMD;
import tic.tack.toe.arduino.game.FieldType;
import tic.tack.toe.arduino.game.GameSettings;

import static android.graphics.PorterDuff.Mode.SRC_IN;

public class GameFragment extends BaseFragment implements View.OnClickListener {

    private static final FieldType START_PLAYER = FieldType.PLAYER_01;

    private final GameSettings mGameSettings = GameSettings.getInstance();
    private final FieldType[] mFieldTypeArray = new FieldType[9];

    private FieldType mCurrentPlayer = START_PLAYER;

    private ImageView mPlayer_01ImageView;
    private ImageView mPlayer_02ImageView;
    private CustomGridView mGameGrid;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.game_layout, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.newGameButton).setOnClickListener(this.mResetGameClickListener);

        this.mPlayer_01ImageView = view.findViewById(R.id.mPlayer_01ImageView);
        this.mPlayer_02ImageView = view.findViewById(R.id.mPlayer_02ImageView);
        this.mGameGrid = view.findViewById(R.id.gridLayout);

        this.initGame();
    }

    private final View.OnClickListener mResetGameClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            FragmentController.setCurrentFragment(Objects.requireNonNull(getActivity()),
                    new GameSymbolFragment());
        }
    };

    private void initGame() {
        this.mPlayer_01ImageView.setImageResource(this.mGameSettings.getPlayer_01Symbol());
        this.mPlayer_02ImageView.setImageResource(this.mGameSettings.getPlayer_02Symbol());

        int length = this.mFieldTypeArray.length;
        for (int i = 0; i < length; i++) {
            this.mFieldTypeArray[i] = FieldType.EMPTY;
            View view = this.mGameGrid.getChildAt(i);
            view.setTag(i);
            view.setOnClickListener(this);
        }

        if (this.mCurrentPlayer == FieldType.PLAYER_01) {
            this.mPlayer_01ImageView.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_01Color()), SRC_IN));
            this.mPlayer_02ImageView.clearColorFilter();
        } else {
            this.mPlayer_01ImageView.clearColorFilter();
            this.mPlayer_02ImageView.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_02Color()), SRC_IN));
        }

        this.setBrightness(this.mGameSettings.getLedBrightness());
    }

    @Override
    public void onClick(View view) {
        int index = (int) view.getTag();
        this.onFieldClick(index);
    }

    private void onFieldClick(int index) {
        FieldType fieldType = this.mFieldTypeArray[index];
        if (FieldType.EMPTY != fieldType) {
            return;
        }

        String indexHex = String.format(Locale.getDefault(), "%02d", index);
        String message = ARDUINO_CMD.PIXEL + indexHex + this.mGameSettings.getPlayer_01Color();

        this.writeMessage(hexStringToByteArray(message));
        this.mFieldTypeArray[index] = getCurrentPlayer();
        this.updateUI(index);
        this.checkWin();
    }

    private BleManager getBleManager() {
        return ((MainActivity) Objects.requireNonNull(getActivity())).mBleManager;
    }

    public void setBrightness(int brightness) {
        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.putInt(brightness);
        String brightnessHexValue = toHexString(byteBuffer.array());

        brightnessHexValue = brightnessHexValue.substring(brightnessHexValue.length() - 2,
                brightnessHexValue.length());

        String message = ARDUINO_CMD.BRIGHTNESS + brightnessHexValue;
        this.writeMessage(hexStringToByteArray(message));
    }

    private byte[] hexStringToByteArray(String value) {
        int length = value.length();
        byte[] data = new byte[length / 2];
        for (int i = 0; i < length; i += 2) {
            data[i / 2] = (byte) ((Character.digit(value.charAt(i), 16) << 4)
                    + Character.digit(value.charAt(i + 1), 16));
        }
        return data;
    }

    private String toHexString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) stringBuilder.append(String.format("%x", b));
        return stringBuilder.toString();
    }

    private void checkCurrentState() {
        int currentState = this.getBleManager().getCurrentState();
        if (currentState != BleManager.STATE_CONNECTED) {
            Toast.makeText(getActivity(), "Brak połącznenia z bluetooth...", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeMessage(byte[] message) {
        this.checkCurrentState();

        BleManager bleManager = this.getBleManager();
        bleManager.writeService(bleManager.getGattService(
                "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"),
                "6E400002-B5A3-F393-E0A9-E50E24DCCA9E", message);
    }

    private void win(FieldType fieldType) {
        String message;
        if (fieldType == FieldType.PLAYER_01) {
            message = "gracz 2";
        } else {
            message = "gracz 1";
        }

        this.displayMessageDialog("Wygrywa: " + message);
        this.reset();
    }

    private void displayMessageDialog(String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(Objects
                .requireNonNull(getActivity()));
        alertDialogBuilder.setTitle("Komunikat");
        alertDialogBuilder.setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    public void reset() {
        this.mCurrentPlayer = START_PLAYER;

        int childCount = this.mGameGrid.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.mFieldTypeArray[i] = FieldType.EMPTY;
            ImageView view = (ImageView) mGameGrid.getChildAt(i);
            view.setImageDrawable(null);
            view.clearColorFilter();
        }

        if (this.mCurrentPlayer == FieldType.PLAYER_01) {
            this.mPlayer_01ImageView.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_01Color()), SRC_IN));
            this.mPlayer_02ImageView.clearColorFilter();
        } else {
            this.mPlayer_01ImageView.clearColorFilter();
            this.mPlayer_02ImageView.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_02Color()), SRC_IN));
        }

        this.writeMessage(hexStringToByteArray(ARDUINO_CMD.RESET));
    }

    private void updateUI(int index) {
        ImageView view = (ImageView) mGameGrid.getChildAt(index);
        if (this.mCurrentPlayer == FieldType.PLAYER_02) {
            view.setImageResource(this.mGameSettings.getPlayer_01Symbol());
            view.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_01Color()), SRC_IN));
        } else {
            view.setImageResource(this.mGameSettings.getPlayer_02Symbol());
            view.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_02Color()), SRC_IN));
        }
    }

    private FieldType getCurrentPlayer() {
        final FieldType currentPlayer;
        if (this.mCurrentPlayer == FieldType.PLAYER_02) {
            this.mPlayer_01ImageView.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_01Color()), SRC_IN));
            this.mPlayer_02ImageView.clearColorFilter();
            currentPlayer = FieldType.PLAYER_01;
        } else {
            this.mPlayer_01ImageView.clearColorFilter();
            this.mPlayer_02ImageView.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_02Color()), SRC_IN));
            currentPlayer = FieldType.PLAYER_02;
        }

        this.mCurrentPlayer = currentPlayer;
        return currentPlayer;
    }

    private void checkWin() {
        this.checkLine1();
        this.checkLine2();
        this.checkLine3();
        this.checkLine4();
        this.checkLine5();
        this.checkLine6();
        this.checkLine7();
        this.checkLine8();

        for (int i = 0; i < 9; i++) {
            FieldType fieldType = this.mFieldTypeArray[i];
            if (fieldType == FieldType.EMPTY) {
                return;
            }
        }

        this.reset();
    }

    private void checkLine1() {
        FieldType v1 = this.mFieldTypeArray[0];
        FieldType v2 = this.mFieldTypeArray[1];
        FieldType v3 = this.mFieldTypeArray[2];
        if (v1 == v2 && v1 == v3 && v1 != FieldType.EMPTY) {
            this.win(v1);
        }
    }

    private void checkLine2() {
        FieldType v1 = this.mFieldTypeArray[3];
        FieldType v2 = this.mFieldTypeArray[4];
        FieldType v3 = this.mFieldTypeArray[5];
        if (v1 == v2 && v1 == v3 && v1 != FieldType.EMPTY) {
            this.win(v1);
        }
    }

    private void checkLine3() {
        FieldType v1 = this.mFieldTypeArray[6];
        FieldType v2 = this.mFieldTypeArray[7];
        FieldType v3 = this.mFieldTypeArray[8];
        if (v1 == v2 && v1 == v3 && v1 != FieldType.EMPTY) {
            this.win(v1);
        }
    }

    private void checkLine4() {
        FieldType v1 = this.mFieldTypeArray[0];
        FieldType v2 = this.mFieldTypeArray[3];
        FieldType v3 = this.mFieldTypeArray[6];
        if (v1 == v2 && v1 == v3 && v1 != FieldType.EMPTY) {
            this.win(v1);
        }
    }

    private void checkLine5() {
        FieldType v1 = this.mFieldTypeArray[1];
        FieldType v2 = this.mFieldTypeArray[4];
        FieldType v3 = this.mFieldTypeArray[7];
        if (v1 == v2 && v1 == v3 && v1 != FieldType.EMPTY) {
            this.win(v1);
        }
    }

    private void checkLine6() {
        FieldType v1 = this.mFieldTypeArray[2];
        FieldType v2 = this.mFieldTypeArray[5];
        FieldType v3 = this.mFieldTypeArray[8];
        if (v1 == v2 && v1 == v3 && v1 != FieldType.EMPTY) {
            this.win(v1);
        }
    }

    private void checkLine7() {
        FieldType v1 = this.mFieldTypeArray[0];
        FieldType v2 = this.mFieldTypeArray[4];
        FieldType v3 = this.mFieldTypeArray[8];
        if (v1 == v2 && v1 == v3 && v1 != FieldType.EMPTY) {
            this.win(v1);
        }
    }

    private void checkLine8() {
        FieldType v1 = this.mFieldTypeArray[2];
        FieldType v2 = this.mFieldTypeArray[4];
        FieldType v3 = this.mFieldTypeArray[6];
        if (v1 == v2 && v1 == v3 && v1 != FieldType.EMPTY) {
            this.win(v1);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.reset();
    }
}
