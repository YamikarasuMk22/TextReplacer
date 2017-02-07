package replacer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;

public class MainFrame {

	public static String ErrMsg;
	public static JFrame frame;
	public static JTextArea textArea;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@SuppressWarnings("static-access")
			public void run() {
				try {
					MainFrame window = new MainFrame();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainFrame() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame = new JFrame("TextReplacer");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		int result;
		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setEditable(false);

		scrollPane.setViewportView(textArea);
		textArea.setText( "一括置換するファイルをここにドロップしてください。\n"
						+ "Backupフォルダにバックアップが作成されます。\n" );

		result = ReadExcel.getReplaceTableSize();
		if(result > -1) {
			textArea.append("置換テーブル読込成功:" + result + "行\n");
		} else {
			textArea.append("置換テーブル読込失敗:" + ErrMsg + "\n");
		}

		// ドロップ操作を有効にする
		textArea.setTransferHandler(new DropFileHandler());
	}

	/**
	 * ドロップ操作の処理を行うクラス
	 */
	private class DropFileHandler extends TransferHandler {

		/**
		 * ドロップされたものを受け取るか判断 (ファイルのときだけ受け取る)
		 */
		@Override
		public boolean canImport(TransferSupport support) {
			if (!support.isDrop()) {
				// ドロップ操作でない場合は受け取らない
				return false;
			}

			if (!support.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				// ドロップされたのがファイルでない場合は受け取らない
				return false;
			}

			return true;
		}

		/**
		 * ドロップされたファイルを受け取る
		 */
		@Override
		public boolean importData(TransferSupport support) {
			// 受け取っていいものか確認する
			if (!canImport(support)) {
				textArea.setText("置換不可:置換可能なファイルではありません。");
				return false;
			}

			// ドロップ処理
			Transferable t = support.getTransferable();
			try {
				// ファイルを受け取る
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

				int replaceCount = 0;		//置換ファイルカウンタ
				int result = 0;			//置換回数(ファイル毎)
				String logFilePath = "";

				textArea.append("[置換開始]--------------------------------------------------------------------------\n");

				for (File file : files) {
					replaceCount ++;

					textArea.append("ファイル" + replaceCount);
					textArea.append("	" + file.getPath() + "\n");

					result = Replace.ReplaceByExcel(file);

					if(result > -1) {
						textArea.append("	置換成功:" + result + "箇所\n");
					} else {
						textArea.append("	置換失敗:" + ErrMsg + "\n");
					}
				}

				textArea.append("[置換終了]--------------------------------------------------------------------------\n");

				//ログファイル出力
				logFilePath = Replace.saveLog(textArea);

				if(!logFilePath.equals("")) {
					textArea.append("※ログファイル作成成功:" + logFilePath + "\n");
				} else {
					textArea.append("※ログファイル作成失敗:" + ErrMsg + "\n");
				}

			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
				textArea.append("	置換失敗(内部エラー)\n" + e);
				textArea.append("[置換終了]--------------------------------------------------------------------------\n");
			}
			return true;
		}
	}
}
