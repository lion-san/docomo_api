/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

/**
 * 定数を定義するユーティリティクラス
 */
public final class Uti {
    // API識別子
    public static final int API_DIALOGUE            = 1;
    public static final int API_KNOWLEDGE           = 2;
    public static final int API_SPEECHRECOGNITION   = 3;
    public static final int API_SPEECHUNDERSTANDING = 4;
    public static final int API_TEXTTOSPEECH        = 5;

    // APIからの通知関連
    public static final int API_NOTIFY_RESULT   = 1;
    public static final int API_NOTIFY_EVENT    = 2;
    public static final int API_NOTIFY_ERROR    = 3;

    /** 音声認識スタンバイ ({@value})*/
    public static final int STATE_NON                       = 1;
    /** 音声認識発話待ち ({@value})*/
    public static final int STATE_READY_FOR_SPEECH          = 3;
    /** 音声認識発話中 ({@value})*/
    public static final int STATE_BEGINNING_OF_SPEECH       = 4;
    /** 音声認識発話終了 ({@value})*/
    public static final int STATE_END_OF_SPEECH             = 5;
    /** エラーコード：音声認識サーバ応答なし ({@value})*/
    public static final int ERROR_CODE_SERVER_NO_RESPONSE   = 100;
    /** エラーコード：音声認識サーバ接続エラー ({@value})*/
    public static final int ERROR_CODE_SERVER_CONNECT       = 101;
    /** エラーコード：該当結果なし ({@value})*/
    public static final int ERROR_CODE_NO_MATCH             = 102;
    /** エラーコード：発話なし ({@value})*/
    public static final int ERROR_CODE_SPEECH_TIMEOUT       = 103;
    // 結果通知
    public static final int NOTIFY_RESULT                   = 200;
    // キャンセル
    public static final int NOTIFY_CANCEL                   = 999;

    private Uti() { };
}
