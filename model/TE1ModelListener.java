package model;

/**
 * TE1EditorModel の状態変更通知を受け取るリスナー
 */
public interface TE1ModelListener {

    /**
     * Model の状態が変更されたときに呼ばれる。
     * 
     * @param model 変更後のモデル
     */
    void modelChanged(TE1EditorModel model);
}
