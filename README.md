# TextEditor

Swingで開発したシンプルなテキストエディタです。  
基本的な編集機能に加え、検索・置換やUndo/Redoを実装しています。

設計面では、MVC志向をベースにController分割やService層の導入を行い、  
責務分離を意識して段階的に改善しました。

---

## ■ 主な機能

- ファイル操作（新規・開く・保存・名前を付けて保存）
- Undo / Redo
- 検索 / 置換（すべて置換は1回のUndoに対応）
- 行番号表示・ステータスバー
- 未保存変更の検知（タイトルに * 表示）

---

## ■ 技術的なポイント

- MVC志向の構成
- Controller分割による責務整理
- Service層による処理の分離
- Modelの状態管理と通知（Observer）
- EventHandlerによるイベント処理の分離
- Undo機構の実装（CompoundEdit）

---

## ■ パッケージ構成

```text
EditorMain              // アプリケーションのエントリーポイント

controller
├ EditorController      // アプリケーションの司令塔
├ EditorEventHandler    // Document・Caret イベントの検知
├ FileController        // ファイル操作の制御
├ SearchController      // 検索・置換の制御
└ UndoController        // Undo・Redoの制御

view
├ EditorView            // メイン画面描画
└ SearchReplaceDialog   // 検索・置換ダイアログ

model
└ EditorModel           // 状態管理（現在ファイル、未保存）

service
├ FileService           // ファイルIOロジック
├ SearchHandler         // 検索・置換IF
├ SearchService         // 検索・置換ロジック
└ UndoSupport           // Undo・Redo履歴の管理
```

---

## ■ クラス図（ファイル操作）

```mermaid
classDiagram
    class EditorController
    class FileController
    class FileService
    class EditorView
    class EditorModel
    class UndoController

    EditorController --> FileController : delegates
    FileController --> EditorView : uses
    FileController --> EditorModel : updates
    FileController --> FileService : delegates
    FileController --> UndoController : uses

    EditorModel --> EditorController : notifies
```

---

## ■ クラス図（検索・置換）

```mermaid
classDiagram
    class EditorController
    class SearchController
    class SearchReplaceDialog
    class SearchHandler
    class SearchService
    class EditorView
    class UndoSupport

    EditorController --> SearchController : delegates
    SearchController --> EditorView : uses
    SearchController --> SearchReplaceDialog : manages
    SearchController --> SearchHandler : delegates

    SearchService ..|> SearchHandler
    SearchService --> UndoSupport : uses
    SearchReplaceDialog --> SearchHandler : delegates
```

---

## ■ 状態通知

```mermaid
classDiagram
    class EditorModel
    class ModelListener
    class EditorController
    class EditorView

    EditorController ..|> ModelListener
    EditorModel --> ModelListener : notifies
    EditorController --> EditorView : updates
```

---

## ■ 処理フロー（ファイル読み込み）

```mermaid
sequenceDiagram
    participant User
    participant View
    participant Controller
    participant FileController
    participant FileService
    participant Model

    User->>View: 開く
    View->>Controller: イベント通知
    Controller->>FileController: openFile()
    FileController->>FileService: readFile()
    FileController->>View: setText()
    FileController->>Model: 状態更新
    Model->>Controller: 通知
    Controller->>View: UI更新
```

---

## ■ 処理フロー（検索・置換）

```mermaid
sequenceDiagram
    participant User
    participant View
    participant Controller
    participant SearchController
    participant Dialog as SearchReplaceDialog
    participant SearchService

    User->>View: 検索 / 置換を実行
    View->>Controller: イベント通知
    Controller->>SearchController: showSearchReplaceDialog()
    SearchController->>Dialog: ダイアログ表示

    User->>Dialog: 検索語を入力
    Dialog->>SearchService: findText() / replaceText()
    SearchService->>View: 選択範囲更新 / フォーカス制御
```

---

## ■ 今後の改善
- 正規表現で検索
- 大文字小文字を無視した検索
- 設計のさらなる整理 (MVC + Service、クラス名やより細かい責務分離など)

---

## ■ 学習ポイント
- MVCに加えた責務分離の重要性
- 状態管理とUI更新の分離
- Swingの内部仕様（Documentの挙動）
- Undoの仕組みと設計

