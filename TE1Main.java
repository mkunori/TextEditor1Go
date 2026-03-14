import javax.swing.*;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.io.*;

public class TE1Main extends JFrame {
    private JTextArea textArea;
    private File currentFile; // 現在開いているファイル
    private UndoManager undoManager; // Undo/Redo

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TE1Main().setVisible(true);
        });
    }

    // コンストラクタ
    public TE1Main() {
        setTitle("テキストエディタ-1号");
        setSize(600, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        textArea = new JTextArea();
        add(new JScrollPane(textArea), BorderLayout.CENTER);

        // Undo/Redo機能を準備する
        undoManager = new UndoManager();
        textArea.getDocument().addUndoableEditListener(undoManager);

        // メニューを生成する
        createMenu();

    }

    // メニューを生成する
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();

        // ファイル項目を用意する
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
        editMenu.add(undoItem);
        editMenu.add(redoItem);
        menuBar.add(editMenu);

        setJMenuBar(menuBar);
    }

    // 新規に始める
    public void newFile() {
        textArea.setText("");
        undoManager.discardAllEdits(); // 新規時は履歴を削除する
    }

    // ファイルを開く
    public void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(currentFile))) {
                textArea.read(br, null);
                // 新たに開いた直後はUndo/Redoが無いはずなので過去の履歴を削除する
                undoManager.discardAllEdits();
                // 新たに開いたファイルにはリスナー登録が無いので再登録する
                textArea.getDocument().addUndoableEditListener(undoManager);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ファイルを上書き保存する
    public void saveFile() {
        // 現在開いているファイルが未指定のときは新規に保存する
        if (currentFile == null) {
            saveAsFile();
            return;
        }
        // 現在開いているファイルにテキスト内容を書き込む
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(currentFile))) {
            textArea.write(bw);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ファイルに名前をつけて保存する
    public void saveAsFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            saveFile();
            setTitle(currentFile.getName());
        }
    }

    // Undoを実行する
    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
        }
    }

    // Redoを実行する
    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
        }
    }
}
