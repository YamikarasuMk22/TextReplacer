package replacer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Replace {

	static int ReplaceByExcel(File file) {
		int result = 0;	//置換回数

		List<ReplaceTable> replaceTable = new ArrayList<ReplaceTable>();

		//Excel読み込み
		replaceTable = ReadExcel.readReplaceTable();

		if(replaceTable == null) {
			result = -1;

			return result;
		}

		//File読み込み


		//Fileバックアップ
		File backupFile = new File("BackUp\\" + file.getName() + ".backup");
		try {
			FileUtil.copyTargetFile(file, backupFile);
        } catch (IOException e) {
            e.printStackTrace();
        }


		//置換

		return result;
	}
}
