package debmi.kopiascsaba.hu.debmi;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;


public class BMIActivity extends AppCompatActivity {


    // UI references.
    private EditText mWeightView;
    private EditText mHeightView;
    private EditText mNameView;
    private TextView tResultView;
    private final static String TAG = "BMIActivity";
    private SharedPreferences SP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bmi);

        mWeightView = (EditText) findViewById(R.id.weight);
        mNameView = (EditText) findViewById(R.id.name);
        mHeightView = (EditText) findViewById(R.id.height);
        tResultView = (TextView) findViewById(R.id.resultView);
        SP = getSharedPreferences("hu.kopiascsaba.debinagyszeru", Context.MODE_PRIVATE);
        populateFields();


        Button mCalculateButton = (Button) findViewById(R.id.calculate);
        mCalculateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                calculate();
            }
        });

    }

    /**
     * Populates the fields from the stored values, if there is any
     */
    private void populateFields() {
        if (SP.contains("name")) {
            mNameView.setText(SP.getString("name", ""));
        }
        if (SP.contains("height")) {
            mHeightView.setText(SP.getString("height", ""));
        }
        if (SP.contains("name") && SP.contains("height")) {
            mWeightView.requestFocus();
        }
    }

    /**
     * Updates the field values into the SharedPreferences upon submission
     */
    private void updateStoredFieldValues() {
        SharedPreferences.Editor editor = SP.edit();
        editor.putString("name", mNameView.getText().toString());
        editor.putString("height", mHeightView.getText().toString());
        editor.apply();
    }

    /**
     * Submit handler
     */
    private void calculate() {
        if (!validateFields()) {
            return;
        }

        updateStoredFieldValues();

        // Let's close the keyboard, it would hide the gui's important parts with the result
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }


        String name = mNameView.getText().toString();
        double weight = Double.valueOf(mWeightView.getText().toString());
        double height = Double.valueOf(mHeightView.getText().toString()) / 100;

        // Do the most important part:)
        double bmi = weight / (height * height);


        StringBuilder sb = new StringBuilder();

        // Display BMI and BMI category
        int bmiDescription = 0;
        if (bmi <= 18.5) {
            bmiDescription = R.string.bmi_underweight;
        } else if (bmi > 18.5 && bmi <= 25) {
            bmiDescription = R.string.bmi_normal;
        } else if (bmi > 25 && bmi <= 30) {
            bmiDescription = R.string.bmi_overweight;
        } else if (bmi > 30) {
            bmiDescription = R.string.bmi_obese;
        }

        sb.append(String.format(getResources().getString(R.string.bmi_result), name, bmi, getResources().getString(bmiDescription)));
        sb.append("<br><br>");

        // If correction needed, then calculate and display that
        if (bmiDescription != R.string.bmi_normal) {
            double idealWeight = 21.75 * (height * height); // 18.5 - 25 között van
            double weightDiff = weight - idealWeight;
            double wPercent = weightDiff / idealWeight * 100;
            sb.append(
                    String.format(getResources().getString(R.string.bmi_correction),
                            Math.abs(weightDiff),
                            getResources().getString(weightDiff < 0 ? R.string.bmi_correction_up : R.string.bmi_correction_down)
                    )
            );
            sb.append("<br><br>");
            sb.append(String.format(
                    getResources().getString(weightDiff < 0 ? R.string.bmi_correction_notice_up : R.string.bmi_correction_notice_down),
                    idealWeight,
                    Math.abs(wPercent)
                    )
            );
            // Some hidden treasury for just someone:)
            if (name.matches("(?i:(Debóra|Debi|Debóca|Cippóra|Cippora|Deby|Debora))")) {
                sb.append("<br><br>");
                String[] praises = getResources().getStringArray(R.array.praises);
                String praise = praises[(new Random()).nextInt(praises.length)];
                sb.append(String.format(getResources().getString(R.string.praises_wrapper), praise));
            }

        }


        tResultView.setText(Html.fromHtml(sb.toString()));

    }

    /**
     * Validation of the input fields, with some added fun messages
     *
     * @return boolean
     */

    private boolean validateFields() {
        String name = mNameView.getText().toString();
        double weight = 0, height = 0;

        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.form_error_name));
            focusView = mNameView;
            cancel = true;
        } else if (TextUtils.isEmpty(mWeightView.getText())) {
            mWeightView.setError(getString(R.string.form_error_weight));
            focusView = mWeightView;
            cancel = true;
        } else if (TextUtils.isEmpty(mHeightView.getText())) {
            mHeightView.setError(getString(R.string.form_error_height));
            focusView = mHeightView;
            cancel = true;
        } else {
            weight = Double.valueOf(mWeightView.getText().toString());
            height = Double.valueOf(mHeightView.getText().toString());

            if (height < 20 || height > 210) {

                if (height < 20) {
                    mHeightView.setError(getString(R.string.form_error_height_low));
                } else if (height > Integer.MAX_VALUE) { // 21474.83647 km
                    mHeightView.setError(getString(R.string.form_error_height_ultra_giga_high));
                } else if (height > 600000) { // 6km
                    mHeightView.setError(getString(R.string.form_error_height_giga_high));
                } else if (height > 5000) {
                    mHeightView.setError(getString(R.string.form_error_height_ultra_high));
                } else if (height > 210) {
                    mHeightView.setError(getString(R.string.form_error_height_high));
                }

                focusView = mHeightView;
                cancel = true;
            } else if (weight < 15 || weight > 300) {
                mWeightView.setError(getString(weight < 15 ? R.string.form_error_weight_low : R.string.form_error_weight_high));
                focusView = mWeightView;
                cancel = true;
            }
        }
        if (cancel) {
            focusView.requestFocus();
            return false;
        } else {
            return true;
        }
    }

}

