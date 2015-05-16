/*
 * 2015 NTT DOCOMO, INC. All Rights Reserved.
 * 提供コードを使用又は利用するためには、以下のURLリンク先のウェブページに掲載される本規約に同意する必要があります。
 * https://dev.smt.docomo.ne.jp/?p=common_page&p_name=samplecode_policy
 */

package jp.ne.docomo.smt.dev.mrconcierge;

import jp.ne.docomo.smt.dev.sentenceunderstanding.common.SentenceProjectSpecific;

/**
 * アプリ固有情報サンプルデータクラス
 */
public class SampleSentenceProjectSpecific extends SentenceProjectSpecific {

    /** サンプルＩＤ */
    private String mSampleId = null;

    /**
     * サンプルＩＤを取得する。
     * @return サンプルＩＤ
     */
    public String getSampleId() {
        return mSampleId;
    }

    /**
     * サンプルＩＤを設定する。
     * @param sampleId
     *            サンプルＩＤ
     */
    public void setSampleId(String sampleId) {
        this.mSampleId = sampleId;
    }
}

