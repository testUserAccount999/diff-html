# diff-html
現新比較テストで使用するHTML比較ツール。<br />
現行アプリで出力されたHTMLと新アプリで出力されたHTMLを比較する。<br />
GoogleChromeで保存(Ctrl+S)したHTMLを想定。<br />


## ビルド方法
    mvn package

## 実行方法
    java -Xmx512m -jar diff-html-jar-with-dependencies.jar "OLD_DIR" "NEW_DIR"
OLD_DIR : 現アプリで出力されたHTMLが格納されたディレクトリ

NEW_DIR : 新アプリで出力されたHTMLが格納されたディレクトリ

## memo
- [jsoup](https://jsoup.org/) への依存あり
- 現新のHTMLで改行、空行、属性順序などの差分が発生するため、HTMLをJsoupで一度パースしてから比較を実施する。
- StrutsのTOKENの差分は無視する
- テキストに日付が入っている場合は無視する。
