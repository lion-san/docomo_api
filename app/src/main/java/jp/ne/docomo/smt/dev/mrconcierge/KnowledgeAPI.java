/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;
import jp.ne.docomo.smt.dev.knowledge.KnowledgeSearch;
import jp.ne.docomo.smt.dev.knowledge.data.KnowledgeResultData;
import jp.ne.docomo.smt.dev.knowledge.param.KnowledgeRequestParam;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
 * 知識Q&A処理クラス
 */
public class KnowledgeAPI {
    private Handler mHandler;   // UIスレッドに通知するためのHandler

    /**
     * コンストラクタ
     */
    public KnowledgeAPI(Handler handler) {
        mHandler = handler;
    }

    /**
     * 非同期処理
     */
    private class KnowledgeAsyncTask extends AsyncTask<String, Integer, KnowledgeResultData> {
        private boolean mIsSdkException = false;
        private String mExceptionMessage = null;

        @Override
        protected KnowledgeResultData doInBackground(String... params) {
            KnowledgeResultData resultData = null;
            String reqParam = params[0];
            try {
                //知識QA 要求処理クラスを作成
                KnowledgeSearch search = new KnowledgeSearch();

                //知識QA 要求リクエストデータクラスを作成してパラメータをsetする
                KnowledgeRequestParam requestParam = new KnowledgeRequestParam();
                requestParam.setQuestion(reqParam);

                //知識QA 要求処理クラスにリクエストデータを渡し、レスポンスデータを取得する
                resultData = search.request(requestParam);

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
        protected void onPostExecute(KnowledgeResultData resultData) {
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
        msg.arg1 = Uti.API_KNOWLEDGE;
        msg.arg2 = arg2;
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    /**
     * 知識Q&A開始
     */
    public void start(String msg) {
        // 実行
        new KnowledgeAsyncTask().execute(msg);
    }
}
