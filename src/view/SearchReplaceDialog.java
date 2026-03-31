package view;

import javax.swing.*;

import service.SearchReplaceHandler;

import java.awt.*;

/**
 * TextEditor の検索・置換ダイアログ。
 *
 * 検索文字列と置換文字列の入力欄を持ち、
 * 「次を検索」「置換」「すべて置換」の操作を提供する。
 *
 * 実際の検索・置換処理そのものは持たず、
 * SearchReplaceHandler へ処理を委譲する。
 */
public class SearchReplaceDialog extends JDialog {

    /** 検索文字列入力欄 */
    private final JTextField searchField;

    /** 置換文字列入力欄 */
    private final JTextField replaceField;

    /** 検索・置換処理の委譲先 */
    private final SearchReplaceHandler searchHandler;

    /**
     * 検索・置換ダイアログを生成する。
     *
     * @param owner         親ウィンドウ
     * @param searchHandler 検索・置換処理の委譲先
     */
    public SearchReplaceDialog(JFrame owner, SearchReplaceHandler searchHandler) {
        super(owner, "検索 / 置換", false);
        this.searchHandler = searchHandler;

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

        findNextButton.addActionListener(e -> {
            String searchText = searchField.getText();

            if (searchText != null && !searchText.isEmpty()) {
                String lastSearchText = searchHandler.getLastSearchText();

                // 入力した検索語が前回と違う場合は、
                // 「次を検索」ではなく新しい検索として扱う。
                // これにより、検索語を変えた直後でも正しく先頭の一致候補を探せる。
                if (!searchText.equals(lastSearchText)) {
                    searchHandler.findText(searchText);
                    return;
                }
            }

            // 検索語が前回と同じなら、続きから次を検索する。
            searchHandler.findNextText();
        });

        replaceButton.addActionListener(e -> searchHandler.replaceText(
                searchField.getText(), replaceField.getText()));

        replaceAllButton.addActionListener(e -> searchHandler.replaceAllText(
                searchField.getText(), replaceField.getText()));

        closeButton.addActionListener(e -> setVisible(false));

        setLayout(new BorderLayout(5, 5));
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 検索欄に文字列を設定する。
     *
     * 前回検索した語をダイアログ表示時に引き継ぐために利用する。
     *
     * @param text 検索欄へ設定する文字列
     */
    public void setSearchText(String text) {
        searchField.setText(text);
    }
}