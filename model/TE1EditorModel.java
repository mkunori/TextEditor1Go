package model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * TextEditor1Go の状態を管理するモデルクラス。
 *
 * このクラスは画面表示やファイル操作そのものは行わず、
 * 「現在どのファイルを開いているか」
 * 「未保存変更があるか」
 * という状態だけを保持する。
 */
public class TE1EditorModel {

    /** 現在開いているファイル */
    private File currentFile;

    /** 未保存変更があるかどうか */
    private boolean modified;

    /** 状態変更通知を受け取るリスナー達 */
    private final List<TE1ModelListener> listeners = new ArrayList<>();

    /**
     * 現在開いているファイルを取得する。
     *
     * @return 現在開いているファイル。新規作成直後などは null
     */
    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * 現在開いているファイルを設定する。
     *
     * @param currentFile 現在開いているファイル。新規状態の場合は null
     */
    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
        notifyListeners();
    }

    /**
     * 未保存変更があるかどうかを返す。
     *
     * @return 未保存変更がある場合は true
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * 未保存変更状態を設定する。
     *
     * @param modified 未保存変更がある場合は true
     */
    public void setModified(boolean modified) {
        this.modified = modified;
        notifyListeners();
    }

    /**
     * モデル変更通知を受け取るリスナーを追加する。
     * 
     * @param listener 登録するリスナー
     */
    public void addModelListener(TE1ModelListener listener) {
        listeners.add(listener);
    }

    /**
     * 登録済みリスナーへ状態変更を通知する。
     */
    private void notifyListeners() {
        for (TE1ModelListener listener : listeners) {
            listener.modelChanged(this);
        }
    }

}