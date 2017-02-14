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

	/** 置換テーブルシート **/
	private static final int REPLACE_TABLE_SHEET = 0;

	/** セッティングシート **/
	private static final int SETTINGS_SHEET = 1;

	/** 置換テーブルサイズ セル位置 **/
	private static final int REPLACE_TABLE_SIZE_ROW = 0;
	private static final int REPLACE_TABLE_SIZE_CELL = 0;

	/** 無視リストサイズ セル位置 **/
	private static final int NOT_REPLACE_LIST_SIZE_ROW = 0;
	private static final int NOT_REPLACE_LIST_SIZE_CELL = 0;

	/** 無視リスト列 **/
	private static final int NOT_REPLACE_LIST_CELL = 0;

	/** 注釈定義 セル位置 **/
	private static final int MARK_STR_ROW = 2;
	private static final int MARK_STR_CELL = 2;

	/** 注釈リストサイズ セル位置 **/
	private static final int MARKING_LIST_SIZE_ROW = 0;
	private static final int MARKING_LIST_SIZE_CELL = 1;

	/** 注釈リスト列 **/
	private static final int MARKING_LIST_CELL = 1;

	/** リスト開始位置 **/
	private static final int START_ROW = 2;

	/** Excel File Path **/
	private static final File EXCEL_REPLACE_TABLE = new File("ReplaceTable.xlsx");

	public static int getReplaceTableSize() {
		int result = -1;

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(REPLACE_TABLE_SHEET);

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

	public static int getNotReplaceListSize() {
		int result = -1;

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(SETTINGS_SHEET);

			XSSFRow row = sheet.getRow(NOT_REPLACE_LIST_SIZE_ROW);
			XSSFCell maxrow = row.getCell(NOT_REPLACE_LIST_SIZE_CELL);

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

	public static int getMarkingListSize() {
		int result = -1;

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(SETTINGS_SHEET);

			XSSFRow row = sheet.getRow(MARKING_LIST_SIZE_ROW);
			XSSFCell maxrow = row.getCell(MARKING_LIST_SIZE_CELL);

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
			XSSFSheet sheet = wb.getSheetAt(REPLACE_TABLE_SHEET);

			XSSFRow row = sheet.getRow(REPLACE_TABLE_SIZE_ROW);
			XSSFCell maxrow = row.getCell(REPLACE_TABLE_SIZE_CELL);

			String cell1utf8 = "";
			String cell2utf8 = "";
			String cell3utf8 = "";

			for(int i = START_ROW; i < (int)maxrow.getNumericCellValue() + START_ROW; i++) {

				row = sheet.getRow(i);
				XSSFCell cell1 = row.getCell(0);	//検索文字列
				XSSFCell cell2 = row.getCell(1);	//置換文字列
				XSSFCell cell3 = row.getCell(2);	//置換禁止条件

				//検索文字列重複列は無視
				if(cell1.getStringCellValue().equals(tmpStr)) {
					continue;
				}

				cell1utf8 = new String(cell1.getStringCellValue().getBytes("UTF-8"), "UTF-8");
				cell2utf8 = new String(cell2.getStringCellValue().getBytes("UTF-8"), "UTF-8");
				cell3utf8 = new String(cell3.getStringCellValue().getBytes("UTF-8"), "UTF-8");

				cell3utf8 = cell3utf8.replaceAll("!", "");
				System.out.print(cell3utf8);

				ReplaceTable cells = new ReplaceTable();
				cells.setNumber(i);
				cells.setSearchStr(cell1utf8);
				cells.setReplaceStr(cell2utf8);
				cells.setNotReplaceStr(cell3utf8);

				replaceTable.add(cells);

				tmpStr = cell1.getStringCellValue();
				//System.out.println(maxrow.getNumericCellValue() + "," + i + "," + replaceTable.size() + "," + cell1 + cell2);
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
			XSSFSheet sheet = wb.getSheetAt(SETTINGS_SHEET);

			XSSFRow row = sheet.getRow(NOT_REPLACE_LIST_SIZE_ROW);
			XSSFCell maxrow = row.getCell(NOT_REPLACE_LIST_SIZE_CELL);

			String cell1utf8;

			for(int i = START_ROW; i < (int)maxrow.getNumericCellValue() + START_ROW; i++) {
				row = sheet.getRow(i);

				//無視対象文字列
				XSSFCell cell = row.getCell(NOT_REPLACE_LIST_CELL);

				cell1utf8 = new String(cell.getStringCellValue().getBytes("UTF-8"), "UTF-8");

				notReplaceList.add(cell1utf8);
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

	public static List<String> readMarkingList() {
		List<String> markingList = new ArrayList<String>();

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(SETTINGS_SHEET);

			XSSFRow row = sheet.getRow(MARKING_LIST_SIZE_ROW);
			XSSFCell maxrow = row.getCell(MARKING_LIST_SIZE_CELL);

			String cell1utf8;

			for(int i = START_ROW; i < (int)maxrow.getNumericCellValue() + START_ROW; i++) {
				row = sheet.getRow(i);

				//注釈対象文字列
				XSSFCell cell = row.getCell(MARKING_LIST_CELL);

				cell1utf8 = new String(cell.getStringCellValue().getBytes("UTF-8"), "UTF-8");

				markingList.add(cell1utf8);
			}

			wb.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			markingList = null;
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			markingList = null;
		}

		return markingList;
	}

	public static String readMarkStr() {
		String markStr = "";

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(SETTINGS_SHEET);

			XSSFRow row = sheet.getRow(MARK_STR_ROW);
			XSSFCell cell = row.getCell(MARK_STR_CELL);

			markStr = new String(cell.getStringCellValue().getBytes("UTF-8"), "UTF-8");

			wb.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			markStr = "";
		} catch (IOException e) {
			e.printStackTrace();
			MainFrame.ErrMsg = e.toString();
			markStr = "";
		}

		return markStr;
	}
}
