import java.io.File;

/**
 * エディタの状態を管理するモデルクラス
 */
public class TE1EditorModel {

    /** 現在開いているファイル */
    private File currentFile;

    /** 未保存変更があるか */
    private boolean modified;

    /**
     * 現在開いているファイルを取得する。
     * 
     * @return currentFile 現在開いているファイル
     */
    public File getCurrentFile() {
        return currentFile;
    }

    /**
     * 現在開いているファイルを設定する。
     * 
     * @param currentFile 現在開いているファイル
     */
    public void setCurrentFile(File currentFile) {
        this.currentFile = currentFile;
    }

    /**
     * 未保存変更があるかを取得する。
     *
     * @return 未保存変更がある場合は true
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * 未保存変更状態を設定する。
     * 
     * @param modified 未保存状態
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
