/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;

import jp.ne.docomo.smt.dev.aitalk.AiTalkTextToSpeech;
import jp.ne.docomo.smt.dev.aitalk.data.AiTalkSsml;
import jp.ne.docomo.smt.dev.common.exception.SdkException;
import jp.ne.docomo.smt.dev.common.exception.ServerException;

/**
* 音声合成AI処理クラス
*/
public class TextToSpeechAI {
    private Handler mHandler;               // UIスレッドに通知するためのHandler
    private String mVoiceName = "koutarou";    // 話者の初期値

    /**
     * コンストラクタ
     */
    public TextToSpeechAI(Handler handler) {
        mHandler = handler;
    }

    /**
     * 非同期処理
     */
    private class TextToSpeechAIAsyncTask extends AsyncTask<Object, Integer, byte[]> {
        // エラーフラグ
        private boolean mIsSdkException = false;
        private String mExceptionMessage = null;

        @Override
        protected void onPreExecute() {
            sendMessage(null, Uti.API_NOTIFY_EVENT, 1);
        }

        @Override
        protected byte[] doInBackground(Object... params) {
            byte[] resultData = null;
            try {
                // 要求処理クラスを作成
                AiTalkTextToSpeech search = new AiTalkTextToSpeech();
                // 要求処理クラスにリクエストデータを渡し、レスポンスデータを取得する
                resultData = search.requestAiTalkSsmlToSound(((AiTalkSsml) params[0]).makeSsml());

                // 音声出力用バッファ作成
                int bufSize = AudioTrack.getMinBufferSize(
                        16000,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT);
                // ビッグエディアンをリトルエディアンに変換
                search.convertByteOrder16(resultData);
                // 音声出力
                AudioTrack at = new AudioTrack(
                        AudioManager.STREAM_MUSIC,
                        16000,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufSize,
                        AudioTrack.MODE_STREAM);
                at.play();
                at.write(resultData, 0, resultData.length);
                at.stop();
                at.release();
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
        protected void onPostExecute(byte[] resultData) {
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
                sendMessage(null, Uti.API_NOTIFY_EVENT, 0);
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
    public void setVoiceName(String voiceName) {
        mVoiceName = voiceName;
    }

    /**
     * 音声合成開始
     */
    public void start(String msg) {
        AiTalkSsml ssml = new AiTalkSsml();
        ssml.startVoice(mVoiceName);
        ssml.addText(msg);
        ssml.endVoice();

        // 実行
        new TextToSpeechAIAsyncTask().execute(ssml);
    }
}

