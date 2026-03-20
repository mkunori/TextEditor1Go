import javax.swing.*;
import java.awt.*;

/**
 * TextEditor1Go の検索・置換機能を担当するクラス。
 *
 * JTextArea を対象に、検索、次を検索、1件置換、すべて置換を行う。
 */
public class TE1SearchService implements TE1SearchReplaceHandler {

    /** 検索対象のテキストエリア */
    private final JTextArea textArea;

    /** メッセージダイアログ表示用の親コンポーネント */
    private final Component parent;

    /** 前回検索した文字列 */
    private String lastSearchText;

    /**
     * 検索・置換サービスを初期化する。
     *
     * @param textArea 検索対象のテキストエリア
     * @param parent   メッセージダイアログ表示用の親コンポーネント
     */
    public TE1SearchService(JTextArea textArea, Component parent) {
        this.textArea = textArea;
        this.parent = parent;
    }

    /**
     * 前回検索した文字列を取得する。
     *
     * @return 前回検索した文字列
     */
    public String getLastSearchText() {
        return lastSearchText;
    }

    /**
     * 検索文字列を指定して本文から検索する。
     *
     * 検索は現在のカーソル位置から開始し、
     * テキスト末尾まで見つからなかった場合は
     * テキスト先頭から再検索する。
     *
     * @param keyword 検索文字列
     */
    public void findText(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return;
        }

        lastSearchText = keyword;

        String text = textArea.getText();
        int startIndex = textArea.getCaretPosition();
        int index = text.indexOf(lastSearchText, startIndex);

        if (index < 0) {
            index = text.indexOf(lastSearchText);
        }

        if (index < 0) {
            JOptionPane.showMessageDialog(parent, "文字列が見つかりませんでした。");
            return;
        }

        textArea.requestFocusInWindow();
        textArea.select(index, index + lastSearchText.length());
    }

    /**
     * 前回検索した文字列の次の一致位置を検索する。
     *
     * 検索は現在の選択範囲の直後から開始し、
     * テキスト末尾まで見つからなかった場合は
     * テキスト先頭から再検索する。
     */
    public void findNextText() {
        if (lastSearchText == null || lastSearchText.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "先に検索を実行してください。");
            return;
        }

        String text = textArea.getText();
        int startIndex = textArea.getSelectionEnd();
        int index = text.indexOf(lastSearchText, startIndex);

        if (index < 0) {
            index = text.indexOf(lastSearchText);
        }

        if (index < 0) {
            JOptionPane.showMessageDialog(parent, "文字列が見つかりませんでした。");
            return;
        }

        textArea.requestFocusInWindow();
        textArea.select(index, index + lastSearchText.length());
    }

    /**
     * 検索文字列を1件置換する。
     *
     * 一致箇所が選択されていない場合は、まず検索を行う。
     *
     * @param searchText      検索文字列
     * @param replacementText 置換文字列
     */
    public void replaceText(String searchText, String replacementText) {
        if (searchText == null || searchText.isEmpty()) {
            return;
        }

        String selectedText = textArea.getSelectedText();

        // 一致箇所が選択されていなければ、まず検索する。
        if (selectedText == null || !selectedText.equals(searchText)) {
            findText(searchText);
            selectedText = textArea.getSelectedText();
        }

        // 検索しても一致箇所が無ければ終了する。
        if (selectedText == null || !selectedText.equals(searchText)) {
            return;
        }

        textArea.requestFocusInWindow();
        textArea.replaceSelection(replacementText);

        lastSearchText = searchText;

        // 置換後に次の一致箇所を検索する。
        findNextText();
    }

    /**
     * 検索文字列をすべて置換文字列に置き換える。
     *
     * 本文中の一致箇所をすべて置換し、置換件数を表示する。
     *
     * @param searchText      検索文字列
     * @param replacementText 置換文字列
     */
    public void replaceAllText(String searchText, String replacementText) {
        if (searchText == null || searchText.isEmpty()) {
            return;
        }

        String text = textArea.getText();
        StringBuilder sb = new StringBuilder();

        int count = 0;
        int fromIndex = 0;
        int index;

        while ((index = text.indexOf(searchText, fromIndex)) >= 0) {
            sb.append(text, fromIndex, index);
            sb.append(replacementText);
            fromIndex = index + searchText.length();
            count++;
        }

        if (count == 0) {
            JOptionPane.showMessageDialog(parent, "文字列が見つかりませんでした。");
            return;
        }

        sb.append(text.substring(fromIndex));
        textArea.setText(sb.toString());
        lastSearchText = searchText;
        textArea.requestFocusInWindow();

        JOptionPane.showMessageDialog(parent, count + "件置換しました。");
    }
}