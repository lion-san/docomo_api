/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import jp.ne.docomo.smt.dev.common.http.AuthApiKey;
import jp.ne.docomo.smt.dev.dialogue.data.DialogueResultData;
import jp.ne.docomo.smt.dev.knowledge.data.KnowledgeAnswerData;
import jp.ne.docomo.smt.dev.knowledge.data.KnowledgeMessageData;
import jp.ne.docomo.smt.dev.knowledge.data.KnowledgeResultData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceCommandData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceDialogStatusData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceResultData;
import jp.ne.docomo.smt.dev.sentenceunderstanding.data.SentenceUserUtteranceData;

/**
* メイン画面表示クラス
*/
public class MainActivity extends Activity {
    // APIキー(開発者ポータルから取得したAPIキーをここに記述する)
    private static final String APIKEY = docomo Developer supportにて発行したAPIKEY;

    private static final int SETTING_REQUEST = 100; // 設定画面のリクエストコード

    // Viewオブジェクト
    private static ListItemManager sListItemManager;   // 会話表示View管理クラスオブジェクト
    private static ScrollView sScrollView;             // 会話表示のスクロールビューオブジェクト
    private static ImageButton sSpeechRecgBtn;         // 音声認識開始/停止ボタンオブジェクト
    private LinearLayout mBaseLayout;           // 会話表示のベースレイアウトオブジェクト

    // ドコモAPIオブジェクト
    private SpeechRecognitionFU mSpeechRecognitionFU;   // 音声認識FUETREK

    private static boolean sSpeechRecognitionFlag = false;  // 音声認識起動フラグ(true:起動中 ,false:待機中)
    private static Context sContext = null;                 // Context
    private static int sVoiceEngine = 0;                    // 音声合成エンジン(0:AI, 1:IT)
    private static String sVoiceName = "koutarou";             // AIの話者
    private static int sSpeekerID = 0;                      // ITの話者
    private static boolean sTextToSpeechFlag = false;       // 音声合成起動フラグ(true:起動中 ,false:待機中)
    private static TextView sCommandTv;                     // コマンド情報
    private int mSbmMode = 0;                       // SBMモード

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        // ドコモ提供のSDKを利用する場合、認証情報初期化を行う
        AuthApiKey.initializeAuth(APIKEY);

        // Viewの設定
        initView();

        // 会話表示View管理クラス
        sListItemManager = new ListItemManager(this, mBaseLayout);

