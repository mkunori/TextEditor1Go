package controller;

import javax.swing.JOptionPane;

import service.TE1SearchReplaceHandler;
import view.TE1EditorView;
import view.TE1SearchReplaceDialog;

/**
 * TextEditor1Go の検索・置換まわりの制御を担当する Controller クラス。
 *
 * このクラスは以下の役割を持つ。
 * - 検索文字列入力ダイアログの表示
 * - 次を検索の実行
 * - 検索・置換ダイアログの生成と再利用
 * - 検索・置換処理を TE1SearchReplaceHandler へ委譲
 *
 * 実際の検索・置換ロジックそのものは持たず、
 * TE1SearchReplaceHandler の実装へ処理を委譲する。
 */
public class TE1SearchController {

    /** 画面表示を担当する View */
    private final TE1EditorView view;

    /** 検索・置換処理の委譲先 */
    private final TE1SearchReplaceHandler searchHandler;

    /** 検索・置換ダイアログ */
    private TE1SearchReplaceDialog searchReplaceDialog;

    /**
     * 検索・置換用 Controller を初期化する。
     *
     * @param view          メイン画面
     * @param searchHandler 検索・置換処理の委譲先
     */
    public TE1SearchController(TE1EditorView view, TE1SearchReplaceHandler searchHandler) {
        this.view = view;
        this.searchHandler = searchHandler;
    }

    /**
     * 検索文字列を入力して検索する。
     * 
     * 入力がキャンセルされた場合や空文字の場合は何もしない。
     */
    public void findText() {
        String keyword = JOptionPane.showInputDialog(view, "検索する文字列を入力してください。");

        if (keyword == null || keyword.isEmpty()) {
            return;
        }

        searchHandler.findText(keyword);
    }

    /**
     * 前回検索した文字列の次の一致位置を検索する。
     */
    public void findNextText() {
        searchHandler.findNextText();
    }

    /**
     * 検索・置換ダイアログを表示する。
     *
     * ダイアログは最初の1回だけ生成し、その後は再利用する。
     * 毎回 new しないことで、前回入力した検索語を保持しやすくなる。
     */
    public void showSearchReplaceDialog() {
        if (searchReplaceDialog == null) {
            searchReplaceDialog = new TE1SearchReplaceDialog(view, searchHandler);
        }

        String lastSearchText = searchHandler.getLastSearchText();
        if (lastSearchText != null) {
            searchReplaceDialog.setSearchText(lastSearchText);
        }

        searchReplaceDialog.setVisible(true);
    }
}
