package controller;

import javax.swing.*;
import javax.swing.undo.UndoManager;

import model.TE1EditorModel;
import model.TE1ModelListener;
import service.TE1FileService;
import service.TE1SearchReplaceHandler;
import service.TE1SearchService;
import service.TE1UndoSupport;
import view.TE1EditorView;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

/**
 * TextEditor1Go 全体の制御を担当する Controller クラス。
 *
 * このクラスはアプリケーションの司令塔として、
 * View、Model、各機能別 Controller / Service を接続し、
 * 画面全体の動作を調整する。
 *
 * 主な役割は以下の通り。
 * - View、Model、各種 Controller / Service の生成と接続
 * - Document や Caret に関する共通編集イベントの管理
 * - Undo / Redo の制御
 * - Model の変更通知を受け取り、必要な View 更新へつなぐ
 *
 * ファイル操作は TE1FileController、
 * 検索・置換まわりの操作は TE1SearchController が担当する。
 */
public class TE1EditorController implements TE1ModelListener {

    /** 画面表示を担当する View */
    private final TE1EditorView view;

    /** アプリケーション状態を保持する Model */
    private final TE1EditorModel model;

    /** ファイル操作を担当する Controller */
    private final TE1FileController fileController;

    /** 検索・置換まわりを担当する Controller */
    private final TE1SearchController searchController;

    /** Undo / Redo の本体 */
    private final UndoManager undoManager;

    /** Undo / Redo 操作を担当する Controller */
    private final TE1UndoController undoController;

    /** Undo 編集を補助するクラス */
    private final TE1UndoSupport undoSupport;

    /** 検索・置換処理の橋渡し */
    private final TE1SearchReplaceHandler searchService;

    /** ファイル読み書きの本体 */
    private final TE1FileService fileService;

    /** Document / Caret の共通イベントを管理するクラス */
    private final TE1EditorEventHandler eventHandler;

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
        undoController = new TE1UndoController(undoManager);

        searchService = new TE1SearchService(view.getTextArea(), view, undoSupport);
        searchController = new TE1SearchController(view, searchService);

        fileService = new TE1FileService();

        eventHandler = new TE1EditorEventHandler(
                view.getTextArea(),
                this::handleDocumentChanged,
                view::updateStatusBar);

        fileController = new TE1FileController(
                view,
                model,
                undoController,
                fileService,
                this::reinstallDocumentListener);

        registerWindowListener();
        registerEditorEventListeners();
        installUndoListener();
        registerMenuActions();
        registerUndoRedoKeyBindings();

        view.updateLineNumbers();
        view.updateStatusBar();
        updateTitle();
        updateSaveActions();
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
     * Document 更新時の共通処理を行う。
     *
     * Document の変更が発生したら、
     * modified フラグ、行番号、ステータスバーを更新する。
     */
    private void handleDocumentChanged() {
        model.markAsModified();
        view.updateLineNumbers();
        view.updateStatusBar();
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

        view.getUndoItem().addActionListener(e -> undoController.undo());
        view.getRedoItem().addActionListener(e -> undoController.redo());
        view.getFindItem().addActionListener(e -> searchController.findText());
        view.getFindNextItem().addActionListener(e -> searchController.findNextText());

        // 置換とすべて置換は、どちらも同じ検索置換ダイアログを開く。
        view.getReplaceItem().addActionListener(e -> searchController.showSearchReplaceDialog());
        view.getReplaceAllItem().addActionListener(e -> searchController.showSearchReplaceDialog());
    }

    /**
     * Model の状態をもとにウィンドウタイトルを更新する。
     *
     * 現在のファイル名と未保存変更の有無を View へ反映する。
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
                undoController.undo();
            }
        });

        actionMap.put("editorRedo", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                undoController.redo();
            }
        });
    }

    /**
     * 現在開いているファイルが変更されたときの通知を受け取る。
     *
     * @param currentFile 変更後のファイル。新規状態の場合は null
     */
    @Override
    public void currentFileChanged(File currentFile) {
        handleCurrentFileChanged();
    }

    /**
     * 未保存変更状態が変更されたときの通知を受け取る。
     *
     * @param modified 未保存変更がある場合は true
     */
    @Override
    public void modifiedChanged(boolean modified) {
        handleModifiedChanged();
    }

    /**
     * 現在ファイル変更時に必要な画面更新を行う。
     *
     * 現時点ではタイトル更新を行う。
     * 今後、ファイル変更に応じた表示更新はこのメソッドへ集約する。
     */
    private void handleCurrentFileChanged() {
        updateTitle();
        updateSaveActions();
    }

    /**
     * 未保存変更状態の変更時に必要な画面更新を行う。
     *
     * 現時点ではタイトル更新を行う。
     * 今後、未保存変更状態に応じた表示更新はこのメソッドへ集約する。
     */
    private void handleModifiedChanged() {
        updateTitle();
        updateSaveActions();
    }

    /**
     * 保存系UIの表示状態を更新する。
     *
     * 現在は保存メニューの有効 / 無効を切り替える。
     * 未保存変更がある場合のみ「保存」を有効にする。
     *
     * 「名前を付けて保存」は常に実行できるため、有効のままとする。
     */
    private void updateSaveActions() {
        view.getSaveItem().setEnabled(model.canSave());
        view.getSaveAsItem().setEnabled(true);
    }

    /**
     * エディタ全体に関わる共通イベントリスナーを登録する。
     *
     * 現在は以下を対象とする。
     * - Document の変更監視
     * - Caret の移動監視
     */
    private void registerEditorEventListeners() {
        eventHandler.installDocumentListener();
        eventHandler.installCaretListener();
    }

    /**
     * 現在の Document に対して変更監視リスナーを再登録する。
     *
     * JTextArea.read(...) 実行後は内部の Document が差し替わることがあるため、
     * FileController からこのメソッドを呼び出して再登録する。
     */
    private void reinstallDocumentListener() {
        eventHandler.installDocumentListener();
        installUndoListener();
    }

    /**
     * 現在の Document に対して Undo 用リスナーを登録する。
     *
     * UndoManager に編集履歴を記録するため、
     * Document が差し替わった場合は再登録が必要になる。
     */
    private void installUndoListener() {
        view.getTextArea().getDocument().addUndoableEditListener(undoSupport);
    }
}