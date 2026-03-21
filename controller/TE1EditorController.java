package controller;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.undo.UndoManager;

import model.TE1EditorModel;
import model.TE1ModelListener;
import service.TE1SearchReplaceHandler;
import service.TE1SearchService;
import service.TE1UndoSupport;
import view.TE1EditorView;
import view.TE1SearchReplaceDialog;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * TextEditor1Go 全体の制御を担当する Controller クラス。
 *
 * このクラスは以下の役割を持つ。
 * - View、Model、各種 Controller / Service の生成と接続
 * - Document やキャレットに関する共通イベント登録
 * - Undo / Redo
 * - Model の変更通知を受けて View を更新する
 *
 * ファイル操作は TE1FileController、
 * 検索・置換まわりの操作は TE1SearchController が担当する。
 */
public class TE1EditorController implements TE1ModelListener {

    /** 画面表示を担当する View */
    private final TE1EditorView view;

    /** エディタの状態を保持する Model */
    private final TE1EditorModel model;

    /** ファイル操作を担当する Controller */
    private final TE1FileController fileController;

    /** 検索・置換まわりを担当する Controller */
    private final TE1SearchController searchController;

    /** Undo / Redo の本体 */
    private final UndoManager undoManager;

    /** Undo 用の補助クラス */
    private final TE1UndoSupport undoSupport;

    /** 検索・置換処理の橋渡し */
    private final TE1SearchReplaceHandler searchService;

    /**
     * Controller を初期化する。
     *
     * View、Model、各種サービスを生成し、
     * イベントリスナーを登録する。
     */
    public TE1EditorController() {
        view = new TE1EditorView();
        model = new TE1EditorModel();

        model.addModelListener(this);

        undoManager = new UndoManager();
        undoSupport = new TE1UndoSupport(undoManager);

        searchService = new TE1SearchService(view.getTextArea(), view, undoSupport);
        searchController = new TE1SearchController(view, searchService);

        fileController = new TE1FileController(
                view,
                model,
                undoManager,
                this::installListenersToCurrentDocument);

        registerWindowListener();
        registerDocumentListeners();
        registerCaretListener();
        registerMenuActions();
        registerUndoRedoKeyBindings();

        view.updateLineNumbers();
        view.updateStatusBar();
        updateTitle();
    }

    /**
     * メイン画面を表示する。
     */
    public void show() {
        view.setVisible(true);
    }

    /**
     * ウィンドウ終了時の処理を登録する。
     *
     * 閉じるボタンが押されたときに、
     * 直接終了せず confirmClose() を経由する。
     */
    private void registerWindowListener() {
        view.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                fileController.confirmClose();
            }
        });
    }

    /**
     * 現在の Document に対して変更監視リスナーを登録する。
     *
     * JTextArea.read(...) 実行後は内部の Document が差し替わることがあるため、
     * 読み込み後は新しい Document に対して再登録する必要がある。
     *
     * 現時点では DocumentListener の再登録のみを行う。
     */
    private void registerDocumentListeners() {
        installListenersToCurrentDocument();
    }

    /**
     * 現在の Document に対して変更監視用リスナーを登録する。
     * 
     * テキスト変更時に以下を行う：
     * - modified フラグ更新
     * - 行番号更新
     * - ステータスバー更新
     * 
     * JTextArea.read(...) 実行後は Document が差し替わるため、
     * このメソッドで再登録する必要がある。
     */
    private void installListenersToCurrentDocument() {
        view.getTextArea().getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                handleDocumentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                handleDocumentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                handleDocumentChanged();
            }
        });
    }

    /**
     * Document 更新時の共通処理を行う。
     *
     * DocumentListener の3メソッドに同じ処理を書かないため、
     * 共通化している。
     */
    private void handleDocumentChanged() {
        model.setModified(true);
        view.updateLineNumbers();
        view.updateStatusBar();
    }

    /**
     * キャレット移動時のリスナーを登録する。
     *
     * キャレットとは、現在の入力位置を表すカーソルのこと。
     * 位置が変わるたびにステータスバーを更新する。
     */
    private void registerCaretListener() {
        view.getTextArea().addCaretListener(e -> view.updateStatusBar());
    }

    /**
     * メニュー項目へアクションを登録する。
     *
     * View は部品を持つだけにし、
     * 何をするかは Controller 側で決める。
     */
    private void registerMenuActions() {
        view.getNewItem().addActionListener(e -> fileController.newFile());
        view.getOpenItem().addActionListener(e -> fileController.openFile());
        view.getSaveItem().addActionListener(e -> fileController.saveFile());
        view.getSaveAsItem().addActionListener(e -> fileController.saveAsFile());

        view.getUndoItem().addActionListener(e -> undo());
        view.getRedoItem().addActionListener(e -> redo());
        view.getFindItem().addActionListener(e -> searchController.findText());
        view.getFindNextItem().addActionListener(e -> searchController.findNextText());

        // 置換とすべて置換は、どちらも同じ検索置換ダイアログを開く。
        view.getReplaceItem().addActionListener(e -> searchController.showSearchReplaceDialog());
        view.getReplaceAllItem().addActionListener(e -> searchController.showSearchReplaceDialog());
    }

    /**
     * 直前の編集操作を取り消す。
     *
     * Undo 実行後は表示内容も変化するため、
     * 行番号とステータスバーもあわせて更新する。
     */
    public void undo() {
        if (undoManager.canUndo()) {
            undoManager.undo();
            view.updateLineNumbers();
            view.updateStatusBar();
        }
    }

    /**
     * 取り消した編集操作をやり直す。
     *
     * Redo 実行後は表示内容も変化するため、
     * 行番号とステータスバーもあわせて更新する。
     */
    public void redo() {
        if (undoManager.canRedo()) {
            undoManager.redo();
            view.updateLineNumbers();
            view.updateStatusBar();
        }
    }

    /**
     * 現在のファイル名と未保存状態をタイトルへ反映する。
     */
    private void updateTitle() {
        String fileName = (model.getCurrentFile() == null)
                ? "無題"
                : model.getCurrentFile().getName();

        view.updateTitle(fileName, model.isModified());
    }

    /**
     * 本文テキストエリアに Undo / Redo のキー割り当てを登録する。
     *
     * メニューのアクセラレータだけに依存すると、
     * 検索・置換ダイアログが前面にある場合に Ctrl+Z / Ctrl+Y が
     * メイン画面へ届かないことがある。
     *
     * そのため、本文エリア自身にもキー操作を登録しておく。
     */
    private void registerUndoRedoKeyBindings() {
        JTextArea textArea = view.getTextArea();

        InputMap inputMap = textArea.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = textArea.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke("control Z"), "editorUndo");
        inputMap.put(KeyStroke.getKeyStroke("control Y"), "editorRedo");

        actionMap.put("editorUndo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                undo();
            }
        });

        actionMap.put("editorRedo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                redo();
            }
        });
    }

    @Override
    public void modelChanged(TE1EditorModel model) {
        // Model の状態変更通知を受けたら、
        // 現在のファイル名と未保存状態をタイトルへ反映する。
        updateTitle();
    }
}