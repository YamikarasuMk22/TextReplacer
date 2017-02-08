package replacer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ReadExcel {

	/** 変換テーブルサイズ セル位置 **/
	private static final int REPLACE_TABLE_SIZE_ROW = 0;
	private static final int REPLACE_TABLE_SIZE_CELL = 0;

	/** 無視リストサイズ セル位置 **/
	private static final int NOT_REPLACE_LIST_SIZE_ROW = 0;
	private static final int NOT_REPLACE_LIST_SIZE_CELL = 2;

	/** リスト開始位置 **/
	private static final int START_ROW = 2;

	/** Excel File Path **/
	private static final File EXCEL_REPLACE_TABLE = new File("ReplaceTable.xlsx");

	public static int getReplaceTableSize() {
		int result = -1;

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(0);

			//テーブルサイズが書いてあるセル
			XSSFRow row = sheet.getRow(REPLACE_TABLE_SIZE_ROW);
			XSSFCell maxrow = row.getCell(REPLACE_TABLE_SIZE_CELL);

			result = (int) (maxrow.getNumericCellValue());

			wb.close();

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

	public static List<ReplaceTable> readReplaceTable() {
		List<ReplaceTable> replaceTable = new ArrayList<ReplaceTable>();
		String tmpStr = "";		//重複チェック用

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(0);

			XSSFRow row = sheet.getRow(REPLACE_TABLE_SIZE_ROW);
			XSSFCell maxrow = row.getCell(REPLACE_TABLE_SIZE_CELL);

			for(int i = START_ROW; i <= (int)maxrow.getNumericCellValue()+1; i++) {

				row = sheet.getRow(i);
				XSSFCell cell1 = row.getCell(0);	//検索文字列
				XSSFCell cell2 = row.getCell(1);	//置換文字列

				//検索文字列重複列は無視
				if(cell1.getStringCellValue().equals(tmpStr)) {
					continue;
				}

				ReplaceTable cells = new ReplaceTable();
				cells.setNumber(i);
				cells.setSearchStr(cell1.getStringCellValue());
				cells.setReplaceStr(cell2.getStringCellValue());

				replaceTable.add(cells);

				tmpStr = cell1.getStringCellValue();
				//System.out.println(maxrow.getNumericCellValue() + "," + replaceTable.size() + "," + cell1 + cell2);
			}

			wb.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			replaceTable = null;
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			replaceTable = null;
		}

		return replaceTable;
	}

	public static List<String> readNotReplaceList() {
		List<String> notReplaceList = new ArrayList<String>();

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(0);

			XSSFRow row = sheet.getRow(NOT_REPLACE_LIST_SIZE_ROW);
			XSSFCell maxrow = row.getCell(NOT_REPLACE_LIST_SIZE_CELL);

			for(int i = START_ROW; i <= (int)maxrow.getNumericCellValue()+1; i++) {

				row = sheet.getRow(i);
				XSSFCell cell = row.getCell(2);		//無視文字列

				notReplaceList.add(cell.getStringCellValue());
			}

			wb.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			notReplaceList = null;
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			notReplaceList = null;
		}

		return notReplaceList;
	}
}
