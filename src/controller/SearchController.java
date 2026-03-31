package controller;

import javax.swing.JOptionPane;

import service.SearchReplaceHandler;
import view.EditorView;
import view.SearchReplaceDialog;

/**
 * TextEditor の検索・置換まわりの制御を担当する Controller クラス。
 *
 * このクラスは以下の役割を持つ。
 * - 検索文字列入力ダイアログの表示
 * - 次を検索の実行
 * - 検索・置換ダイアログの生成と再利用
 * - 検索・置換処理を SearchReplaceHandler へ委譲
 *
 * 実際の検索・置換ロジックそのものは持たず、
 * SearchReplaceHandler の実装へ処理を委譲する。
 */
public class SearchController {

    /** 画面表示を担当する View */
    private final EditorView view;

    /** 検索・置換処理の委譲先 */
    private final SearchReplaceHandler searchHandler;

    /** 検索・置換ダイアログ */
    private SearchReplaceDialog searchReplaceDialog;

    /**
     * 検索・置換用 Controller を初期化する。
     *
     * @param view          メイン画面
     * @param searchHandler 検索・置換処理の委譲先
     */
    public SearchController(EditorView view, SearchReplaceHandler searchHandler) {
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

        if (!isValidKeyword(keyword)) {
            return;
        }

        searchHandler.findText(keyword);
    }

    /**
     * 検索文字列として有効かどうかを判定する。
     *
     * @param keyword 入力された検索文字列
     * @return キャンセルでなく、かつ空文字でない場合は true
     */
    private boolean isValidKeyword(String keyword) {
        return keyword != null && !keyword.isEmpty();
    }

    /**
     * 前回検索した文字列の次の一致位置を検索する。
     */
    public void findNextText() {
        searchHandler.findNextText();
    }

    /**
     * 検索・置換ダイアログを表示する。
     */
    public void showSearchReplaceDialog() {
        prepareSearchReplaceDialog();
        searchReplaceDialog.setVisible(true);
    }

    /**
     * 検索・置換ダイアログを表示前の状態に整える。
     *
     * 必要ならダイアログを生成し、
     * 前回検索した文字列があれば検索欄へ反映する。
     */
    private void prepareSearchReplaceDialog() {
        if (searchReplaceDialog == null) {
            searchReplaceDialog = new SearchReplaceDialog(view, searchHandler);
        }

        String lastSearchText = searchHandler.getLastSearchText();
        if (lastSearchText != null) {
            searchReplaceDialog.setSearchText(lastSearchText);
        }
    }
}
