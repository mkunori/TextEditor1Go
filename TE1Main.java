import javax.swing.*;
import java.awt.*;
import java.io.*;

public class TE1Main extends JFrame {
    private JTextArea textArea;

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
        JMenuItem open = new JMenuItem("Open"); // ファイルを開く
        JMenuItem save = new JMenuItem("Save"); // ファイルを保存する

        open.addActionListener(e -> openFile());
        save.addActionListener(e -> saveFile());

        fileMenu.add(open);
        fileMenu.add(save);
        menuBar.add(fileMenu);

        setJMenuBar(menuBar);
    }

    // ファイルを開く
    public void openFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedReader br =
                    new BufferedReader(new FileReader(chooser.getSelectedFile()))) {
                textArea.read(br, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ファイルを保存する
    public void saveFile() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (BufferedWriter bw =
                    new BufferedWriter(new FileWriter(chooser.getSelectedFile()))) {
                textArea.write(bw);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
