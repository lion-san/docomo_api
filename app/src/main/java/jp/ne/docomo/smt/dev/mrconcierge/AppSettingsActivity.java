/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import java.util.Arrays;

/**
 * 設定画面処理クラス
 */
public class AppSettingsActivity extends Activity implements OnClickListener {
    private static final String[] VOICE_NAME = {
            "のぞみ", "すみれ", "かほ", "まき", "あかり", "ななこ",
            "せいじ", "おさむ", "ひろし", "あんず", "こうたろう"};
    private static final String[] VOICE_NAME_A = {
            "nozomi", "sumire", "kaho", "maki", "akari", "nanako",
            "seiji", "osamu", "hiroshi", "anzu", "koutarou"};
    private static final String[] SPEAKER_ID = {"女声", "男声"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // 親からの情報の受け取り
        Intent i = getIntent();

        // ラジオボタンのチェック
        RadioGroup group2 = (RadioGroup) findViewById(R.id.radioGroup2);
        switch (i.getIntExtra("voiceEngine", 0)) {
        case 0: // 音声合成AI
            group2.check(R.id.radioButton2_1);
            break;
        case 1: // 音声合成IT
            group2.check(R.id.radioButton2_2);
            break;
        default:
            break;
        }

        // ボタンの設定
        Button btn = (Button) findViewById(R.id.button1);
        btn.setOnClickListener(this);

        // AIの話者
        Spinner s1 = (Spinner) findViewById(R.id.spinner2_1_1);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, VOICE_NAME);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setAdapter(adapter1);
        s1.setSelection(Arrays.asList(VOICE_NAME_A).indexOf(i.getStringExtra("voiceName")));
        // ITの話者
        Spinner s2 = (Spinner) findViewById(R.id.spinner2_2_1);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, SPEAKER_ID);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s2.setAdapter(adapter2);
        s2.setSelection(i.getIntExtra("speekerID", 0));

        // Backキー対応
        i = new Intent();
        setResult(RESULT_CANCELED, i);
    }

    @Override
    public void onClick(View v) {
        Intent i = new Intent();
        // 戻り値の設定
        RadioButton radio2 = (RadioButton) findViewById(R.id.radioButton2_1);
        if (radio2.isChecked()) {
            i.putExtra("voiceEngine", 0);   // 音声合成AI
        } else {
            i.putExtra("voiceEngine", 1);   // 音声合成IT
        }

        // 話者
        Spinner s1 = (Spinner) findViewById(R.id.spinner2_1_1); // AI
        i.putExtra("voiceName", VOICE_NAME_A[s1.getSelectedItemPosition()]);
        Spinner s2 = (Spinner) findViewById(R.id.spinner2_2_1); // IT
        i.putExtra("speekerID", s2.getSelectedItemPosition());

        // 終了
        setResult(RESULT_OK, i);
        finish();
    }
}
