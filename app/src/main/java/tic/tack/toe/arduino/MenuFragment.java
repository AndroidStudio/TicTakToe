package tic.tack.toe.arduino;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Spinner;

import com.github.mata1.simpledroidcolorpicker.interfaces.OnColorChangedListener;
import com.github.mata1.simpledroidcolorpicker.pickers.CircleColorPicker;

import java.util.Objects;

public class MenuFragment extends Fragment {

    private EditText mLedColorEditText;

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

        this.mLedColorEditText = view.findViewById(R.id.ledColorEditText);
        this.mLedColorEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setLedColor(mLedColorEditText.getText().toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        final Spinner spinner = view.findViewById(R.id.ledTypeSpinner);
        final ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                Objects.requireNonNull(getActivity()), R.array.let_types,
                android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!first) {
                    String type = (String) parent.getItemAtPosition(position);
                    MainActivity mainActivity = (MainActivity) getActivity();
                    if (mainActivity != null) {
                        mainActivity.setLedType(type);
                    }
                }
                first = false;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner.setAdapter(adapter);

        CircleColorPicker colorPicker = view.findViewById(R.id.colorPicker);
        colorPicker.setOnColorChangedListener(new OnColorChangedListener() {
            @Override
            public void colorChanged(int color) {
                String hex = String.format("%02x%02x%02x",
                        Color.red(color),
                        Color.green(color),
                        Color.blue(color));
                setLedColor(hex);
            }
        });


        AppCompatSeekBar seekBar = view.findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                setBrightness(seekBar.getProgress());
            }
        });
    }

    private void setBrightness(int progress) {
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setBrightness(progress);
        }
    }

    private void setLedColor(String color) {
        try {
            Color.parseColor(String.valueOf("#".concat(color)));
        } catch (Exception e) {
            this.mLedColorEditText.setError("Nieprawidłowy kolor");
            e.printStackTrace();
            return;
        }

        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.setLedColor(color);
        }
    }
}
