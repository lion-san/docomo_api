/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

/**
* アプリケーション情報画面表示クラス
*/
public class AppInfomationActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_info);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        //バージョン情報の取得
        PackageInfo packageInfo = null;
        String appVersion = "1.0.0";
        try {
            packageInfo = getPackageManager().getPackageInfo(
                    this.getPackageName(), PackageManager.GET_ACTIVITIES);
            appVersion = packageInfo.versionName;
        } catch (NameNotFoundException e) {
            // エラー表示
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        //バージョン情報を設定
        TextView textView = (TextView) findViewById(R.id.app_version);
        textView.setText(appVersion);
    }
}
