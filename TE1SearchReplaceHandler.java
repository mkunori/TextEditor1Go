/**
 * 検索置換ダイアログから呼び出す検索・置換機能のインターフェース。
 */
public interface TE1SearchReplaceHandler {

    /**
     * 検索文字列を指定して検索する。
     *
     * @param searchText 検索文字列
     */
    void findText(String searchText);

    /**
     * 検索文字列を1件置換する。
     *
     * @param searchText      検索文字列
     * @param replacementText 置換文字列
     */
    void replaceText(String searchText, String replacementText);

    /**
     * 検索文字列をすべて置換する。
     *
     * @param searchText      検索文字列
     * @param replacementText 置換文字列
     */
    void replaceAllText(String searchText, String replacementText);

    /**
     * 前回検索した文字列を取得する。
     *
     * @return 前回検索した文字列
     */
    String getLastSearchText();
}