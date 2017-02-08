package replacer;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import javax.swing.TransferHandler;
import javax.swing.UIManager;

public class MainFrameNimbus {

	public JFrame frame;
	public static JTextArea textArea;
	public static JProgressBar progressBar;
	public static String ErrMsg;
	public static List<File> files;
	public static int tmpFileNum = 0;

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {

				MainFrameNimbus window = new MainFrameNimbus();
				window.frame.setVisible(true);
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainFrameNimbus() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame("TextReplacer");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		// テキストエリア
		textArea = new JTextArea(15, 30);
		JScrollPane sp = new JScrollPane(textArea);
		frame.add(sp);

		// プログレスバー
		progressBar = new JProgressBar(0, 100);
		frame.add(BorderLayout.SOUTH, progressBar);

		// ドロップ操作を有効にする
		textArea.setTransferHandler(new DropFileHandler());

		textArea.setText("一括置換するファイルをここにドロップしてください。\n" + "Backupフォルダにバックアップが作成されます。\n");

		int result = ReadExcel.getReplaceTableSize();
		if (result > -1) {
			textArea.append("置換テーブル読込成功:" + result + "行\n");
		} else {
			textArea.append("置換テーブル読込失敗:" + ErrMsg + "\n");
		}

		// ウィンドウ表示
		frame.pack();
		frame.setVisible(true);
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
		 *
		 * @return
		 */
		@SuppressWarnings("unchecked")
		@Override
		public boolean importData(final TransferSupport support) {

			// 受け取っていいものか確認する
			if (!canImport(support)) {
				// textArea.setText("置換不可:置換可能なファイルではありません。");
				return false;
			}

			// ファイルを受け取る
			try {
				// ドロップ処理
				Transferable t = support.getTransferable();

				files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);

			} catch (UnsupportedFlavorException e) {
				textArea.append("	置換失敗(内部エラー)\n" + e);
				textArea.append("[置換終了]--------------------------------------------------------------------------\n");
				e.printStackTrace();
			} catch (IOException e) {
				textArea.append("	置換失敗(内部エラー)\n" + e);
				textArea.append("[置換終了]--------------------------------------------------------------------------\n");
				e.printStackTrace();
			}

			// 実行結果Integer, 処理経過データint[]のSwingWorker
			final SwingWorker<Integer, int[]> runReplace = new SwingWorker<Integer, int[]>() {
				/** バックグラウンド処理 */
				@Override
				protected Integer doInBackground() throws Exception {

					int replaceCount = 0; 	// 置換ファイルカウンタ
					int result = 0;		 	// 置換回数(ファイル毎)

					for (File file : files) {
						replaceCount ++;

						List<ReplaceTable> replaceTableList = new ArrayList<ReplaceTable>();
						List<String> notReplaceList = new ArrayList<String>();

						// 置換テーブル読み込み
						replaceTableList = ReadExcel.readReplaceTable();
						if (replaceTableList == null) {
							result = -1;
							break;
						}

						// 無視リスト読み込み
						notReplaceList = ReadExcel.readNotReplaceList();
						if (notReplaceList == null) {
							result = -1;
							break;
						}

						// バックアップ
						int backUpNumber = 1;
						File backupFile = new File("BackUp\\" + file.getName() + ".BK" + backUpNumber);

						// バックアップファイル上書き回避
						while (backupFile.exists()) {
							backUpNumber++;
							backupFile = new File("BackUp\\" + file.getName() + ".BK" + backUpNumber);
						}

						try {
							FileUtil.copyTargetFile(file, backupFile);
						} catch (IOException e) {
							MainFrame.ErrMsg = e.toString();
							e.printStackTrace();
							result = -1;
							break;
						}

						// 行数取得
						double fileRow = FileUtil.getFileRow(file);

						// 文字列置換・ファイル置換
						try {
							String strReadText = "";
							StringBuffer sbWriteText = new StringBuffer();

							FileReader fr = new FileReader(file);
							BufferedReader br = new BufferedReader(fr);

							System.out.println(replaceTableList.size());

							boolean notReplaceFlag = false;
							double nowRow = 1;

							while ((strReadText = br.readLine()) != null) {

								// 無視リスト処理
								for (int j = 0; j < notReplaceList.size(); j++) {
									if (FileUtil.isMatch(strReadText, notReplaceList.get(j))) {
										notReplaceFlag = true;
									}
								}
								if (notReplaceFlag) {
									notReplaceFlag = false;
									// System.out.println(strReadText);
									sbWriteText.append(strReadText);
									sbWriteText.append("\r\n");

									nowRow = nowRow + 1;
									continue;
								}

								// 置換処理
								for (int i = 0; i < replaceTableList.size(); i++) {

									ReplaceTable replaceTable = replaceTableList.get(i);
									String strBefore = replaceTable.getSearchStr();
									String strAfter = replaceTable.getReplaceStr();

									// System.out.print(i + "," + strBefore +
									// "=>" + strAfter);

									result = result + FileUtil.matchCounter(strReadText, strBefore);

									// System.out.println(":" + result);

									strReadText = strReadText.replaceAll(strBefore, strAfter);

									// System.out.println(strReadText);
								}
								sbWriteText.append(strReadText);
								sbWriteText.append("\r\n");

								//置換中のファイルNo, 処理中の行数, 全体の行数, 置換数
								publish(new int[]{replaceCount, (int)nowRow, (int)fileRow, result });
			                    setProgress((int) (nowRow / fileRow * 100));

			                    //System.out.println(replaceCount + "," + (int)nowRow + "," + (int)fileRow + "," + result);

			                    nowRow = nowRow + 1;
							}

							FileWriter fw = new FileWriter(file);
							fw.write(sbWriteText.toString());

							// System.out.println(strReadText);

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
					}

					return result;
				}

				/** 途中経過の表示 **/
				/** 0:置換中のファイルNo, 1:処理中の行数, 2:全体の行数, 3:置換数 **/
				@Override
				protected void process(List<int[]> chunks) {

					for (int[] values : chunks) {

						//置換の開始
						if(values[0] != tmpFileNum) {

							if(values[0] == 1) {
								textArea.append("[置換開始]--------------------------------------------------------------------------\n");
							}

							File file = files.get(values[0] - 1);

							textArea.append("ファイル" + values[0]);
							textArea.append("	" + file.getPath() + "\n");

							tmpFileNum = values[0];
						}

						//置換の終了
						if(values[1] == values[2]) {
							if (values[3] > -1) {
								textArea.append("	置換成功:" + values[3] + "箇所\n");
							} else {
								textArea.append("	置換失敗:" + ErrMsg + "\n");
							}
						}
					}
				}

				/** 処理終了 */
				@Override
				protected void done() {
					try {
						@SuppressWarnings("unused")
						int result = get();

						textArea.append("[置換終了]--------------------------------------------------------------------------\n");
					} catch (InterruptedException e) {

						e.printStackTrace();
					} catch (ExecutionException e) {

						e.printStackTrace();
					}

					// ログファイル出力
					String logFilePath = Replace.saveLog(textArea);

					if (!logFilePath.equals("")) {
						textArea.append("※ログファイル作成成功:" + logFilePath + "\n");
					} else {
						textArea.append("※ログファイル作成失敗:" + ErrMsg + "\n");
					}
				}
			};
			// プログレスバーの処理
			runReplace.addPropertyChangeListener(new PropertyChangeListener() {
				@Override
				public void propertyChange(PropertyChangeEvent evt) {
					if ("progress".equals(evt.getPropertyName())) {
						progressBar.setValue((Integer) evt.getNewValue());
					}
				}
			});

			runReplace.execute(); // SwingWorkderの実行

			return false;
		}
	}
}