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

import java.util.Objects;

import tic.tack.toe.arduino.CustomGridView;
import tic.tack.toe.arduino.R;
import tic.tack.toe.arduino.dialog.MessageDialog;
import tic.tack.toe.arduino.game.GameSettings;

public class GameSymbolFragment extends BaseFragment implements View.OnClickListener {
    private final static int NO_SYMBOL = -1;

    private final int[] mSymbolArray = new int[]{
            R.drawable.ic_game_symbol_01,
            R.drawable.ic_game_symbol_02,
            R.drawable.ic_game_symbol_03,
            R.drawable.ic_game_symbol_04,
            R.drawable.ic_game_symbol_05,
            R.drawable.ic_game_symbol_06,
            R.drawable.ic_game_symbol_07,
            R.drawable.ic_game_symbol_08,
            R.drawable.ic_game_symbol_09,
    };

    private int mSelectedSymbol = NO_SYMBOL;
    private CustomGridView mGridLayout;

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
            int symbol = this.mSymbolArray[i];

            ImageView imageView = (ImageView) mGridLayout.getChildAt(i);
            imageView.setImageResource(symbol);
            imageView.setTag(symbol);
            imageView.setOnClickListener(this);
        }
    }

    private final View.OnClickListener onNextClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mSelectedSymbol == NO_SYMBOL) {
                MessageDialog.displayDialog(getActivity(), getString(R.string.prosze_wybrac_symbol));
                return;
            }

            GameSettings.getInstance().setPlayer_01Symbol(mSelectedSymbol);
            FragmentController.setCurrentFragment(Objects.requireNonNull(getActivity()),
                    new GameSettingsLedFragment());
        }
    };

    @Override
    public void onClick(View v) {
        for (int i = 0; i < 9; i++) {
            ImageView imageView = (ImageView) this.mGridLayout.getChildAt(i);
            imageView.clearColorFilter();
        }

        ((ImageView) v).setColorFilter(new PorterDuffColorFilter(Color.parseColor(
                "#" + GameSettings.getInstance().getPlayer_01Color()),
                PorterDuff.Mode.SRC_IN));
        this.mSelectedSymbol = (int) v.getTag();
    }
}
