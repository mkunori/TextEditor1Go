import javax.swing.*;
import java.awt.*;
import java.io.*;

public class TE1Main extends JFrame {
    private JTextArea textArea;
    private File currentFile; // 現在開いているファイル

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

        // メニューを生成する
        createMenu();
    }

    // メニューを生成する
    private void createMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        // メニュー項目を用意する
        JMenuItem newItem = new JMenuItem("New"); // 新規に始める
        JMenuItem openItem = new JMenuItem("Open"); // ファイルを開く
        JMenuItem saveItem = new JMenuItem("Save"); // ファイルを上書き保存する
        JMenuItem saveAsItem = new JMenuItem("Save As"); // ファイルに名前をつけて保存する

        newItem.addActionListener(e -> newFile());
        openItem.addActionListener(e -> openFile());
        saveItem.addActionListener(e -> saveFile());
        saveAsItem.addActionListener(e -> saveAsFile());

        fileMenu.add(newItem);
        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(saveAsItem);

        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    // 新規に始める
    public void newFile() {
        textArea.setText("");
    }

    // ファイルを開く
    public void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            currentFile = chooser.getSelectedFile();
            try (BufferedReader br = new BufferedReader(new FileReader(currentFile))) {
                textArea.read(br, null);
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

}
