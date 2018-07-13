package tic.tack.toe.arduino;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.Objects;

public class MenuFragment extends Fragment {
    private EditText ledColorEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.menu_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.finishButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Objects.requireNonNull(getActivity()).finish();
            }
        });

        this.ledColorEditText = view.findViewById(R.id.ledColorEditText);
        this.ledColorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setLedColor(ledColorEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void setLedColor(String color) {
        try {
            Color.parseColor(String.valueOf("#".concat(color)));
        } catch (Exception e) {
            this.ledColorEditText.setError("Nieprawidłowy kolor");
            e.printStackTrace();
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setLedColor(color);
        }
    }
}
