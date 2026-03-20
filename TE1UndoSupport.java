import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;

/**
 * Undo / Redo 用の編集履歴を管理するクラス。
 *
 * 通常の編集は UndoManager へそのまま追加し、
 * 「すべて置換」のように複数回の編集を1回の Undo にまとめたい場合は
 * CompoundEdit を利用して一括管理する。
 */
public class TE1UndoSupport implements UndoableEditListener {

    /** Undo / Redo の履歴本体 */
    private final UndoManager undoManager;

    /** 複数編集をまとめるための一時的な入れ物 */
    private CompoundEdit compoundEdit;

    /**
     * Undo 補助クラスを初期化する。
     *
     * @param undoManager Undo / Redo の履歴本体
     */
    public TE1UndoSupport(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    /**
     * 編集が発生したときに呼ばれる。
     *
     * CompoundEdit が開始中ならそこへ追加し、
     * そうでなければ通常の Undo 履歴として UndoManager へ追加する。
     *
     * @param e Undo 可能な編集イベント
     */
    @Override
    public void undoableEditHappened(UndoableEditEvent e) {
        if (compoundEdit != null) {
            // まとめ編集中は、個々の編集を CompoundEdit にためる。
            compoundEdit.addEdit(e.getEdit());
        } else {
            // 通常時は、そのまま UndoManager に追加する。
            undoManager.addEdit(e.getEdit());
        }
    }

    /**
     * 複数の編集動作を1つの Undo 単位としてまとめ始める。
     */
    public void beginCompoundEdit() {
        compoundEdit = new CompoundEdit();
    }

    /**
     * まとめた編集動作を1つの Undo 単位として確定する。
     */
    public void endCompoundEdit() {
        if (compoundEdit != null) {
            // CompoundEdit は end() を呼んで確定してから
            // UndoManager に渡す必要がある。
            compoundEdit.end();
            undoManager.addEdit(compoundEdit);
            compoundEdit = null;
        }
    }
}