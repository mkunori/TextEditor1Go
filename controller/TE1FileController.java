package controller;

import java.io.File;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import model.TE1EditorModel;
import service.TE1FileService;
import view.TE1EditorView;

/**
 * TextEditor1Go のファイル操作を担当する Controller クラス。
 *
 * このクラスは以下の役割を持つ。
 * - 新規作成
 * - ファイルの読み込み
 * - 上書き保存
 * - 名前を付けて保存
 * - 終了時の保存確認
 * 
 * 実際のファイル入出力は TE1FileService に委譲し、
 * このクラスは「処理の流れ」を制御する役割を持つ。
 *
 * 状態更新は Model へ反映し、
 * タイトル更新などの表示変更は Observer を通じて別途反映される。
 */
public class TE1FileController {

    /** 画面表示を担当する View */
    private final TE1EditorView view;

    /** エディタの状態を保持する Model */
    private final TE1EditorModel model;

    /** Undo / Redo 操作を担当する Controller */
    private final TE1UndoController undoController;

    /** ファイル読み書きの本体 */
    private final TE1FileService fileService;

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
     * @param view                      メイン画面
     * @param model                     エディタの状態を保持する Model
     * @param undoController            Undo / Redo 操作を担当する Controller
     * @param fileService               ファイル読み書きの本体
     * @param documentListenerInstaller Document リスナー再登録用コールバック
     */
    public TE1FileController(
            TE1EditorView view,
            TE1EditorModel model,
            TE1UndoController undoController,
            TE1FileService fileService,
            Runnable documentListenerInstaller) {
        this.view = view;
        this.model = model;
        this.undoController = undoController;
        this.fileService = fileService;
        this.documentListenerInstaller = documentListenerInstaller;
    }

    /**
     * 新規ファイルを作成する。
     *
     * テキスト内容、現在ファイル、未保存状態、Undo 履歴を初期化する。
     */
    public void newFile() {
        // テキスト内容をクリア
        view.getTextArea().setText("");

        // currentFile を null にすることで「無題状態」にする
        // 同時に Model 側で modified は false に初期化される
        model.setCurrentFile(null);

        refreshAfterFileContentChanged();
    }

    /**
     * ファイルを選択して読み込む。
     */
    public void openFile() {
        JFileChooser chooser = new JFileChooser();

        if (chooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();

            try {
                // ファイル内容を読み込む（I/O本体はServiceへ委譲）
                String content = fileService.readFile(selectedFile);

                // テキストエリアへ反映
                view.getTextArea().setText(content);

                // 現在ファイルを更新（ここで modified は false になる）
                model.setCurrentFile(selectedFile);

                // Undo履歴はファイル単位でリセットする
                undoController.clear();

                refreshAfterFileContentChanged();

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

        try {
            fileService.writeFile(
                    model.getCurrentFile(),
                    view.getTextArea().getText());

            model.markAsSaved();

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

    /**
     * ファイル内容の切り替え後に必要な共通後処理を行う。
     * 
     * 読み込み直後や新規作成直後の状態を基準にするため、
     * Undo 履歴を破棄し、画面表示を更新する。
     */
    private void refreshAfterFileContentChanged() {
        undoController.clear();
        view.updateLineNumbers();
        view.updateStatusBar();
    }
}
