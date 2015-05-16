/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import com.fuetrek.fsr.FSRServiceEventListener;
import com.fuetrek.fsr.FSRServiceOpen;
import com.fuetrek.fsr.FSRServiceEnum.BackendType;
import com.fuetrek.fsr.FSRServiceEnum.EventType;
import com.fuetrek.fsr.entity.AbortInfoEntity;
import com.fuetrek.fsr.entity.ConstructorEntity;
import com.fuetrek.fsr.entity.RecognizeEntity;
import com.fuetrek.fsr.entity.ResultInfoEntity;
import com.fuetrek.fsr.entity.StartRecognitionEntity;
import com.fuetrek.fsr.exception.AbortException;
import com.fuetrek.fsr.exception.MemoryException;
import com.fuetrek.fsr.exception.NoDataException;
import com.fuetrek.fsr.exception.NoResourceException;
import com.fuetrek.fsr.exception.OperationException;
import com.fuetrek.fsr.exception.ParameterException;

/**
 * 音声認識FUETREK処理クラス
 */
public class SpeechRecognitionFU implements FSRServiceEventListener {
    private static final long SPEECH_TIME = 10 * 1000;
    private static final long RECORD_SIZE = 120;
    private static final long RECOGNIZE_TIME = 10 * 1000;

    // BackendTypeはBackendType.D固定
    private static final BackendType BACKEND_TYPE = BackendType.D;

    private FSRServiceOpen mFsrService;

    private Context mContext = null;        // Context
    private String mApiKey = "";            // APIキー
    private Handler mHandler;               // UIスレッドに通知するためのHandler

    /**
     * コンストラクタ
     */
    public SpeechRecognitionFU(Context context, String apiKey, Handler handler) {
        mContext = context;
        mApiKey = apiKey;
        mHandler = handler;
    }

    /**
     * 非同期処理
     */
    private final class FsrControlThread extends Thread {
        private final FSRServiceEventListener mFsr;

        private FsrControlThread(final FSRServiceEventListener fsr) {
            this.mFsr = fsr;
        }

        @Override
        public void run() {
            final ConstructorEntity construct = new ConstructorEntity();
            // アプリケーションコンテキストインスタンス設定
            construct.setContext(mContext);
            // APIキー設定
            construct.setApiKey(mApiKey);
            // 最大発話時間(msec)設定
            construct.setSpeechTime(SPEECH_TIME);
            // 音声入力バッファのフレームサイズ(msec)設定
            construct.setRecordSize(RECORD_SIZE);
            // 最大認識結果取得時間(msec)設定
            construct.setRecognizeTime(RECOGNIZE_TIME);

            try {
                //FSRServiceOpenインスタンス生成
                mFsrService = new FSRServiceOpen(this, mFsr, construct);
                mFsrService.connectSession(BACKEND_TYPE);
            } catch (final MemoryException | NoResourceException | ParameterException
                    | AbortException | OperationException e) {
                sendMessage("ネットワークエラー\nMessage: " + e.getMessage(), Uti.API_NOTIFY_ERROR, 0);
            }
        }
    }

    /**
     * 音声認識処理における各イベント通知
     */
    @Override
    public void notifyEvent(
            final Object appHandle, final EventType eventType,
            final BackendType backendType, final Object eventData) {
        switch (eventType) {

        case CompleteCancel:    // キャンセルを通知
            // UIスレッドへイベント通知
            sendMessage(null, Uti.API_NOTIFY_EVENT, Uti.NOTIFY_CANCEL);
            break;
        case CompleteConnect:   // 接続完了
            try {
                final StartRecognitionEntity startRecognitionEntity = new StartRecognitionEntity();
                // 有声検知での自動開始指定設定
                startRecognitionEntity.setAutoStart(true);
                // 無声検知での自動終了指定設定
                startRecognitionEntity.setAutoStop(true); // falseにする場合は
                                                          // UIからstopRecognition()実行する仕組みが必要
                // 自動終了時の無声判定時間(msec)設定
                startRecognitionEntity.setVadOffTime((short) 500);
                // 自動発話開始タイムアウト時間(msec)設定
                startRecognitionEntity.setListenTime(0);
                // 有声検知レベル閾値設定
                startRecognitionEntity.setLevelSensibility(10);
                // 認識開始
                mFsrService.startRecognition(BACKEND_TYPE, startRecognitionEntity);
            } catch (final AbortException | ParameterException | OperationException
                    | NoResourceException e) {
                sendMessage("ネットワークエラー\nMessage: " + e.getMessage(),
                        Uti.API_NOTIFY_ERROR, 0);
            }
            break;
        case CompleteDisconnect:    // 切断完了
            break;
        case CompleteStart:         // 音声認識開始処理完了
            // UIスレッドへイベント通知
            sendMessage(null, Uti.API_NOTIFY_EVENT, Uti.STATE_READY_FOR_SPEECH);
            break;
        case NotifyAutoStart:       // 自動認識開始
            // UIスレッドへイベント通知
            sendMessage(null, Uti.API_NOTIFY_EVENT, Uti.STATE_BEGINNING_OF_SPEECH);
            break;
        case NotifyEndRecognition:  // 認識完了
            try {
                String result = null;
                // 認識結果の取得
                final RecognizeEntity recogize = mFsrService.getSessionResultStatus(BACKEND_TYPE);

                if (recogize.getCount() > 0) {
                    final ResultInfoEntity info = mFsrService.getSessionResult(BACKEND_TYPE, 1);
                    result = info.getText();
                }
                // 切断
                mFsrService.disconnectSession(BACKEND_TYPE);
                // UIスレッドへ結果通知
                sendMessage(result, Uti.API_NOTIFY_RESULT, 0);
            } catch (final AbortException | ParameterException | OperationException
                    | NoDataException | NoResourceException e) {
                sendMessage("ネットワークエラー\nMessage: " + e.getMessage(), Uti.API_NOTIFY_ERROR, 0);
            } finally {
                if (mFsrService != null) {
                    mFsrService.destroy();
                    mFsrService = null;
                }
            }
            // UIスレッドへイベント通知
            sendMessage(null, Uti.API_NOTIFY_EVENT, Uti.STATE_END_OF_SPEECH);
            break;
        case NotifyLevel:           // レベルメータ更新
            break;
        default:
            break;
        }
    }

    /**
     * 処理異常発生イベント通知
     */
    @Override
    public void notifyAbort(final Object appHandle, final AbortInfoEntity abortInfo) {
        // UIスレッドへイベント通知
        sendMessage("音声認識を中止しました", Uti.API_NOTIFY_ERROR, 0);
    }

    /**
     * メッセージ送信
     */
    private void sendMessage(Object obj, int arg2, int what) {
        Message msg = new Message();
        msg.obj = obj;
        msg.arg1 = Uti.API_SPEECHRECOGNITION;
        msg.arg2 = arg2;
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    /**
    * 音声認識開始
    */
    public void start() {
       // 認識スレッドの開始
       new FsrControlThread(this).start();
    }

    /**
     * キャンセル
     */
    public void cancel() {
        try {
            if (mFsrService != null) {
                mFsrService.cancelRecognition();
                mFsrService.disconnectSession(BACKEND_TYPE);
            }
        } catch (AbortException | OperationException | ParameterException
                | NoResourceException e) {
            // エラー表示
            sendMessage(e.getMessage(), Uti.API_NOTIFY_ERROR, 0);
        } finally {
            if (mFsrService != null) {
                mFsrService.destroy();
                mFsrService = null;
            }
        }
    }
}
