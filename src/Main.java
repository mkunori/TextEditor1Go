import javax.swing.*;

import controller.EditorController;

/**
 * TextEditor1Go を起動するクラス。
 *
 * 実際の画面制御やイベント処理は EditorController が担当する。
 */
public class Main {

    /**
     * アプリケーションを起動する。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EditorController controller = new EditorController();
            controller.show();
        });
    }
}