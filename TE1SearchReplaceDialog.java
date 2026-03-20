import javax.swing.*;
import java.awt.*;

public class TE1SearchReplaceDialog extends JDialog {

    /** 検索文字列入力欄 */
    private JTextField searchField;

    /** 置換文字列入力欄 */
    private JTextField replaceField;

    /** 検索機能 */
    private TE1SearchService searchService;

    /**
     * 検索置換ダイアログを生成する。
     *
     * @param owner         親ウィンドウ
     * @param searchService 検索・置換機能
     */
    public TE1SearchReplaceDialog(JFrame owner, TE1SearchService searchService) {
        this.searchService = searchService;

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

        findNextButton.addActionListener(e -> searchService.findText(
                searchField.getText()));
        replaceButton.addActionListener(e -> searchService.replaceText(
                searchField.getText(),
                replaceField.getText()));
        replaceAllButton.addActionListener(e -> searchService.replaceAllText(
                searchField.getText(),
                replaceField.getText()));

        closeButton.addActionListener(e -> setVisible(false));

        setLayout(new BorderLayout(5, 5));
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 検索文字列を設定する。
     */
    public void setSearchText(String text) {
        searchField.setText(text);
    }
}