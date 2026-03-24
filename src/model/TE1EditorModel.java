package model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * エディタの状態を管理する Model クラス。
 *
 * 現在のファイルや未保存状態を保持し、
 * 状態変更時はリスナーへ通知する。
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
     * 現在のファイルを設定し、未保存状態をリセットする。
     *
     * ファイルを切り替えた直後は保存済み状態になるため、
     * modified は false に初期化される。
     */
    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;

        // ファイル切り替え直後は保存済み状態とする
        this.modified = false;

        notifyCurrentFileChanged();
        notifyModifiedChanged();
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
     * 未保存状態にする。
     *
     * Controller から modified フラグを直接 true / false にするのではなく、
     * 「どんな意味の変更か」を表すメソッド名で呼び出せるようにしている。
     *
     * これにより、呼び出し側の意図が読み取りやすくなり、
     * 状態変更時の通知処理も Model 側へまとめられる。
     */
    public void markAsModified() {
        // すでに未保存状態なら通知しない（無駄なUI更新を防ぐ）
        if (!modified) {
            modified = true;
            notifyModifiedChanged();
        }
    }

    /**
     * 保存済み状態にする。
     *
     * Controller から modified フラグを直接 true / false にするのではなく、
     * 「どんな意味の変更か」を表すメソッド名で呼び出せるようにしている。
     *
     * これにより、呼び出し側の意図が読み取りやすくなり、
     * 状態変更時の通知処理も Model 側へまとめられる。
     */
    public void markAsSaved() {
        if (modified) {
            modified = false;
            notifyModifiedChanged();
        }
    }

    /**
     * 保存可能かどうかを判定する。
     *
     * 未保存変更がある場合のみ保存可能とする。
     */
    public boolean canSave() {
        // 保存は「未保存変更があるときのみ」可能
        return modified;
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
     * 登録済みリスナーへ現在開いているファイルの変更を通知する。
     */
    private void notifyCurrentFileChanged() {
        for (TE1ModelListener listener : listeners) {
            listener.currentFileChanged(currentFile);
        }
    }

    /**
     * 登録済みリスナーへ未保存状態の変更を通知する。
     */
    private void notifyModifiedChanged() {
        for (TE1ModelListener listener : listeners) {
            listener.modifiedChanged(modified);
        }
    }

}