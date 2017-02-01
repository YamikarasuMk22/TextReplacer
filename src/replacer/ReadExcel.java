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

	/** Excel File Path **/
	private static final File EXCEL_REPLACE_TABLE = new File("ReplaceTable.xlsx");

	public static int getReplaceTableSize() {
		int result = -1;

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(0);

			XSSFRow row = sheet.getRow(0);
			XSSFCell maxrow = row.getCell(2);

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

		try {
			XSSFWorkbook wb = new XSSFWorkbook(new FileInputStream(EXCEL_REPLACE_TABLE));
			XSSFSheet sheet = wb.getSheetAt(0);

			XSSFRow row = sheet.getRow(0);
			XSSFCell maxrow = row.getCell(2);

			for(int i=1; i<=maxrow.getNumericCellValue(); i++) {

				row = sheet.getRow(i);
				XSSFCell cell1 = row.getCell(0);	//検索文字列
				XSSFCell cell2 = row.getCell(1);	//置換文字列

				ReplaceTable cells = new ReplaceTable();
				cells.setNumber(i);
				cells.setSearchStr(cell1.getStringCellValue());
				cells.setReplaceStr(cell2.getStringCellValue());

				replaceTable.add(cells);

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
}
