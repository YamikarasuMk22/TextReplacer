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
	private JFrame frame;
	private JTextArea textArea;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
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
		frame = new JFrame("Replace By Excel");
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JScrollPane scrollPane = new JScrollPane();
		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		textArea = new JTextArea();
		scrollPane.setViewportView(textArea);
		textArea.setText( "一括置換するファイルをここにドロップしてください。\n"
						+ "Backupフォルダにバックアップが作成されます。\n" );

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

				// テキストエリアにステータスリストを作成する
				StringBuffer statusList = new StringBuffer();
				int replaceCount = 0;
				boolean result = true;

				for (File file : files) {
					statusList.append(file.getPath());
					statusList.append("\n");

					result = Replace.ReplaceByExcel(file.getPath());

					if(result) {
						statusList.append("置換成功:" + replaceCount + "箇所\n");
					} else {
						statusList.append("置換失敗:" + ErrMsg + "\n");
					}
					// テキストエリアにステータスリストを表示する
					textArea.setText(statusList.toString());
				}
			} catch (UnsupportedFlavorException | IOException e) {
				e.printStackTrace();
				textArea.setText("置換失敗(内部エラー)\n" + e);
			}
			return true;
		}
	}
}
