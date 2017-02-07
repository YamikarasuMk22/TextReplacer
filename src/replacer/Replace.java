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

		//Excel読み込み
		replaceTableList = ReadExcel.readReplaceTable();

		if(replaceTableList == null) {
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

		//文字列置換・ファイル置換
		try {
			String strReadText =null;
			StringBuffer sbWriteText = new StringBuffer();

			FileReader fr;

			fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			while((strReadText = br.readLine()) != null) {

				for(int i=0; i<replaceTableList.size(); i++) {
					ReplaceTable replaceTable = replaceTableList.get(i);
					String strBefore = replaceTable.getSearchStr();
					String strAfter = replaceTable.getReplaceStr();

					result = result + FileUtil.matchCounter(strReadText, strBefore);

					//System.out.println(strBefore + "=>" + strAfter);

					strReadText= strReadText.replaceAll(strBefore,strAfter);
				}
				sbWriteText.append(strReadText);
				sbWriteText.append("\r\n");

			}

			FileWriter fw = new FileWriter(file);
			fw.write(sbWriteText.toString());

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
