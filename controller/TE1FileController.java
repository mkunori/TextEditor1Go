package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.undo.UndoManager;

import model.TE1EditorModel;
import view.TE1EditorView;

/**
 * TextEditor1Go 全体の制御を担当する Controller クラス。
 *
 * このクラスは以下の役割を持つ。
 * - View、Model、各種サブ Controller / Service の生成と接続
 * - Model の変更通知を受けて View を更新する
 * - Undo / Redo
 * - 検索 / 置換機能との連携
 *
 * ファイル操作そのものは TE1FileController が担当する。
 */
public class TE1FileController {

    /** 画面表示を担当する View */
    private final TE1EditorView view;

    /** エディタの状態を保持する Model */
    private final TE1EditorModel model;

    /** Undo / Redo の本体 */
    private final UndoManager undoManager;

    /**
     * Document のリスナーを再登録するためのコールバック。
     *
     * ファイル読み込み（JTextArea.read(...)）後は、
     * 内部の Document が別インスタンスに置き換わることがある。
     *
     * そのため、変更監視用のリスナーを新しい Document に
     * 付け直す必要があり、このコールバックで再登録を行う。
     */
    private final Runnable documentListenerInstaller;

    /**
     * ファイル操作用 Controller を初期化する。
     *
     * View、Model、Undo、Document リスナーを登録する。
     */
    public TE1FileController(
            TE1EditorView view,
            TE1EditorModel model,
            UndoManager undoManager,
            Runnable documentListenerInstaller) {
        this.view = view;
        this.model = model;
        this.undoManager = undoManager;
        this.documentListenerInstaller = documentListenerInstaller;
    }

    /**
     * 新規ファイルを作成する。
     *
     * テキスト内容、現在ファイル、未保存状態、Undo 履歴を初期化する。
     */
    public void newFile() {
        view.getTextArea().setText("");
        model.setCurrentFile(null);
        model.setModified(false);

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
                // 新しい Document にリスナーを再登録する。
                documentListenerInstaller.run();

                model.setCurrentFile(selectedFile);
                model.setModified(false);

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
            model.setModified(false);
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
            model.setCurrentFile(chooser.getSelectedFile());
            saveFile();
        }
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

}
