package controller;

import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

/**
 * Undo / Redo 操作を担当する Controller クラス。
 *
 * このクラスは UndoManager をラップし、
 * 実際の Undo / Redo 実行を担当する。
 */
public class TE1UndoController {

    private final UndoManager undoManager;

    public TE1UndoController(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    /**
     * Undo を実行する。
     */
    public void undo() {
        if (undoManager.canUndo()) {
            try {
                undoManager.undo();
            } catch (CannotUndoException e) {
                // 必要ならログ出力
            }
        }
    }

    /**
     * Redo を実行する。
     */
    public void redo() {
        if (undoManager.canRedo()) {
            try {
                undoManager.redo();
            } catch (CannotRedoException e) {
                // 必要ならログ出力
            }
        }
    }

    /**
     * Undo履歴をクリアする。
     */
    public void clear() {
        undoManager.discardAllEdits();
    }
}