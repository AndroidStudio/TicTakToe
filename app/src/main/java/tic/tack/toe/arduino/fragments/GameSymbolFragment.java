package tic.tack.toe.arduino.fragments;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import org.json.JSONObject;

import java.util.Objects;

import tic.tack.toe.arduino.CustomGridView;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.dialog.MessageDialog;
import tic.tack.toe.arduino.game.GameSettings;
import tic.tack.toe.arduino.sockets.SocketConstants;
import tic.tack.toe.arduino.sockets.UDID;
import timber.log.Timber;

public class GameSymbolFragment extends BaseFragment implements View.OnClickListener {

    private static final String TAG = "GameSymbolFragment";
    private CustomGridView mGridLayout;

    private int mSymbolIndex = GameSettings.NO_SYMBOL_INDEX;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.game_symbol_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.findViewById(R.id.nextButton).setOnClickListener(this.onNextClickListener);
        this.mGridLayout = view.findViewById(R.id.gridLayout);
        for (int i = 0; i < 9; i++) {
            int symbol = GameSettings.mSymbolArray[i];

            ImageView imageView = (ImageView) mGridLayout.getChildAt(i);
            imageView.setImageResource(symbol);
            imageView.setTag(i);
            imageView.setOnClickListener(this);
        }
    }

    private final View.OnClickListener onNextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSymbolIndex == GameSettings.NO_SYMBOL_INDEX) {
                MessageDialog.displayDialog(getActivity(), getString(R.string.prosze_wybrac_symbol));
                return;
            }
            initPlayerSymbol();
        }
    };

    @Override
    public void onMessage(String message) {
        super.onMessage(message);
        try {
            JSONObject responseObject = new JSONObject(message);
            String type = responseObject.getString(SocketConstants.TYPE);
            switch (type) {
                case SocketConstants.SYMBOL:
                    String symbolStatus = responseObject.getString(SocketConstants.SYMBOL);
                    if (symbolStatus.equals(SocketConstants.OK)) {
                        playerSymbolInitialized();
                    } else {
                        MessageDialog.displayDialog(getActivity(), "Symbol niedostępny")
                                .show();
                    }
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initPlayerSymbol() {
        try {
            JSONObject messageObject = new JSONObject();
            messageObject.put(SocketConstants.TYPE, SocketConstants.SYMBOL);
            messageObject.put(SocketConstants.UDID, UDID.getUDID());
            messageObject.put(SocketConstants.SYMBOL, this.mSymbolIndex);

            setMessage(messageObject);
            Timber.tag(TAG).e("initPlayerSymbol");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void playerSymbolInitialized() {
        try {
            if (this.mSymbolIndex == GameSettings.NO_SYMBOL_INDEX) {
                return;
            }

            GameSettings.getInstance().setPlayer_01Symbol(GameSettings.mSymbolArray[this.mSymbolIndex]);
            FragmentController.setCurrentFragment(Objects.requireNonNull(getActivity()),
                    new GameSettingsLedFragment(), true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < 9; i++) {
            ImageView imageView = (ImageView) this.mGridLayout.getChildAt(i);
            imageView.clearColorFilter();
        }

        ((ImageView) v).setColorFilter(new PorterDuffColorFilter(Color.parseColor(
                "#" + GameSettings.getInstance().getPlayer_01Color()),
                PorterDuff.Mode.SRC_IN));

        this.mSymbolIndex = (int) v.getTag();
    }
}
