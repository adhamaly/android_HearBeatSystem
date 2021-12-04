package com.example.adham.firebase1_authentication.Activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.adham.firebase1_authentication.R;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CheckActivity extends AppCompatActivity {

    private static final String TAG = "CheckActivity";
    private Module module;
    TextView predictionText;
    EditText age,education,cigPerDay,tolChol,sysDP,diaBP,BMI,BPM,glucose,gender,currSmoker,BPmeds,prevalentStroke,PrevalentHyp,diabtes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check);

        try {
            module = Module.load(assetFilePath(this, "andoirdmodel.pt"));


        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.e(TAG, getPredictions(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1));
        //Log.e(TAG, String.valueOf("done"));

        age = (EditText)findViewById(R.id.age);
        education =(EditText)findViewById(R.id.education);
        cigPerDay = (EditText)findViewById(R.id.cigerates);
        tolChol = (EditText)findViewById(R.id.alcohol);
        sysDP = (EditText)findViewById(R.id.sysBloodPressure);
        diaBP = (EditText)findViewById(R.id.DiaBP);
        BMI = (EditText)findViewById(R.id.BodyMassIndex);
        BPM = (EditText)findViewById(R.id.BPM);
        glucose = (EditText)findViewById(R.id.Glucose);
        gender = (EditText)findViewById(R.id.Gender);
        currSmoker = (EditText)findViewById(R.id.curentSmoker);
        BPmeds = (EditText)findViewById(R.id.BloodPressureMedication);
        prevalentStroke = (EditText)findViewById(R.id.PrevalentStroke);
        PrevalentHyp = (EditText)findViewById(R.id.prevHyp);
        diabtes = (EditText)findViewById(R.id.Diabetes);

        predictionText = (TextView) findViewById(R.id.checkedCHD);
        Button checkButton = findViewById( R.id.diseaseCheck );
        checkButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String prediction = getPredictions( Float.parseFloat(age.getText().toString()),Float.parseFloat(education.getText().toString()),Float.parseFloat(cigPerDay.getText().toString()),
                        Float.parseFloat(tolChol.getText().toString()),Float.parseFloat(sysDP.getText().toString()),Float.parseFloat(diaBP.getText().toString()),Float.parseFloat(BMI.getText().toString()),
                        Float.parseFloat(BPM.getText().toString()),Float.parseFloat(glucose.getText().toString()),Float.parseFloat(gender.getText().toString()),Float.parseFloat(currSmoker.getText().toString()),
                        Float.parseFloat(BPmeds.getText().toString()),Float.parseFloat(prevalentStroke.getText().toString()),Float.parseFloat(PrevalentHyp.getText().toString()),Float.parseFloat(diabtes.getText().toString()));

                predictionText.setText(prediction);
            }
        } );

    }

    public static String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (OutputStream os = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
                os.flush();
            }
            return file.getAbsolutePath();
        }
    }

    private String getPredictions(float age, float education, float cigsPerDay, float totChol,
                                  float sysBP, float diaBP, float BMI, float heartRate, float glucose,
                                  float male, float currentSmoker, float BPMeds, float prevalentStroke,
                                  float prevalentHyp, float diabetes) {

        float notmale = male == 1 ? 0 : 1;
        float notcurrentSmoker = currentSmoker == 1 ? 0 : 1;
        float notBPMeds = BPMeds == 1 ? 0 : 1;
        float notprevalentStroke = prevalentStroke == 1 ? 0 : 1;
        float notprevalentHyp = prevalentHyp == 1 ? 0 : 1;
        float notdiabetes = diabetes == 1 ? 0 : 1;

        age = (float) ((age - 49.551941	) / 8.562029);
        education = (float) ((education - 1.980317 ) / 1.022656);
        cigsPerDay = (float) ((cigsPerDay -9.025424 ) / 11.921590);
        totChol  = (float) ((totChol - 236.847731) / 44.097681);
        sysBP  = (float) ((sysBP - 132.370558) / 22.086866);
        diaBP  = (float) ((diaBP - 82.917031) / 11.974258);
        BMI   = (float) ((BMI - 25.782802) / 4.065601);
        heartRate = (float) ((heartRate- 75.730727) / 11.981525);
        glucose = (float) ((glucose - 81.852925) / 23.904164);


        float [] inputs = {age,education,cigsPerDay,totChol,sysBP,diaBP,BMI,heartRate,glucose,
                notmale,male,notcurrentSmoker,currentSmoker,notBPMeds,BPMeds,notprevalentStroke,
                prevalentStroke,notprevalentHyp,prevalentHyp,notdiabetes,diabetes};
        final long[] shape = new long[]{1, inputs.length};

        final Tensor inputTensor = Tensor.fromBlob(inputs, shape);
        Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
        float[] scores = outputTensor.getDataAsFloatArray();
        if (scores[0]>scores[1])
            return "no TenYearCHD";
        return "yes TenYearCHD";
    }

}