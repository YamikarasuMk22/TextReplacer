package replacer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

public class Replace {

	public static int ReplaceByExcel(File file) {
		int result = 0;	//置換回数

		List<ReplaceTable> replaceTableList = new ArrayList<ReplaceTable>();
		List<String> notReplaceList = new ArrayList<String>();

		//置換テーブル読み込み
		replaceTableList = ReadExcel.readReplaceTable();
		if(replaceTableList == null) {
			result = -1;

			return result;
		}

		//無視リスト読み込み
		notReplaceList = ReadExcel.readNotReplaceList();
		if(notReplaceList == null) {
			result = -1;

			return result;
		}

		//バックアップ
		int backUpNumber = 1;
		File backupFile = new File("BackUp\\" + file.getName() + ".BK" + backUpNumber);

		//バックアップファイル上書き回避
		while(backupFile.exists()) {
			backUpNumber ++;
			backupFile = new File("BackUp\\" + file.getName() + ".BK" + backUpNumber);
		}

		try {
			FileUtil.copyTargetFile(file, backupFile);
        } catch (IOException e) {
        	MainFrame.ErrMsg = e.toString();
            e.printStackTrace();
            result = -1;
			return result;
        }

		//行数取得
//		int fileRow = FileUtil.getFileRow(file);

		//文字列置換・ファイル置換
		try {
			String strReadText = "";
			StringBuffer sbWriteText = new StringBuffer();

			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			System.out.println(replaceTableList.size());

			boolean notReplaceFlag = false;
//			int nowRow = 1;

			//MainFrame.textArea.append("	0%");

			while((strReadText = br.readLine()) != null) {

				//無視リスト処理
				for(int j=0; j<notReplaceList.size(); j++) {
					if(FileUtil.isMatch(strReadText, notReplaceList.get(j))) {
						notReplaceFlag = true;
					}
				}
				if(notReplaceFlag) {
					notReplaceFlag = false;
					System.out.println(strReadText);
					sbWriteText.append(strReadText);
					sbWriteText.append("\r\n");
					continue;
				}

				//置換処理
				for(int i=0; i<replaceTableList.size(); i++) {

					ReplaceTable replaceTable = replaceTableList.get(i);
					String strBefore = replaceTable.getSearchStr();
					String strAfter = replaceTable.getReplaceStr();

					//System.out.print(i + "," + strBefore + "=>" + strAfter);

					result = result + FileUtil.matchCounter(strReadText, strBefore);

					//System.out.println(":" + result);

					strReadText = strReadText.replaceAll(strBefore,strAfter);

					//System.out.println(strReadText);
				}
				sbWriteText.append(strReadText);
				sbWriteText.append("\r\n");

				//進捗率表示
//				for(int p=20; p<100; p=p+20) {
//					if(nowRow/fileRow == p) {
//						MainFrame.textArea.append("..." + p + "%");
//					}
//				}
//				nowRow++;

			}

			//MainFrame.textArea.append("...100%\n");

			FileWriter fw = new FileWriter(file);
			fw.write(sbWriteText.toString());

			//System.out.println(strReadText);

			fw.close();
			fr.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			result = -1;
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			result = -1;
		}

		return result;
	}

	public static String saveLog(JTextArea textArea) {

		Date nowDate = new Date();
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyyMMddHHmmss");

		File logFile = new File("Log\\" + dateTimeFormat.format(nowDate) + ".log");
		String result = "";

		StringBuffer sb = new StringBuffer();

		int linecount = textArea.getLineCount();
		try {

			for (int i = 0; i < linecount; i++) {
				int start = textArea.getLineStartOffset(i);
				int end = textArea.getLineEndOffset(i);
				sb.append(textArea.getText(start, end - start) + "\r\n");
			}

			FileWriter fw = new FileWriter(logFile);
			fw.write(sb.toString());
			fw.close();
			result = logFile.getPath();
		} catch (BadLocationException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			result = "";
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			result = "";
		}

		return result;
	}
}
