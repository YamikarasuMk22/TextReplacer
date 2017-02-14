package replacer;

public class ReplaceTable {

	private int number;				//番号

	private String searchStr;			//検索文字列

	private String replaceStr;			//置換文字列

	private String notReplaceStr;		//置換禁止文字列


	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	public String getSearchStr() {
		return searchStr;
	}

	public void setSearchStr(String searchStr) {
		this.searchStr = searchStr;
	}

	public String getReplaceStr() {
		return replaceStr;
	}

	public void setReplaceStr(String replaceStr) {
		this.replaceStr = replaceStr;
	}

	public String getNotReplaceStr() {
		return notReplaceStr;
	}

	public void setNotReplaceStr(String notReplaceStr) {
		this.notReplaceStr = notReplaceStr;
	}
}
