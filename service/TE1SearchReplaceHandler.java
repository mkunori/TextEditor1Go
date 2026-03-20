package service;

/**
 * TextEditor1Go の検索・置換機能を表すインターフェース。
 *
 * 検索ダイアログ側はこのインターフェースだけを知っていればよく、
 * 具体的な実装クラスに依存しないようにするために用いる。
 */
public interface TE1SearchReplaceHandler {

    /**
     * 指定された文字列を本文から検索する。
     *
     * @param keyword 検索文字列
     */
    void findText(String keyword);

    /**
     * 前回検索した文字列の次の一致位置を検索する。
     */
    void findNextText();

    /**
     * 指定された文字列を1件置換する。
     *
     * @param searchText      検索文字列
     * @param replacementText 置換文字列
     */
    void replaceText(String searchText, String replacementText);

    /**
     * 指定された文字列をすべて置換する。
     *
     * @param searchText      検索文字列
     * @param replacementText 置換文字列
     */
    void replaceAllText(String searchText, String replacementText);

    /**
     * 前回検索した文字列を返す。
     *
     * @return 前回検索した文字列
     */
    String getLastSearchText();
}