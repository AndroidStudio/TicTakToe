package tic.tack.toe.arduino.fragments;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Objects;

import tic.tack.toe.arduino.CustomGridView;
import tic.tack.toe.arduino.MainActivity;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.bluetooth.BleManager;
import tic.tack.toe.arduino.game.CMD;
import tic.tack.toe.arduino.game.FieldType;
import tic.tack.toe.arduino.game.GameSettings;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

import static android.graphics.PorterDuff.Mode.SRC_IN;

public class GameFragment extends BaseFragment implements View.OnClickListener, Runnable {

    private static final FieldType START_PLAYER = FieldType.PLAYER_01;

    private static final long GAME_UPDATE_DELAY = 500;
    private static final String TAG = "GameFragment";

    private final GameSettings mGameSettings = GameSettings.getInstance();
    private final FieldType[] mFieldTypeArray = new FieldType[9];
    private final Handler mGameHandler = new Handler();

    private FieldType mCurrentPlayer = START_PLAYER;

    private boolean mIsGameInitialized = false;

    private ImageView mPlayer_01ImageView;
    private ImageView mPlayer_02ImageView;
    private AlertDialog mMessageDialog;
    private CustomGridView mGameGrid;
    private boolean mClicked = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.game_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.newGameButton).setOnClickListener(this.mNewGameClickListener);

        this.mPlayer_01ImageView = view.findViewById(R.id.mPlayer_01ImageView);
        this.mPlayer_02ImageView = view.findViewById(R.id.mPlayer_02ImageView);
        this.mGameGrid = view.findViewById(R.id.gridLayout);

        this.mGameHandler.postDelayed(this, GAME_UPDATE_DELAY);
    }

    private final View.OnClickListener mNewGameClickListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            reset();
            newGame();
        }
    };

    private void newGame() {
        try {
            try {
                JSONObject requestObject = new JSONObject();
                requestObject.put(SocketConstants.TYPE, SocketConstants.NEW_GAME);
                requestObject.put(SocketConstants.UDID, UDID.getUDID());
                setMessage(requestObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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

        this.updateCurrentPlayer();
        this.setBrightness(this.mGameSettings.getLedBrightness());
        this.mIsGameInitialized = true;
    }

    private void updateCurrentPlayer() {
        if (this.mCurrentPlayer == FieldType.PLAYER_01) {
            this.mPlayer_01ImageView.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_01Color()), SRC_IN));
            this.mPlayer_02ImageView.clearColorFilter();
        } else {
            this.mPlayer_01ImageView.clearColorFilter();
            this.mPlayer_02ImageView.setColorFilter(new PorterDuffColorFilter(
                    Color.parseColor("#" + this.mGameSettings.getPlayer_02Color()), SRC_IN));
        }
    }

    @Override
    public void run() {
        try {
            try {
                JSONObject messageObject = new JSONObject();
                messageObject.put(SocketConstants.TYPE, SocketConstants.GAME_INFO);
                messageObject.put(SocketConstants.UDID, UDID.getUDID());

                setMessage(messageObject);
                Timber.tag(TAG).e("gameInfo");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.mGameHandler.postDelayed(this, GAME_UPDATE_DELAY);
    }

    @Override
    public void onMessage(String message) {
        super.onMessage(message);
        try {
            JSONObject responseObject = new JSONObject(message);
            String type = responseObject.getString(SocketConstants.TYPE);
            switch (type) {
                case SocketConstants.GAME_INFO:
                    updateGame(responseObject);
                    break;
                case SocketConstants.WIN:
                    win();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void win() {
        reset();
        displayMessageDialog("Wygrał przeciwnik");
    }

    private void updateGame(JSONObject responseObject) throws Exception {
        String status = responseObject.getString(SocketConstants.STATUS);
        if (status.equals(SocketConstants.AWAITING)) {
            return;
        }

        JSONArray playersInfo = responseObject.getJSONArray(SocketConstants.INFO);
        JSONObject playerObject_01 = playersInfo.getJSONObject(0);
        JSONObject playerObject_02 = playersInfo.getJSONObject(1);

        GameSettings gameSettings = GameSettings.getInstance();
        if (playerObject_01.getString(SocketConstants.UDID).equals(UDID.getUDID())) {
            if (!playerObject_01.isNull(SocketConstants.COLOR)
                    && !playerObject_02.isNull(SocketConstants.COLOR)) {
                gameSettings.setPlayer_01Color(playerObject_01.getString(SocketConstants.COLOR));
                gameSettings.setPlayer_02Color(playerObject_02.getString(SocketConstants.COLOR));
            }
            if (!playerObject_01.isNull(SocketConstants.SYMBOL)
                    && !playerObject_02.isNull(SocketConstants.SYMBOL)) {
                gameSettings.setPlayer_01Symbol(GameSettings.mSymbolArray[playerObject_01
                        .getInt(SocketConstants.SYMBOL)]);
                gameSettings.setPlayer_02Symbol(GameSettings.mSymbolArray[playerObject_02
                        .getInt(SocketConstants.SYMBOL)]);
            }
        } else {
            if (!playerObject_01.isNull(SocketConstants.COLOR)
                    && !playerObject_02.isNull(SocketConstants.COLOR)) {
                gameSettings.setPlayer_01Color(playerObject_02.getString(SocketConstants.COLOR));
                gameSettings.setPlayer_02Color(playerObject_01.getString(SocketConstants.COLOR));
            }

            if (!playerObject_01.isNull(SocketConstants.SYMBOL)
                    && !playerObject_02.isNull(SocketConstants.SYMBOL)) {
                gameSettings.setPlayer_01Symbol(GameSettings.mSymbolArray[playerObject_02
                        .getInt(SocketConstants.SYMBOL)]);
                gameSettings.setPlayer_02Symbol(GameSettings.mSymbolArray[playerObject_01
                        .getInt(SocketConstants.SYMBOL)]);
            }
        }

        String currentPlayerUDID = responseObject.getString(SocketConstants.CURRENT_PLAYER);
        if (currentPlayerUDID.equals(UDID.getUDID())) {
            this.mCurrentPlayer = FieldType.PLAYER_01;
            this.mClicked = false;
        } else {
            this.mCurrentPlayer = FieldType.PLAYER_02;
        }

        updateCurrentPlayer();
        updateGameBoard(responseObject);

        if (!this.mIsGameInitialized) {
            this.initGame();
        }
    }

    private void updateGameBoard(JSONObject responseObject) throws Exception {
        JSONArray gameBoardArray = responseObject.getJSONArray(SocketConstants.GAME_BOARD);
        int length = gameBoardArray.length();

        for (int i = 0; i < length; i++) {
            int value = gameBoardArray.getInt(i);

            if (this.mFieldTypeArray[i] == FieldType.EMPTY && value != 0) {
                this.setPixel(i, value);
            }

            switch (value) {
                case 0:
                    this.mFieldTypeArray[i] = FieldType.EMPTY;
                    break;
                case 1:
                    this.mFieldTypeArray[i] = FieldType.PLAYER_01;
                    break;
                case 2:
                    this.mFieldTypeArray[i] = FieldType.PLAYER_02;
                    break;
            }
            this.updateUI(i);
        }

        this.checkWin();
    }

    private void setPixel(int index, int value) {
        Timber.tag(TAG).e("setPixelIndex %s", index);

        if (index == 3) {
            index = 5;
        } else if (index == 5) {
            index = 3;
        }

        String indexHex = String.format(Locale.getDefault(), "%02d", index);
        String message = CMD.PIXEL + indexHex + (value == 1
                ? this.mGameSettings.getPlayer_01Color()
                : this.mGameSettings.getPlayer_02Color());
        this.writeMessage(hexStringToByteArray(message));
    }

    @Override
    public void onClick(View view) {
        if (this.mCurrentPlayer == FieldType.PLAYER_01 && !this.mClicked) {
            int index = (int) view.getTag();
            this.onFieldClick(index);
            this.mClicked = true;
        }
    }

    private void onFieldClick(int index) {
        FieldType fieldType = mFieldTypeArray[index];
        if (fieldType != FieldType.EMPTY) {
            return;
        }

        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put(SocketConstants.TYPE, SocketConstants.INDEX);
            messageObject.put(SocketConstants.UDID, UDID.getUDID());
            messageObject.put(SocketConstants.INDEX, index);

            setMessage(messageObject);
            Timber.tag(TAG).e("setIndex");
        } catch (Exception e) {
            e.printStackTrace();
        }
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

        String message = CMD.BRIGHTNESS + brightnessHexValue;
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

    private void writeMessage(byte[] message) {
        BleManager bleManager = this.getBleManager();
        bleManager.writeService(bleManager.getGattService(
                "6E400001-B5A3-F393-E0A9-E50E24DCCA9E"),
                "6E400002-B5A3-F393-E0A9-E50E24DCCA9E", message);
    }

    private void win(FieldType fieldType) {
        if (fieldType == FieldType.PLAYER_01) {
            try {
                JSONObject requestObject = new JSONObject();
                requestObject.put(SocketConstants.TYPE, SocketConstants.WIN);
                requestObject.put(SocketConstants.UDID, UDID.getUDID());
                setMessage(requestObject);

                displayMessageDialog("Wygrałeś");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.reset();
    }

    private void displayMessageDialog(String message) {
        this.hideMessageDialog();

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

        this.mMessageDialog = alertDialogBuilder.create();
        this.mMessageDialog.show();
    }

    private void hideMessageDialog() {
        if (this.mMessageDialog != null && this.mMessageDialog.isShowing()) {
            this.mMessageDialog.dismiss();
        }
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

        this.writeMessage(hexStringToByteArray(CMD.RESET));
    }

    private void updateUI(int index) {
        ImageView view = (ImageView) mGameGrid.getChildAt(index);
        FieldType fieldType = this.mFieldTypeArray[index];

        switch (fieldType) {
            case EMPTY:
                break;
            case PLAYER_01:
                view.setImageResource(this.mGameSettings.getPlayer_01Symbol());
                view.setColorFilter(new PorterDuffColorFilter(
                        Color.parseColor("#" + this.mGameSettings.getPlayer_01Color()), SRC_IN));
                break;
            case PLAYER_02:
                view.setImageResource(this.mGameSettings.getPlayer_02Symbol());
                view.setColorFilter(new PorterDuffColorFilter(
                        Color.parseColor("#" + this.mGameSettings.getPlayer_02Color()), SRC_IN));
                break;
        }
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
    public void onDestroyView() {
        super.onDestroyView();
        this.mGameHandler.removeCallbacksAndMessages(null);
        this.hideMessageDialog();
        this.reset();
    }
}
