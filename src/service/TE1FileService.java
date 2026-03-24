package service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * ファイルの読み書きを担当する Service クラス。
 *
 * このクラスは純粋なファイルI/Oのみを扱い、
 * UIやControllerの処理は持たない。
 */
public class TE1FileService {

    /**
     * 指定したファイルの内容を文字列として読み込む。
     *
     * @param file 読み込むファイル
     * @return ファイルの内容
     * @throws IOException 読み込みに失敗した場合
     */
    public String readFile(File file) throws IOException {
        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append(System.lineSeparator());
            }
        }

        return sb.toString();
    }

    /**
     * 指定したファイルへ文字列を書き込む。
     *
     * @param file    書き込み先ファイル
     * @param content 書き込む内容
     * @throws IOException 書き込みに失敗した場合
     */
    public void writeFile(File file, String content) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.write(content);
        }
    }
}