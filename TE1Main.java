import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

/**
 * TextEditor1Go のメインウィンドウクラス。
 *
 * JTextArea を中心としたシンプルなテキストエディタを実装する。
 * 以下の機能を持つ。
 * - 新規作成
 * - ファイルの読み込み
 * - 保存 / 名前を付けて保存
 * - Undo / Redo
 */
public class TE1Main extends JFrame {

    /** テキストエリア */
    private JTextArea textArea;

    /** 行番号エリア */
    private JTextArea lineNumberArea;

    /** 現在開いているファイル */
    private File currentFile;

    /** Undo / Redo 機能用オブジェクト */
    private UndoManager undoManager;

    /** 未保存変更があるか */
    private boolean modified = false;

    /**
     * アプリケーションを起動する。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TE1Main().setVisible(true);
        });
    }

    /**
     * メインウィンドウを初期化する。
     *
     * テキストエリア、行番号エリア、Undo / Redo機能、
     * メニューバーを生成する。
     */
    public TE1Main() {
        setTitle("テキストエディタ-1号");
        setSize(600, 400);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        // 未保存でウィンドウを閉じたときは警告を表示する。
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                confirmClose();
            }
        });

        // 本文用エリア
        textArea = new JTextArea();

        // 行番号用エリア
        lineNumberArea = new JTextArea("1");
        lineNumberArea.setEditable(false);
        lineNumberArea.setBackground(Color.LIGHT_GRAY);
        lineNumberArea.setFont(textArea.getFont());

        // スクロール付きテキストエリアを作成し、左側に行番号を表示する。
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setRowHeaderView(lineNumberArea);
        add(scrollPane, BorderLayout.CENTER);

        // Undo / Redo機能を準備する。
        undoManager = new UndoManager();

        // リスナーを登録する。
        registerDocumentListeners();

        // 初期状態の行番号を表示する。
        updateLineNumbers();

        // メニューを生成する。
        createMenu();
    }

    /**
     * メインウィンドウのメニューバーを構築する。
     *
     * ファイルメニューと編集メニューを作成し、
     * 各メニュー項目に対応するアクションを設定する。
     */
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        // ファイル項目を用意する。
        JMenu fileMenu = new JMenu("ファイル");
        JMenuItem newItem = new JMenuItem("新規作成");
        JMenuItem openItem = new JMenuItem("開く");
        JMenuItem saveItem = new JMenuItem("保存");
        JMenuItem saveAsItem = new JMenuItem("名前を付けて保存");

        newItem.addActionListener(e -> newFile());
        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());
        saveAsItem.addActionListener(e -> saveAsFile());
        newItem.setAccelerator(KeyStroke.getKeyStroke("control N"));
        openItem.setAccelerator(KeyStroke.getKeyStroke("control O"));
        saveItem.setAccelerator(KeyStroke.getKeyStroke("control S"));
        saveAsItem.setAccelerator(KeyStroke.getKeyStroke("control shift S"));

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        menuBar.add(fileMenu);

        // 編集項目を用意する。
        JMenu editMenu = new JMenu("編集");
        JMenuItem undoItem = new JMenuItem("元に戻す");
        JMenuItem redoItem = new JMenuItem("やり直す");
        JMenuItem findItem = new JMenuItem("検索");

        undoItem.addActionListener(e -> undo());
        redoItem.addActionListener(e -> redo());
        findItem.addActionListener(e -> findText());
        undoItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        redoItem.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        findItem.setAccelerator(KeyStroke.getKeyStroke("control F"));

        editMenu.add(undoItem);
        editMenu.add(redoItem);
        editMenu.addSeparator();
        editMenu.add(findItem);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);
    }

    /**
     * Document にリスナーを登録する。
     */
    private void registerDocumentListeners() {
        registerLineNumberListener();
        textArea.getDocument().addUndoableEditListener(undoManager);
    }

    /**
     * 行番号更新用のリスナーを登録する。
     */
    private void registerLineNumberListener() {
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                setModified(true);
                updateLineNumbers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                setModified(true);
                updateLineNumbers();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                setModified(true);
                updateLineNumbers();
            }
        });
    }

    /**
     * 新規ファイルを作成する。
     * 
     * テキスト内容を空にし、現在のファイル情報をリセットする。
     * また Undo / Redo の履歴もクリアする。
     */
    public void newFile() {
        textArea.setText("");
        currentFile = null;
        modified = false;

        undoManager.discardAllEdits();
        updateLineNumbers();
        setTitle("テキストエディタ-1号");
    }

    /**
     * ファイルを選択して読み込む。
     * 
     * JFileChooser を表示してユーザーにファイルを選択させ、
     * 選択されたファイルの内容を JTextArea に読み込む。
     */
    public void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();

            try (BufferedReader br = new BufferedReader(new FileReader(currentFile))) {
                textArea.read(br, null);

                setModified(false);

                // 新たに開いた直後はUndo / Redoが無いはずなので過去の履歴を削除する。
                undoManager.discardAllEdits();

                // 新しい Document にはリスナー登録が無いので再登録する。
                registerDocumentListeners();

                updateLineNumbers();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 現在のファイルにテキスト内容を保存する。
     *
     * 保存先が未指定、または元ファイルが存在しない場合は、
     * 「名前を付けて保存」の処理に切り替える。
     */
    public void saveFile() {
        if (currentFile == null || !currentFile.exists()) {
            saveAsFile();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentFile))) {
            textArea.write(bw);
            setModified(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 保存先を指定してテキスト内容を保存する。
     *
     * JFileChooser を表示してユーザーに保存先を選択させ、
     * 選択されたファイルに内容を保存する。
     */
    public void saveAsFile() {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            saveFile();
        }
    }

    /**
     * 直前の編集操作を取り消す。
     */
    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    /**
     * 取り消した編集操作をやり直す。
     */
    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }

    /**
     * 本文の行数に応じて行番号を更新する。
     */
    private void updateLineNumbers() {
        int lineCount = textArea.getLineCount();
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i <= lineCount; i++) {
            sb.append(i).append(System.lineSeparator());
        }

        lineNumberArea.setText(sb.toString());
    }

    /**
     * ウィンドウタイトルを更新する。
     */
    private void updateTitle() {
        String title;

        if (currentFile == null) {
            title = "テキストエディタ-1号";
        } else {
            title = currentFile.getName();
        }

        if (modified) {
            title += " *";
        }

        setTitle(title);
    }

    /**
     * 未保存変更状態を設定する。
     * 
     * 編集による変更の有無を管理するフラグを設定し、
     * タイトルバーの表示（* の有無）を更新する。
     * 
     * @param modified 未保存変更がある場合は true
     */
    private void setModified(boolean modified) {
        this.modified = modified;
        updateTitle();
    }

    /**
     * ウィンドウを閉じる前に未保存変更を確認する。
     */
    private void confirmClose() {
        if (!modified) {
            dispose();
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                this,
                "変更内容を保存しますか？",
                "確認",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (result == JOptionPane.YES_NO_OPTION) {
            saveFile();

            // 保存後に未保存状態でなければ閉じる
            if (!modified) {
                dispose();
            }
        } else if (result == JOptionPane.NO_OPTION) {
            dispose();
        }
    }

    /**
     * 検索文字列を入力して本文から検索する。
     */
    public void findText() {
        String keyword = JOptionPane.showInputDialog(this, "検索する文字列を入力してください。");

        if (keyword == null || keyword.isEmpty()) {
            return;
        }

        String text = textArea.getText();
        int index = text.indexOf(keyword);

        if (index >= 0) {
            textArea.requestFocus();
            textArea.select(index, index + keyword.length());
        } else {
            JOptionPane.showMessageDialog(this, "文字列が見つかりませんでした。");
        }
    }
}
