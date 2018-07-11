package tic.tack.toe.arduino;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayout;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private final static String EMPTY = "";

    private final String[] fieldValueArray = new String[9];

    private String currentCharacter = "X";

    private GridLayout gridLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.gridLayout = findViewById(R.id.gridLayout);
        int childCount = this.gridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.fieldValueArray[i] = EMPTY;
            View view = this.gridLayout.getChildAt(i);
            view.setTag(i);
            view.setOnClickListener(this);
        }
    }

    @Override
    public void onClick(View view) {
        int position = (int) view.getTag();
        String value = this.fieldValueArray[position];
        if (!TextUtils.isEmpty(value)) {
            return;
        }

        this.fieldValueArray[position] = getCharacter();
        this.updateUI();
        this.checkWin();
    }

    private void win(String value) {
        Toast.makeText(this, "Wygrywa: " + value, Toast.LENGTH_LONG).show();
    }

    private void reset() {
        this.currentCharacter = "X";

        int childCount = this.gridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            this.fieldValueArray[i] = EMPTY;
            Button view = (Button) gridLayout.getChildAt(i);
            view.setText(EMPTY);
        }
    }

    private void updateUI() {
        int childCount = this.gridLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            Button view = (Button) gridLayout.getChildAt(i);
            String character = String.valueOf(this.fieldValueArray[i]);
            view.setText(character);
        }
    }

    private String getCharacter() {
        final String character;
        if (this.currentCharacter.equals("X")) {
            character = "O";
        } else {
            character = "X";
        }
        this.currentCharacter = character;
        return character;
    }

    private void checkWin() {
        checkLine1();
        checkLine2();
        checkLine3();
        checkLine4();
        checkLine5();
        checkLine6();
        checkLine7();
        checkLine8();

        for (int i = 0; i < 9; i++) {
            String character = String.valueOf(this.fieldValueArray[i]);
            if (TextUtils.isEmpty(character)) {
                return;
            }
        }

        reset();
    }

    private void checkLine1() {
        String v1 = this.fieldValueArray[0];
        String v2 = this.fieldValueArray[1];
        String v3 = this.fieldValueArray[2];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine2() {
        String v1 = this.fieldValueArray[3];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[5];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine3() {
        String v1 = this.fieldValueArray[6];
        String v2 = this.fieldValueArray[7];
        String v3 = this.fieldValueArray[8];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine4() {
        String v1 = this.fieldValueArray[0];
        String v2 = this.fieldValueArray[3];
        String v3 = this.fieldValueArray[6];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine5() {
        String v1 = this.fieldValueArray[1];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[7];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine6() {
        String v1 = this.fieldValueArray[2];
        String v2 = this.fieldValueArray[5];
        String v3 = this.fieldValueArray[8];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine7() {
        String v1 = this.fieldValueArray[0];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[8];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }

    private void checkLine8() {
        String v1 = this.fieldValueArray[2];
        String v2 = this.fieldValueArray[4];
        String v3 = this.fieldValueArray[6];
        if (v1.equals(v2) && v1.equals(v3) && !TextUtils.isEmpty(v1)) {
            this.win(v1);
            this.reset();
        }
    }
}
