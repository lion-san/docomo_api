/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

/**
* 音声合成IT処理クラス
*/
public class TextToSpeechIT {
    private static final String REQUEST_URL =
            "https://api.apigw.smt.docomo.ne.jp/virtualNarrator/v1/textToSpeech";
    private static final String CONTENT_TYPE = "application/json";        // JSON形式

    private Handler mHandler;           // UIスレッドに通知するためのHandler
    private String mUrl = "";           // リクエストURL
    private String mSpeakerID = "0";    // 話者の初期値

    /**
     * コンストラクタ
     */
    public TextToSpeechIT(String apikey, Handler handler) {
        mUrl = REQUEST_URL + "?APIKEY=" + apikey;
        mHandler = handler;
    }

    /**
     * 非同期処理
     */
    private class TextToSpeechITAsyncTask extends AsyncTask<String, Void, Boolean> {
        // エラーフラグ
        private String mExceptionMessage = null;

        @Override
        protected void onPreExecute() {
            sendMessage(null, Uti.API_NOTIFY_EVENT, 1);
        }

        @Override
        protected Boolean doInBackground(String... params) {
            String postData = params[0];
            HttpsURLConnection connection = null;

            try {
                // 接続準備
                URL url = new URL(mUrl);
                connection = (HttpsURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", CONTENT_TYPE);
                connection.setInstanceFollowRedirects(false);
                OutputStream os = connection.getOutputStream();
                PrintStream ps = new PrintStream(os, true, "UTF-8");
                ps.print(postData);
                ps.close();

                // 接続
                connection.connect();

                // レスポンスコードの取得
                int res = connection.getResponseCode();
                if (res == 200) {
                    // 音声データの取得
                    InputStream in = connection.getInputStream();
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();

                    byte [] buffer = new byte[1024];
                    // wavファイルのヘッダ部分（46byte）を削除して書き込む
                    in.read(buffer);
                    bout.write(buffer, 46, buffer.length - 46);

                    while (true) {
                        int len = in.read(buffer);
                        if (len < 0) {
                            break;
                        }
                        bout.write(buffer, 0, len);
                    }
                    in.close();

                    // 音声データの再生
                    int bufSize = AudioTrack.getMinBufferSize(
                            22050,                          //16bit LinearPCM Wave、22,050Hz
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT);
                    AudioTrack at = new AudioTrack(
                            AudioManager.STREAM_MUSIC,
                            22050,                          //16bit LinearPCM Wave、22,050Hz
                            AudioFormat.CHANNEL_OUT_MONO,
                            AudioFormat.ENCODING_PCM_16BIT,
                            bufSize,
                            AudioTrack.MODE_STREAM);
                    at.play();
                    at.write(bout.toByteArray(), 0, bout.toByteArray().length);
                    at.stop();
                    at.release();
                    bout.close();
                } else {    // 200 OK以外
                    // エラー処理
                    mExceptionMessage = String.valueOf(res);
                }
            } catch (IOException e) {
                mExceptionMessage = "ネットワークエラー\nMessage: " + e.getMessage();
            } finally {
                // 切断
                connection.disconnect();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (mExceptionMessage == null) {
                // 結果通知
                sendMessage(null, Uti.API_NOTIFY_EVENT, 0);
            } else {
                // エラー表示
                sendMessage(mExceptionMessage, Uti.API_NOTIFY_ERROR, 0);
            }
        }
    }

    /**
     * メッセージ送信
     */
    private void sendMessage(Object obj, int arg2, int what) {
        Message msg = new Message();
        msg.obj = obj;
        msg.arg1 = Uti.API_TEXTTOSPEECH;
        msg.arg2 = arg2;
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    /**
     * 話者の設定
     */
    public void setSpeakerID(int speakerID) {
        mSpeakerID = String.valueOf(speakerID);
    }

    /**
     * 音声合成開始
     */
    public void start(String msg) {
        // 漢字かな混じり文のリクエストボディを作成
        String requestBody = "{\n";
        requestBody += "  \"Command\":\"AP_Synth\",\n";
        requestBody += "  \"SpeakerID\":\"" + mSpeakerID + "\",\n"; // 話者
        requestBody += "  \"AudioFileFormat\":\"2\",\n";   // 16bit LinearPCM Wave、22,050Hz
        requestBody += "  \"TextData\":\"" + msg + "\"\n";
        requestBody += "}\n";

        // 実行
        new TextToSpeechITAsyncTask().execute(requestBody);
    }
}
