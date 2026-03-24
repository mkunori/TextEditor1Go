import javax.swing.*;

import controller.TE1EditorController;

/**
 * TextEditor1Go を起動するクラス。
 *
 * 実際の画面制御やイベント処理は TE1EditorController が担当する。
 */
public class TE1Main {

    /**
     * アプリケーションを起動する。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            TE1EditorController controller = new TE1EditorController();
            controller.show();
        });
    }
}