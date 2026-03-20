package service;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;

/**
 * TextEditor1Go の検索・置換機能を担当するサービスクラス。
 *
 * JTextArea を対象として、
 * 検索、次を検索、1件置換、すべて置換を行う。
 */
public class TE1SearchService implements TE1SearchReplaceHandler {

    /** 検索対象のテキストエリア */
    private final JTextArea textArea;

    /** メッセージダイアログ表示用の親コンポーネント */
    private final Component parent;

    /** 前回検索した文字列 */
    private String lastSearchText;

    /** まとめ Undo を扱うための補助クラス */
    private final TE1UndoSupport undoSupport;

    /**
     * 検索・置換サービスを初期化する。
     *
     * @param textArea    検索対象のテキストエリア
     * @param parent      メッセージダイアログ表示用の親コンポーネント
     * @param undoSupport Undo 編集補助
     */
    public TE1SearchService(JTextArea textArea, Component parent, TE1UndoSupport undoSupport) {
        this.textArea = textArea;
        this.parent = parent;
        this.undoSupport = undoSupport;
    }

    /**
     * 前回検索した文字列を返す。
     *
     * @return 前回検索した文字列
     */
    @Override
    public String getLastSearchText() {
        return lastSearchText;
    }

    /**
     * 指定された文字列を本文から検索する。
     *
     * 検索開始位置は現在のキャレット位置とし、
     * 末尾まで見つからなければ先頭へ戻って再検索する。
     *
     * @param keyword 検索文字列
     */
    @Override
    public void findText(String keyword) {
        if (keyword == null || keyword.isEmpty()) {
            return;
        }

        lastSearchText = keyword;

        String text = textArea.getText();
        int startIndex = textArea.getCaretPosition();

        // indexOf(検索語, 開始位置) を使うことで、
        // 現在のカーソル位置以降から検索できる。
        int index = text.indexOf(lastSearchText, startIndex);

        // 現在位置以降で見つからなければ、先頭から検索し直す。
        if (index < 0) {
            index = text.indexOf(lastSearchText);
        }

        if (index < 0) {
            JOptionPane.showMessageDialog(parent, "文字列が見つかりませんでした。");
            return;
        }

        textArea.requestFocusInWindow();
        textArea.select(index, index + lastSearchText.length());

        // ダイアログのボタン押下イベント中にフォーカスを変更しても、
        // その後のイベント処理で元のコンポーネント（ダイアログ）に
        // フォーカスが戻されてしまうことがある。
        //
        // そのため、invokeLater を使って「イベント処理がすべて終わったあと」に
        // フォーカスをテキストエリアへ戻す。
        SwingUtilities.invokeLater(() -> textArea.requestFocusInWindow());
    }

    /**
     * 前回検索した文字列の次の一致位置を検索する。
     *
     * 開始位置は現在の選択範囲の直後とし、
     * 末尾まで見つからなければ先頭へ戻って再検索する。
     */
    @Override
    public void findNextText() {
        if (lastSearchText == null || lastSearchText.isEmpty()) {
            JOptionPane.showMessageDialog(parent, "先に検索を実行してください。");
            return;
        }

        String text = textArea.getText();

        // 選択中の一致箇所の直後から次を検索する。
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

        // ダイアログのボタン押下イベント中にフォーカスを変更しても、
        // その後のイベント処理で元のコンポーネント（ダイアログ）に
        // フォーカスが戻されてしまうことがある。
        //
        // そのため、invokeLater を使って「イベント処理がすべて終わったあと」に
        // フォーカスをテキストエリアへ戻す。
        SwingUtilities.invokeLater(() -> textArea.requestFocusInWindow());
    }

    /**
     * 指定された文字列を1件置換する。
     *
     * 置換後はテキストエリアへフォーカスを戻す。
     * （ダイアログ操作中はフォーカスが移動しないため）
     *
     * @param searchText      検索文字列
     * @param replacementText 置換文字列
     */
    @Override
    public void replaceText(String searchText, String replacementText) {
        if (searchText == null || searchText.isEmpty()) {
            return;
        }

        String selectedText = textArea.getSelectedText();

        if (selectedText == null || !selectedText.equals(searchText)) {
            findText(searchText);
            selectedText = textArea.getSelectedText();
        }

        if (selectedText == null || !selectedText.equals(searchText)) {
            return;
        }

        textArea.replaceSelection(replacementText);
        lastSearchText = searchText;

        findNextText();

        // ダイアログのボタン押下イベント中にフォーカスを変更しても、
        // その後のイベント処理で元のコンポーネント（ダイアログ）に
        // フォーカスが戻されてしまうことがある。
        //
        // そのため、invokeLater を使って「イベント処理がすべて終わったあと」に
        // フォーカスをテキストエリアへ戻す。
        SwingUtilities.invokeLater(() -> textArea.requestFocusInWindow());
    }

    /**
     * 指定された文字列をすべて置換する。
     *
     * 一致箇所を順番に Document へ反映し、
     * 最後に置換件数をダイアログで表示する。
     *
     * @param searchText      検索文字列
     * @param replacementText 置換文字列
     */
    @Override
    public void replaceAllText(String searchText, String replacementText) {
        if (searchText == null || searchText.isEmpty()) {
            return;
        }

        Document doc = textArea.getDocument();

        try {
            String text = doc.getText(0, doc.getLength());

            int count = 0;
            int index = 0;
            boolean started = false;

            try {
                while ((index = text.indexOf(searchText, index)) >= 0) {
                    if (!started) {
                        // すべて置換を1回の Undo で戻せるように、
                        // 最初の置換前にまとめ編集を開始する。
                        undoSupport.beginCompoundEdit();
                        started = true;
                    }

                    // 一致箇所を削除してから、同じ位置へ置換文字列を挿入する。
                    doc.remove(index, searchText.length());
                    doc.insertString(index, replacementText, null);

                    // 次の検索開始位置は、今入れた置換文字列の直後にする。
                    index += replacementText.length();

                    // Document の内容は remove / insert のたびに変化するので、
                    // 次の indexOf のために毎回文字列を取り直す必要がある。
                    text = doc.getText(0, doc.getLength());

                    count++;
                }
            } finally {
                if (started) {
                    undoSupport.endCompoundEdit();
                }
            }

            if (count == 0) {
                JOptionPane.showMessageDialog(parent, "文字列が見つかりませんでした。");
                return;
            }

            lastSearchText = searchText;

            // ダイアログ操作の直後はフォーカスがダイアログ側へ残りやすいため、
            // イベント処理が終わったあとに本文へフォーカスを戻す。
            SwingUtilities.invokeLater(() -> textArea.requestFocusInWindow());

            JOptionPane.showMessageDialog(parent, count + "件置換しました。");

        } catch (BadLocationException e) {
            // Document の不正な位置を操作した場合に発生する。
            // 通常は起きにくいが、安全のため例外を表示しておく。
            e.printStackTrace();
        }
    }
}