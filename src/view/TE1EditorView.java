package view;

import javax.swing.*;
import java.awt.*;

/**
 * TextEditor1Go の画面表示を担当する View クラス。
 *
 * テキストエリア、行番号エリア、ステータスバー、
 * メニューバーなどの UI 部品を構築・保持する。
 *
 * このクラスは「見た目」と「表示更新」を担当し、
 * ファイル保存や検索などの処理本体は持たない。
 */
public class TE1EditorView extends JFrame {

    /** 本文入力用テキストエリア */
    private final JTextArea textArea;

    /** 行番号表示用エリア */
    private final JTextArea lineNumberArea;

    /** ステータスバー */
    private final JLabel statusLabel;

    /** メニュー項目 */
    private final JMenuItem newItem;
    private final JMenuItem openItem;
    private final JMenuItem saveItem;
    private final JMenuItem saveAsItem;
    private final JMenuItem undoItem;
    private final JMenuItem redoItem;
    private final JMenuItem findItem;
    private final JMenuItem findNextItem;
    private final JMenuItem replaceItem;
    private final JMenuItem replaceAllItem;

    /**
     * メイン画面を初期化する。
     */
    public TE1EditorView() {
        setTitle("テキストエディタ-1号");
        setSize(600, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        textArea = new JTextArea();

        lineNumberArea = new JTextArea("1");
        lineNumberArea.setEditable(false);
        lineNumberArea.setBackground(Color.LIGHT_GRAY);
        lineNumberArea.setFont(textArea.getFont());

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setRowHeaderView(lineNumberArea);
        add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JLabel("Ln 1, Col 1");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 6, 2, 6));
        add(statusLabel, BorderLayout.SOUTH);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("ファイル");
        newItem = new JMenuItem("新規作成");
        openItem = new JMenuItem("開く");
        saveItem = new JMenuItem("保存");
        saveAsItem = new JMenuItem("名前を付けて保存");

        newItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke("control shift S"));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        menuBar.add(fileMenu);

        JMenu editMenu = new JMenu("編集");
        undoItem = new JMenuItem("元に戻す");
        redoItem = new JMenuItem("やり直す");
        findItem = new JMenuItem("検索");
        findNextItem = new JMenuItem("次を検索");
        replaceItem = new JMenuItem("置換");
        replaceAllItem = new JMenuItem("すべて置換");

        undoItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        redoItem.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        findItem.setAccelerator(KeyStroke.getKeyStroke("control F"));
        findNextItem.setAccelerator(KeyStroke.getKeyStroke("F3"));

        // Ctrl+H は JTextArea 側で削除操作と競合しやすいため、
        // 置換には Ctrl+R を割り当てている。
        replaceItem.setAccelerator(KeyStroke.getKeyStroke("control R"));
        replaceAllItem.setAccelerator(KeyStroke.getKeyStroke("control shift R"));

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(findItem);
        editMenu.add(findNextItem);
        editMenu.add(replaceItem);
        editMenu.add(replaceAllItem);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);
    }

    /**
     * 行番号表示を更新する。
     *
     * JTextArea の行数を数え、1行ごとの番号を左側へ表示する。
     */
    public void updateLineNumbers() {
        int lineCount = textArea.getLineCount();
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= lineCount; i++) {
            sb.append(i).append(System.lineSeparator());
        }

        lineNumberArea.setText(sb.toString());
    }

    /**
     * ステータスバー表示を更新する。
     *
     * キャレット位置から現在の行・列を求め、
     * あわせて総行数、文字数、選択文字数も表示する。
     */
    public void updateStatusBar() {
        try {
            int caretPos = textArea.getCaretPosition();
            int line = textArea.getLineOfOffset(caretPos);
            int lineStart = textArea.getLineStartOffset(line);
            int column = caretPos - lineStart;

            int lineCount = textArea.getLineCount();
            int charCount = textArea.getText().length();
            int selectedCount = textArea.getSelectionEnd() - textArea.getSelectionStart();

            statusLabel.setText(
                    "Ln " + (line + 1)
                            + ", Col " + (column + 1)
                            + " | 行数: " + lineCount
                            + " | 文字数: " + charCount
                            + " | 選択: " + selectedCount);
        } catch (Exception e) {
            // 取得タイミングによっては行や位置の計算に失敗することがあるため、
            // その場合は初期表示に戻す。
            statusLabel.setText("Ln 1, Col 1 | 行数: 1 | 文字数: 0 | 選択: 0");
        }
    }

    /**
     * ウィンドウタイトルを設定する。
     *
     * @param title 表示タイトル
     */
    public void setWindowTitle(String title) {
        setTitle(title);
    }

    /**
     * 本文入力用テキストエリアを返す。
     *
     * Controller 側で DocumentListener や CaretListener を
     * 登録するために利用する。
     *
     * @return 本文入力用テキストエリア
     */
    public JTextArea getTextArea() {
        return textArea;
    }

    /**
     * 新規作成メニュー項目を返す。
     *
     * @return 新規作成メニュー項目
     */
    public JMenuItem getNewItem() {
        return newItem;
    }

    /**
     * 開くメニュー項目を返す。
     *
     * @return 開くメニュー項目
     */
    public JMenuItem getOpenItem() {
        return openItem;
    }

    /**
     * 保存メニュー項目を返す。
     *
     * @return 保存メニュー項目
     */
    public JMenuItem getSaveItem() {
        return saveItem;
    }

    /**
     * 名前を付けて保存メニュー項目を返す。
     *
     * @return 名前を付けて保存メニュー項目
     */
    public JMenuItem getSaveAsItem() {
        return saveAsItem;
    }

    /**
     * 元に戻すメニュー項目を返す。
     *
     * @return 元に戻すメニュー項目
     */
    public JMenuItem getUndoItem() {
        return undoItem;
    }

    /**
     * やり直すメニュー項目を返す。
     *
     * @return やり直すメニュー項目
     */
    public JMenuItem getRedoItem() {
        return redoItem;
    }

    /**
     * 検索メニュー項目を返す。
     *
     * @return 検索メニュー項目
     */
    public JMenuItem getFindItem() {
        return findItem;
    }

    /**
     * 次を検索メニュー項目を返す。
     *
     * @return 次を検索メニュー項目
     */
    public JMenuItem getFindNextItem() {
        return findNextItem;
    }

    /**
     * 置換メニュー項目を返す。
     *
     * @return 置換メニュー項目
     */
    public JMenuItem getReplaceItem() {
        return replaceItem;
    }

    /**
     * すべて置換メニュー項目を返す。
     *
     * @return すべて置換メニュー項目
     */
    public JMenuItem getReplaceAllItem() {
        return replaceAllItem;
    }

    /**
     * タイトルを更新する。
     * 
     * @param fileName ファイル名
     * @param modified 未保存変更があるか
     */
    public void updateTitle(String fileName, boolean modified) {
        String modifiedMark = modified ? " *" : "";
        setTitle("テキストエディタ-1号 - " + fileName + modifiedMark);
    }
}