        // Contextの取得
        sContext = this.getApplicationContext();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mBaseLayout.getChildCount() == 0) {
            // 初回表示のみ
            // 初回表示が遅れる対応
            new DelayShowStartMessage().execute(100); // 0.1秒ウエイト
            return;
        }

        // 画面下までスクロール
        scrollToBottom();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // 音声認識の停止
        stopSpeechRecognition();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 音声認識の停止
        stopSpeechRecognition();

        // 音声認識サービスの終了
        sListItemManager = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_conversation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;
        switch(item.getItemId()) {
        case R.id.menu_appinfo:
            // アプリ情報画面へ遷移
            i = new Intent(this, AppInfomationActivity.class);
            startActivity(i);
            return true;
        case R.id.menu_appsettings:
            // 設定画面へ遷移
            i = new Intent(this, AppSettingsActivity.class);
            // 子への引継ぎ情報
            i.putExtra("voiceEngine", sVoiceEngine);
            i.putExtra("voiceName", sVoiceName);
            i.putExtra("speekerID", sSpeekerID);
            i.putExtra("sbmMode", mSbmMode);
            startActivityForResult(i, SETTING_REQUEST);
            return true;
        default:
            break;
        }
        return false;
    }

    /**
     * 設定画面の後処理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
        case SETTING_REQUEST:   // 設定画面
            if (resultCode == RESULT_OK) {
                // 設定画面からの受け取ったデータを設定する
                sVoiceEngine = data.getIntExtra("voiceEngine", 0);
                sVoiceName = data.getStringExtra("voiceName");
                sSpeekerID = data.getIntExtra("speekerID", 0);
                mSbmMode = data.getIntExtra("sbmMode", 0);
            }
            break;
        default:
            break;
        }
    }

    /**
     * 初回表示が遅れる対応。非同期でウエイト後にUI表示処理追加
     */
    private class DelayShowStartMessage extends AsyncTask<Integer, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                Thread.sleep(params[0].intValue());
            } catch (InterruptedException e) {
                // エラー表示
                Toast.makeText(sContext, e.getMessage(), Toast.LENGTH_LONG).show();
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (!result.booleanValue()) {
                return;
            }

            //初回テキスト表示（既に表示されている場合は表示されない）
            if (sListItemManager != null) {
                // テキスト出力
                sListItemManager.setFirstMessage();
                // 音声合成出力
                startTextToSpeech(getString(R.string.txt_first_message));
            }
        }
    };

    /**
     * 音声認識開始/終了ボタンのクリックリスナー
     */
    private View.OnClickListener mClickSpeechRecg = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (sSpeechRecognitionFlag) {
                // 音声認識起動中なら停止
                stopSpeechRecognition();
            } else {
                // 音声合成起動中なら無効
                if (!sTextToSpeechFlag) {
                    //音声認識スタート
                    startSpeechRecognition();
                }
            }
        }
    };

    /**
     * 「はじめから」ボタンのクリックリスナー
     */
    private View.OnClickListener mClickRestart = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // 音声合成起動中なら無効
            if (!sTextToSpeechFlag) {
                if (sSpeechRecognitionFlag) {
                    // 音声認識起動中なら停止
                    stopSpeechRecognition();
                }
                // テキストクリア
                TextView textView = (TextView) findViewById(R.id.textView1);
                textView.setText("");
                // 表示テキストを全て削除
                sListItemManager.clearAll();
                // テキスト出力
                sListItemManager.setFirstMessage();
                // 音声合成出力
                startTextToSpeech(getString(R.string.txt_first_message));
            }
        }
    };

    /**
     * Viewの初期化
     */
    private void initView() {
        // 会話表示エリア
        mBaseLayout = (LinearLayout) findViewById(R.id.conersation_base);
        sScrollView = (ScrollView) findViewById(R.id.scrollView);
        sCommandTv = (TextView) findViewById(R.id.textView1);

        // マイクボタン
        sSpeechRecgBtn = (ImageButton) findViewById(R.id.btn_recg);
        sSpeechRecgBtn.setOnClickListener(mClickSpeechRecg);

        // はじめからボタン
        ((ImageButton) findViewById(R.id.btn_restart)).setOnClickListener(mClickRestart);
    }

    /**
     * スクロールビューを一番下まで自動スクロールさせる。
     */
    private static void scrollToBottom() {
           AsyncTask<Void, Void, Boolean> waitScroll = new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    Thread.sleep(100); // 0.1秒ディレイ
                } catch (InterruptedException e) {
                    // エラー表示
                    Toast.makeText(sContext, e.getMessage(), Toast.LENGTH_LONG).show();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                sScrollView.fullScroll(View.FOCUS_DOWN);
            };
        };
        waitScroll.execute();
    }

    /**
     * 音声認識の開始
     */
    private void startSpeechRecognition() {
        sSpeechRecognitionFlag = true;
        if (mSpeechRecognitionFU == null) {
            mSpeechRecognitionFU = new SpeechRecognitionFU(this, APIKEY, sHandler);
        }
        mSpeechRecognitionFU.start();
    }

    /**
     * 音声認識の停止
     */
    private void stopSpeechRecognition() {
        if (mSpeechRecognitionFU != null) {
            mSpeechRecognitionFU.cancel();
            mSpeechRecognitionFU = null;
        }
        sSpeechRecognitionFlag = false;
    }

    /**
     * 音声合成の開始
     */
    private static void startTextToSpeech(String msg) {
        switch(sVoiceEngine) {
        case 0:    // AI
            TextToSpeechAI textToSpeechAI = new TextToSpeechAI(sHandler);
            textToSpeechAI.setVoiceName(sVoiceName);
            textToSpeechAI.start(msg);
            break;
        case 1:    // IT
            TextToSpeechIT textToSpeechIT = new TextToSpeechIT(APIKEY, sHandler);
            textToSpeechIT.setSpeakerID(sSpeekerID);
            textToSpeechIT.start(msg);
            break;
        default:
            break;
        }
    }

    /**
     * 雑談対話の開始
     */
    private static void startDialogue(String msg) {
        sListItemManager.setInterpretationProgress();
        //画面下までスクロール
        scrollToBottom();

        new DialogueAPI(sHandler).start(msg);
    }

    /**
     * 知識Q&A対話の開始
     */
    private static void startKnowledgeAPI(String msg) {
        sListItemManager.setInterpretationProgress();
        //画面下までスクロール
        scrollToBottom();

        new KnowledgeAPI(sHandler).start(msg);
    }

    /**
     * 発話理解の開始
     */
    private static void startSpeechUnderstanding(String msg) {
        new SpeechUnderstanding(sHandler).start(msg);
    }

    /**
     * UIスレッドでの処理
     */
    private static Handler sHandler = new Handler() {

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);

            // メッセージの振り分け
            switch (msg.arg1) {
            case Uti.API_DIALOGUE:              // 雑談対話
                dialogueProc(msg);
                break;
            case Uti.API_KNOWLEDGE:             // 知識Q&A
                knowledgeProc(msg);
                break;
            case Uti.API_SPEECHRECOGNITION:     // 音声認識
                speechRecognitionProc(msg);
                break;
            case Uti.API_SPEECHUNDERSTANDING:   // 発話理解
                speechUnderstandingsProc(msg);
                break;
            case Uti.API_TEXTTOSPEECH:          // 音声合成
                textToSpeechProc(msg);
                break;
            default:
                break;
            }
        }

        /**
         * 音声認識の後処理
         */
        private void speechRecognitionProc(Message msg) {
            switch (msg.arg2) {
            case Uti.API_NOTIFY_RESULT:
                String result = (String) msg.obj;
                if (result != null) {
                    // 音声認識結果の表示
                    sListItemManager.setSpeechResult(result);
                    // 発話理解
                    startSpeechUnderstanding(result);
                } else {
                    // 認識失敗
                    sListItemManager.setSpeechTimeOutError();
                    //画面下までスクロール
                    scrollToBottom();
                    // 音声合成出力
                    startTextToSpeech(sContext.getString(R.string.txt_error_speech_timout));
                }
                break;
            case Uti.API_NOTIFY_EVENT:
                speechRecognitionEvent(msg.what);
                break;
            case Uti.API_NOTIFY_ERROR:
                sListItemManager.removeSpeechProgress();
                // エラー表示
                Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
            }
        }

        /**
         * 音声認識のイベント処理
         */
        private void speechRecognitionEvent(int what) {
            switch (what) {
            case Uti.STATE_NON://スタンバイ状態
                //黒マイク画像
                sSpeechRecgBtn.setImageResource(R.drawable.btn_mic);
                break;
            case Uti.STATE_READY_FOR_SPEECH://音声認識開始(発話待ち)
                //赤マイク画像
                sSpeechRecgBtn.setImageResource(R.drawable.btn_rec);
                sSpeechRecgBtn.setEnabled(true);
                break;
            case Uti.STATE_BEGINNING_OF_SPEECH://発話開始
                // 削除すべきアイテムを削除する。（はい/いいえボタン、地名一覧、電話帳）
                sListItemManager.removeViews();
                //音声認識中テキスト表示
                sListItemManager.setSpeechProgress();
                //画面下までスクロール
                scrollToBottom();
                break;
            case Uti.STATE_END_OF_SPEECH://発話終了
                //黒マイク画像
                sSpeechRecgBtn.setImageResource(R.drawable.btn_mic);
                sSpeechRecognitionFlag = false;
                break;
            case Uti.NOTIFY_RESULT://音声認識結果
                break;
            case Uti.NOTIFY_CANCEL:
                //黒マイク画像
                sSpeechRecgBtn.setImageResource(R.drawable.btn_mic);
                sSpeechRecognitionFlag = false;
                sListItemManager.removeSpeechProgress();
                break;
            default:
                break;
            }
        }

        /**
         * 音声合成の後処理
         */
        private void textToSpeechProc(Message msg) {
            switch (msg.arg2) {
            case Uti.API_NOTIFY_RESULT:
                break;
            case Uti.API_NOTIFY_EVENT:
                switch(msg.what) {
                    case 0: // 停止
                        sTextToSpeechFlag = false;
                        break;
                    case 1: // 開始
                        sTextToSpeechFlag = true;
                        break;
                    default:
                        break;
                    }
                break;
            case Uti.API_NOTIFY_ERROR:
                sTextToSpeechFlag = false;
                // エラー表示
                Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
            }
        }

        /**
         * 雑談対話の後処理
         */
        private void dialogueProc(Message msg) {
            switch (msg.arg2) {
            case Uti.API_NOTIFY_RESULT:
                DialogueResultData resultData = (DialogueResultData) msg.obj;
                String utt = resultData.getUtt();
                String yomi = resultData.getYomi();
                if (utt != null && yomi != null) {
                    // 雑談対話結果の表示
                    sListItemManager.setInterpretationResultParam(utt, false);
                    // 画面下までスクロール
                    scrollToBottom();

                    // 音声合成出力
                    startTextToSpeech(yomi);
                }
                break;
            case Uti.API_NOTIFY_EVENT:
                break;
            case Uti.API_NOTIFY_ERROR:
                // エラー表示
                Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
            }
        }

        /**
         * 知識Q&Aの後処理
         */
        private void knowledgeProc(Message msg) {
            String textForSpeech = null;
            String textForDisplay = null;
            switch (msg.arg2) {
                case Uti.API_NOTIFY_RESULT:
                    KnowledgeResultData resultData = (KnowledgeResultData) msg.obj;
                    if (resultData != null) {
                        String code = resultData.getCode();
                        KnowledgeMessageData messageData = resultData.getMessage();
                        if (messageData != null) {
                            textForSpeech = messageData.getTextForSpeech();
                            textForDisplay = messageData.getTextForDisplay();
                        }
                        if (code.startsWith("S")) {
                            ArrayList<KnowledgeAnswerData> answerDatas = resultData.getAnswers();
                            StringBuffer strBuffer = new StringBuffer();

                            if (0 < answerDatas.size()) {
                                // 回答文テキスト(answerText)と引用元URL(linkUrl)を表示し、引用元URLを利用してユーザが引用元のページへ遷移できるようにする。
                                // レイアウトの TextView に android:autoLink="web" を追加する。
                                strBuffer.append(textForDisplay + "\n" + answerDatas.get(0).getAnswerText()
                                        + " " + answerDatas.get(0).getLinkUrl());
                                // 雑談対話結果の表示
                                sListItemManager.setInterpretationResultParam(textForDisplay, false);
                                // 画面下までスクロール
                                scrollToBottom();

                                // 音声合成出力
                                startTextToSpeech(textForSpeech);
                            }
                        } else {
                            // 雑談対話結果の表示
                            sListItemManager.setInterpretationResultParam(textForSpeech, false);
                            // 画面下までスクロール
                            scrollToBottom();

                            // 音声合成出力
                            startTextToSpeech(textForSpeech);
                        }
                    }
                    break;
                case Uti.API_NOTIFY_EVENT:
                    break;
                case Uti.API_NOTIFY_ERROR:
                    // エラー表示
                    Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                    break;
                default:
                    break;
            }
        }

        /**
         * 発話理解の後処理
         */
        private void speechUnderstandingsProc(Message msg) {
            switch (msg.arg2) {
            case Uti.API_NOTIFY_RESULT:
                SentenceResultData resultData = (SentenceResultData) msg.obj;
                // 対話ステータス
                SentenceDialogStatusData dialogStatus = resultData.getDialogStatus();
                if (dialogStatus != null) {
                    // コマンド情報
                    SentenceCommandData command = dialogStatus.getCommand();
                    // コマンド情報の表示
                    sCommandTv.setText(command.getCommandId() + ":" + command.getCommandName());
                    // ユーザ発話内容
                    SentenceUserUtteranceData utterance = resultData.getUserUtterance();

                    switch(command.getCommandId()) {
                    case "BK00101":
                        // 知識Q&A
                        startKnowledgeAPI(utterance.getUtteranceText());
                        break;
                    default:
                        // 雑談対話
                        startDialogue(utterance.getUtteranceText());
                        break;
                    }
                }
                break;
            case Uti.API_NOTIFY_EVENT:
                break;
            case Uti.API_NOTIFY_ERROR:
                // エラー表示
                Toast.makeText(sContext, (String) msg.obj, Toast.LENGTH_LONG).show();
                break;
            default:
                break;
            }
        }
        };
}
