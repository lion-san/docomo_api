/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.dialogue.Dialogue;
import jp.ne.docomo.smt.dev.dialogue.data.DialogueResultData;
import jp.ne.docomo.smt.dev.dialogue.param.DialogueRequestParam;

/**
 * 雑談対話処理クラス
 */
public class DialogueAPI {
    private Handler mHandler;   // UIスレッドに通知するためのHandler

    /**
     * コンストラクタ
     */
    public DialogueAPI(Handler handler) {
        mHandler = handler;
    }

    /**
     * 非同期処理
     */
    private class DialogueAsyncTask
            extends AsyncTask<DialogueRequestParam, Integer, DialogueResultData> {
        private boolean mIsSdkException = false;
        private String mExceptionMessage = null;

        @Override
        protected DialogueResultData doInBackground(DialogueRequestParam... params) {
            DialogueResultData resultData = null;
            DialogueRequestParam reqParam = params[0];

            try {
                //要求処理クラスを作成
                Dialogue dialogue = new Dialogue();

                //要求処理クラスにリクエストデータを渡し、レスポンスデータを取得する
                resultData = dialogue.request(reqParam);
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
        protected void onPostExecute(DialogueResultData resultData) {
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
        msg.arg1 = Uti.API_DIALOGUE;
        msg.arg2 = arg2;
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    /**
     * 雑談開始
     */
    public void start(String msg) {
        // 雑談対話パラメータクラスを生成して、質問を設定する
        DialogueRequestParam param = new DialogueRequestParam();
        // ユーザ発話を設定
        param.setUtt(msg);
        // 実行
        new DialogueAsyncTask().execute(param);
    }
}
