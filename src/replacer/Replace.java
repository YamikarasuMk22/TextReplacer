package replacer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Replace {

	static int ReplaceByExcel(File file) {
		int result = 0;	//置換回数

		List<ReplaceTable> replaceTableList = new ArrayList<ReplaceTable>();

		//Excel読み込み
		replaceTableList = ReadExcel.readReplaceTable();

		if(replaceTableList == null) {
			result = -1;

			return result;
		}

		//Fileバックアップ
		File backupFile = new File("BackUp\\" + file.getName() + ".backup");
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

					System.out.println(strBefore + "=>" + strAfter);

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
}
