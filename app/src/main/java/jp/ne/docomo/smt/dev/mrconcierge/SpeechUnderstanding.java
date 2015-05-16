/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.sentenceunderstanding.SentenceTask;
import jp.ne.docomo.smt.dev.sentenceunderstanding.constants.Lang;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceResultData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.param.SentenceAppInfoParam;
import jp.ne.docomo.smt.dev.sentenceunderstanding.param.SentenceTaskRequestParam;
import jp.ne.docomo.smt.dev.sentenceunderstanding.param.SentenceUserUtteranceParam;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
* 発話理解処理クラス
*/
public class SpeechUnderstanding {
    private Handler mHandler;   // UIスレッドに通知するためのHandler

    /**
     * コンストラクタ
     */
    public SpeechUnderstanding(Handler handler) {
        mHandler = handler;
    }

    /**
     * 非同期処理
     */
    private class SentenceUnderstandingAsyncTask
            extends AsyncTask<SentenceTaskRequestParam, Integer, SentenceResultData> {
        private boolean mIsSdkException = false;
        private String mExceptionMessage = null;

        @Override
        protected SentenceResultData doInBackground(SentenceTaskRequestParam... params) {
            SentenceResultData resultData = null;
            SentenceTaskRequestParam reqParam = params[0];
            try {
                // 要求処理クラスを作成
                final SentenceTask su = new SentenceTask();

                // 要求処理クラスにリクエストデータを渡し、レスポンスデータを取得する
                resultData = su.request(reqParam);

            } catch (SdkException ex) {
                mIsSdkException = true;
                mExceptionMessage = "ErrorCode: " + ex.getErrorCode() + "\nMessage: "
                        + ex.getMessage();
            } catch (ServerException ex) {
                mExceptionMessage = "ErrorCode: " + ex.getErrorCode() + "\nMessage: "
                        + ex.getMessage();
            }

            return resultData;
        }

        @Override
        protected void onCancelled() {
        }

        @Override
        protected void onPostExecute(SentenceResultData resultData) {
            String title = "";
            if (resultData == null) {
                if (mIsSdkException) {
                    title = "SdkException 発生";
                } else {
                    title = "ServerException 発生";
                }
                // エラー表示
                sendMessage(title + "\n" + mExceptionMessage, Uti.API_NOTIFY_ERROR, 0);
            } else {
                // 結果通知
                sendMessage(resultData, Uti.API_NOTIFY_RESULT, 0);
            }
        }
    }

    /**
     * メッセージ送信
     */
    private void sendMessage(Object obj, int arg2, int what) {
        Message msg = new Message();
        msg.obj = obj;
        msg.arg1 = Uti.API_SPEECHUNDERSTANDING;
        msg.arg2 = arg2;
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    /**
     * 発話理解開始
     */
    public void start(String msg) {
        // 意図解釈パラメータクラスを生成して、発話を設定する
        SentenceTaskRequestParam param = new SentenceTaskRequestParam();

        // 言語設定
        param.setLanguage(Lang.JP);

        // アプリケーション情報
        SentenceAppInfoParam appInfo = new SentenceAppInfoParam();
        appInfo.setAppKey("hoge_app01");
        param.setAppInfo(appInfo);

        // クライアントバージョン
        param.setClientVer("0.1");

        // 発話理解を行う文章を設定
        SentenceUserUtteranceParam userUtterance = new SentenceUserUtteranceParam();
        userUtterance.setUtteranceText(msg);
        param.setUserUtterance(userUtterance);

        // アプリ固有情報
        SampleSentenceProjectSpecific project = new SampleSentenceProjectSpecific();
        project.setSampleId("SAMPLE ID");
        param.setProjectSpecific(project);

        // 実行
        new SentenceUnderstandingAsyncTask().execute(param);
    }
}
