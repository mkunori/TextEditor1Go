package model;

import java.io.File;

/**
 * EditorModel の状態変更通知を受け取るリスナー
 */
public interface ModelListener {

    /**
     * 現在開いているファイルが変更されたときに呼ばれる。
     *
     * @param currentFile 変更後のファイル。新規状態の場合は null
     */
    void currentFileChanged(File currentFile);

    /**
     * 未保存変更状態が変更されたときに呼ばれる。
     *
     * @param modified 未保存変更がある場合は true
     */
    void modifiedChanged(boolean modified);
}