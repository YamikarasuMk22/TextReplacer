package replacer;

import java.util.ArrayList;
import java.util.List;

public class Replace {

	static boolean ReplaceByExcel(String FilePath) {
		boolean result = true;
		List<ReplaceTable> replaceTable = new ArrayList<ReplaceTable>();

		//Excel読み込み
		replaceTable = ReadExcel.ReadReplaceTable();

		if(replaceTable == null) {
			result = false;

			return result;
		}

		//File読み込み

		//置換

		return result;
	}
}
