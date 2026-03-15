import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
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
    private JTextArea textArea;
    private File currentFile; // 現在開いているファイル
    private UndoManager undoManager; // Undo / Redo

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
     * テキストエリア、Undo/Redo機能、メニューバーを生成する。
     */
    public TE1Main() {
        setTitle("テキストエディタ-1号");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Undo/Redo機能を準備する。
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

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
        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);
        menuBar.add(fileMenu);

        // 編集項目を用意する
        JMenu editMenu = new JMenu("編集");
        JMenuItem undoItem = new JMenuItem("元に戻す");
        JMenuItem redoItem = new JMenuItem("やり直す");
        undoItem.addActionListener(e -> undo());
        redoItem.addActionListener(e -> redo());
        undoItem.setAccelerator(KeyStroke.getKeyStroke("control Z"));
        redoItem.setAccelerator(KeyStroke.getKeyStroke("control Y"));
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);
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
        undoManager.discardAllEdits(); // 新規時は履歴を削除する。
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
                // 新たに開いた直後はUndo/Redoが無いはずなので過去の履歴を削除する。
                undoManager.discardAllEdits();
                // 新たに開いたファイルにはリスナー登録が無いので再登録する。
                textArea.getDocument().addUndoableEditListener(undoManager);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 現在のファイルにテキスト内容を保存する。
     *
     * 現在のファイルが未指定の場合は、
     * 名前を付けて保存の処理を行う。
     */
    public void saveFile() {
        // 現在開いているファイルが未指定のときは新規に保存する。
        if (currentFile == null) {
            saveAsFile();
            return;
        }
        // 現在開いているファイルにテキスト内容を書き込む。
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentFile))) {
            textArea.write(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ファイル名を指定してテキスト内容を保存する。
     *
     * JFileChooser を表示して保存先を選択し、
     * 選択されたファイルに内容を書き込む。
     */
    public void saveAsFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            saveFile();
            setTitle(currentFile.getName());
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
}
