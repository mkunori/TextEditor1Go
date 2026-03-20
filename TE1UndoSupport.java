import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

/**
 * Undo / Redo 用の編集履歴を管理するクラス
 */
public class TE1UndoSupport implements UndoableEditListener {

    private final UndoManager undoManager;
    private CompoundEdit compoundEdit;

    public TE1UndoSupport(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        if (compoundEdit != null) {
            compoundEdit.addEdit(e.getEdit());
        } else {
            undoManager.addEdit(e.getEdit());
        }
    }

    /**
     * 複数の編集動作を1つの Undo 単位でまとめ始める。
     */
    public void beginCompoundEdit() {
        compoundEdit = new CompoundEdit();
    }

    /**
     * まとめた編集動作を1つの Undo 単位として確定する。
     */
    public void endCompoundEdit() {
        if (compoundEdit != null) {
            compoundEdit.end();
            undoManager.addEdit(compoundEdit);
            compoundEdit = null;
        }
    }
}
