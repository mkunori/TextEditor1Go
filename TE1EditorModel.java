import java.io.File;

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
    }
}