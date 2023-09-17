package com.example.translatorapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLanguage;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import android.annotation.SuppressLint;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    Spinner fromSpinner, toSpinner;
    public EditText srcText;
//    public TextView tdBtn;
    public TextView dnyText;
    public ImageView micIV;

    String[] fromLang = {"From", "English", "Hindi"};
    String[] toLang = {"To", "English", "Hindi"};

            public static final int REQUEST_PERMISSION_CODE = 1;
            int langCode,fromLangCode,toLangCode = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromSpinner = findViewById(R.id.fromspinner);
        toSpinner = findViewById(R.id.tospinner);
        srcText = findViewById(R.id.sourcetext);
        dnyText = findViewById(R.id.destinytext);
        micIV = findViewById(R.id.mic);
//        tdBtn = findViewById(R.id.translate);

        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromLangCode = getLangCode(fromLang[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromLang);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toLangCode = getLangCode(toLang[position]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,toLang);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        findViewById(R.id.translate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dnyText.setText("");
                if(srcText.getText().toString().isEmpty()){
                    Toast.makeText(MainActivity.this,"Pls Enter u & translete", Toast.LENGTH_SHORT).show();
                } else if(fromLangCode==0) {
                    Toast.makeText(MainActivity.this,"Pls select src Text", Toast.LENGTH_SHORT).show();
                } else if (toLangCode==0) {
                    Toast.makeText(MainActivity.this, "Pls select lang to feel me", Toast.LENGTH_SHORT).show();
                } else{
                    translateText(fromLangCode,toLangCode,srcText.getText().toString());
                }
            }
        });

        //by chance use mic feature & anything write this place

        findViewById(R.id.clearall).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                MainActivity.this.srcText.setText("");
            }
        });

        findViewById(R.id.copyq).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (MainActivity.this.dnyText.length() > 0) {
                    ((ClipboardManager) MainActivity.this.getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("text", MainActivity.this.dnyText.getText()));
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Text Copied", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(MainActivity.this.getApplicationContext(), "No Text!", Toast.LENGTH_SHORT).show();
            }
        });

        micIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Convert in Text");
                try {
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);
                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_PERMISSION_CODE){
            if(resultCode==RESULT_OK && data!=null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                srcText.setText(result.get(0));
            }
        }
    }

    public void translateText(int fromLangCode, int toLangCode, String source){
             dnyText.setText("downloading Model...");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLangCode)
                .setTargetLanguage(toLangCode)
                .build();

        FirebaseTranslator translator = FirebaseNaturalLanguage.getInstance().getTranslator(options);

        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                dnyText.setText("Translatingg...");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        dnyText.setText(s);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to Translate : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "fail to download model : "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        }

    public int getLangCode(String lang) {
        int langCode = 0;
         switch (lang){
             case "English":
                 langCode = FirebaseTranslateLanguage.EN;
                 break;
             case "Hindi":
                 langCode = FirebaseTranslateLanguage.HI;
                 break;
             default:
                 langCode = 0;

         }
         return langCode;
    }

}