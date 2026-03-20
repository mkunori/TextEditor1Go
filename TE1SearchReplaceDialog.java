import javax.swing.*;
import java.awt.*;

public class TE1SearchReplaceDialog extends JDialog {

    /** 検索文字列入力欄 */
    private JTextField searchField;

    /** 置換文字列入力欄 */
    private JTextField replaceField;

    /**
     * 検索置換ダイアログを表示する。
     *
     * 初回呼び出し時にダイアログを生成し、以降は再利用する。
     * 前回検索した文字列がある場合は、検索欄に初期値をして設定する。
     */
    public TE1SearchReplaceDialog(TE1Main owner) {
        super(owner, "検索 / 置換", false);

        setSize(400, 150);
        setLocationRelativeTo(owner);

        searchField = new JTextField(20);
        replaceField = new JTextField(20);

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        inputPanel.add(new JLabel("検索"));
        inputPanel.add(searchField);
        inputPanel.add(new JLabel("置換"));
        inputPanel.add(replaceField);

        JButton findNextButton = new JButton("次を検索");
        JButton replaceButton = new JButton("置換");
        JButton replaceAllButton = new JButton("すべて置換");
        JButton closeButton = new JButton("閉じる");

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(findNextButton);
        buttonPanel.add(replaceButton);
        buttonPanel.add(replaceAllButton);
        buttonPanel.add(closeButton);

        findNextButton.addActionListener(e -> owner.findFromDialog());
        replaceButton.addActionListener(e -> owner.replaceFromDialog());
        replaceAllButton.addActionListener(e -> owner.replaceAllFromDialog());
        closeButton.addActionListener(e -> setVisible(false));

        setLayout(new BorderLayout(5, 5));
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 検索文字列を取得する。
     */
    public String getSearchText() {
        return searchField.getText();
    }

    /**
     * 置換文字列を取得する。
     */
    public String getReplaceText() {
        return replaceField.getText();
    }

    /**
     * 検索文字列を設定する。
     */
    public void setSearchText(String text) {
        searchField.setText(text);
    }
}