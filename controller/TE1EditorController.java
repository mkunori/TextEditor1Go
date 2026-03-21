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
import java.io.*;

/**
 * TextEditor1Go の制御を担当する Controller クラス。
 *
 * このクラスは以下の役割を持つ。
 * - View と Model の生成と接続
 * - メニュー操作やウィンドウ操作への応答
 * - ファイルの新規作成、読み込み、保存
 * - Undo / Redo
 * - 検索 / 置換機能との連携
 *
 * 画面そのものの構築は TE1EditorView、
 * 状態保持は TE1EditorModel が担当する。
 */
public class TE1EditorController implements TE1ModelListener {

    /** 画面表示を担当する View */
    private final TE1EditorView view;

    /** エディタの状態を保持する Model */
    private final TE1EditorModel model;

    /** Undo / Redo の本体 */
    private final UndoManager undoManager;

    /** Undo 用の補助クラス */
    private final TE1UndoSupport undoSupport;

    /** 検索・置換ダイアログ */
    private TE1SearchReplaceDialog searchReplaceDialog;

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
                confirmClose();
            }
        });
    }

    /**
     * Document 関連のリスナーを登録する。
     *
     * 初期表示時に現在の Document へ監視処理を接続する。
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
        setModified(true);
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
        view.getNewItem().addActionListener(e -> newFile());
        view.getOpenItem().addActionListener(e -> openFile());
        view.getSaveItem().addActionListener(e -> saveFile());
        view.getSaveAsItem().addActionListener(e -> saveAsFile());

        view.getUndoItem().addActionListener(e -> undo());
        view.getRedoItem().addActionListener(e -> redo());
        view.getFindItem().addActionListener(e -> findText());
        view.getFindNextItem().addActionListener(e -> findNextText());

        // 置換とすべて置換は、どちらも同じ検索置換ダイアログを開く。
        view.getReplaceItem().addActionListener(e -> showSearchReplaceDialog());
        view.getReplaceAllItem().addActionListener(e -> showSearchReplaceDialog());
    }

    /**
     * 新規ファイルを作成する。
     *
     * テキスト内容、現在ファイル、未保存状態、Undo 履歴を初期化する。
     */
    public void newFile() {
        view.getTextArea().setText("");
        setCurrentFile(null);
        setModified(false);

        undoManager.discardAllEdits();
        view.updateLineNumbers();
        view.updateStatusBar();
    }

    /**
     * ファイルを選択して読み込む。
     */
    public void openFile() {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();

            try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                view.getTextArea().read(br, null);

                // read(...) 後は Document が差し替わることがあるため、
                // 新しい Documento にリスナーを登録し直す。
                installListenersToCurrentDocument();

                setCurrentFile(selectedFile);
                setModified(false);

                // 読み込み直後を「編集前の基準状態」にするため、
                // それ以前の Undo 履歴は破棄する。
                undoManager.discardAllEdits();

                view.updateLineNumbers();
                view.updateStatusBar();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(view, "ファイルを開けませんでした。");
            }
        }
    }

    /**
     * 現在のファイルへ保存する。
     *
     * 保存先が未設定なら「名前を付けて保存」へ委譲する。
     */
    public void saveFile() {
        if (model.getCurrentFile() == null) {
            saveAsFile();
            return;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(model.getCurrentFile()))) {
            view.getTextArea().write(bw);
            setModified(false);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(view, "保存に失敗しました。");
        }
    }

    /**
     * 保存先を選択して保存する。
     */
    public void saveAsFile() {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
            setCurrentFile(chooser.getSelectedFile());
            saveFile();
        }
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
     * 検索文字列を入力して検索する。
     */
    public void findText() {
        String keyword = JOptionPane.showInputDialog(view, "検索する文字列を入力してください。");

        if (keyword == null || keyword.isEmpty()) {
            return;
        }

        searchService.findText(keyword);
    }

    /**
     * 前回検索した文字列の次の一致位置を検索する。
     */
    public void findNextText() {
        searchService.findNextText();
    }

    /**
     * 検索・置換ダイアログを表示する。
     *
     * ダイアログは使い回すことで、
     * 毎回生成するより状態を保持しやすくする。
     */
    public void showSearchReplaceDialog() {
        if (searchReplaceDialog == null) {
            searchReplaceDialog = new TE1SearchReplaceDialog(view, searchService);
        }

        String lastSearchText = searchService.getLastSearchText();
        if (lastSearchText != null) {
            searchReplaceDialog.setSearchText(lastSearchText);
        }

        searchReplaceDialog.setVisible(true);
    }

    /**
     * 終了前に保存確認を行う。
     *
     * 未保存変更がない場合はそのまま終了する。
     */
    public void confirmClose() {
        if (!model.isModified()) {
            view.dispose();
            return;
        }

        int result = JOptionPane.showConfirmDialog(
                view,
                "変更が保存されていません。保存しますか？",
                "終了確認",
                JOptionPane.YES_NO_CANCEL_OPTION);

        if (result == JOptionPane.YES_OPTION) {
            saveFile();

            // 名前を付けて保存でキャンセルされた場合は、
            // modified が true のままなので終了しない。
            if (!model.isModified()) {
                view.dispose();
            }
        } else if (result == JOptionPane.NO_OPTION) {
            view.dispose();
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
     * 現在編集中のファイルを設定する。
     *
     * @param file 現在のファイル。新規状態なら null
     */
    private void setCurrentFile(File file) {
        model.setCurrentFile(file);
    }

    /**
     * 未保存変更状態を設定する。
     *
     * @param modified 未保存変更がある場合は true
     */
    private void setModified(boolean modified) {
        model.setModified(modified);
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
        updateTitle();
    }
}