package controller;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.CaretListener;

/**
 * エディタの共通イベント（Document / Caret）を管理するクラス。
 *
 * このクラスは以下の役割を持つ。
 * - Document の変更監視
 * - Caret の移動監視
 *
 * 実際の処理は外部から渡されたコールバックへ委譲する。
 */
public class TE1EditorEventHandler {

    /** 監視対象のテキストエリア */
    private final JTextArea textArea;

    /** Document 変更時に呼び出すコールバック */
    private final Runnable documentChangedHandler;

    /** Caret 移動時に呼び出すコールバック */
    private final Runnable caretChangedHandler;

    /**
     * コンストラクタ
     *
     * @param textArea               対象のテキストエリア
     * @param documentChangedHandler Document変更時の処理
     * @param caretChangedHandler    Caret移動時の処理
     */
    public TE1EditorEventHandler(
            JTextArea textArea,
            Runnable documentChangedHandler,
            Runnable caretChangedHandler) {

        this.textArea = textArea;
        this.documentChangedHandler = documentChangedHandler;
        this.caretChangedHandler = caretChangedHandler;
    }

    /**
     * 現在のDocumentに対してリスナーを登録する。
     *
     * JTextArea.read(...) により Document が差し替わるため、
     * 必要に応じて再登録する。
     */
    public void installDocumentListener() {
        textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                documentChangedHandler.run();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                documentChangedHandler.run();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                documentChangedHandler.run();
            }
        });
    }

    /**
     * Caretリスナーを登録する。
     */
    public void installCaretListener() {
        textArea.addCaretListener(e -> caretChangedHandler.run());
    }
}