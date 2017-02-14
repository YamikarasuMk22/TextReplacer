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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
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

	private static List<File> files;
	private static File backUpFile;
	private static int tmpFileNum = 0;

	public static void main(String[] args) {
		System.setProperty("file.encoding", "UTF-8");

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
		textArea = new JTextArea(12, 40);
		textArea.setLineWrap(true);
		textArea.setEditable(false);

		JScrollPane sp = new JScrollPane(textArea);
		sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		frame.add(sp);

		// プログレスバー
		progressBar = new JProgressBar(0, 100);
		frame.add(BorderLayout.SOUTH, progressBar);

		// ドロップ操作を有効にする
		textArea.setTransferHandler(new DropFileHandler());

		textArea.setText("※一括置換するファイルをここにドロップしてください。\n" + "Backupフォルダにバックアップが作成されます。\n");

		int replaceTableSize = ReadExcel.getReplaceTableSize();
		if (replaceTableSize > -1) {
			textArea.append("[置換テーブルサイズ:" + replaceTableSize + "行]\n");
		} else {
			textArea.append("置換テーブル読込失敗:" + ErrMsg + "\n");
		}
		int notReplaceListSize = ReadExcel.getNotReplaceListSize();
		if (notReplaceListSize > -1) {
			textArea.append("[無視フィルタ数:" + notReplaceListSize + "行]\n");
		} else {
			textArea.append("無視フィルタ読込失敗:" + ErrMsg + "\n");
		}
		int markingListSize = ReadExcel.getMarkingListSize();
		if (markingListSize > -1) {
			textArea.append("[注釈フィルタ数:" + markingListSize + "行]\n");
		} else {
			textArea.append("注釈フィルタ読込失敗:" + ErrMsg + "\n");
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

					int fileCount = 0; 		// 置換ファイルカウンタ
					int result = 0;		 	// 置換回数(ファイル毎)
					int markCount = 0;		// 注釈回数(ファイル毎)
					int notReplaceCount = 0;	// 無視回数(ファイル毎)

					//置換テーブル
					List<ReplaceTable> replaceTableList = new ArrayList<ReplaceTable>();
					//無視リスト
					List<String> notReplaceList = new ArrayList<String>();
					//注釈リスト
					List<String> markingList = new ArrayList<String>();
					//注釈文字列
					String markStr;

					// 置換テーブル読み込み
					replaceTableList = ReadExcel.readReplaceTable();
					if (replaceTableList == null) {
						result = -1;
					}
					// 無視リスト読み込み
					notReplaceList = ReadExcel.readNotReplaceList();
					if (notReplaceList == null) {
						result = -1;
					}
					// 注釈リスト読み込み
					markingList = ReadExcel.readMarkingList();
					if (markingList == null) {
						result = -1;
					}
					// 注釈文字列読み込み
					markStr = ReadExcel.readMarkStr();
					if (markStr.equals("")) {
						result = -1;
					}

					for (File file : files) {
						fileCount ++;

						// バックアップ
						int backUpNumber = 1;
						backUpFile = new File("BackUp\\" + file.getName() + ".BK" + backUpNumber);

						// バックアップファイル上書き回避
						while (backUpFile.exists()) {
							backUpNumber++;
							backUpFile = new File("BackUp\\" + file.getName() + ".BK" + backUpNumber);
						}
						try {
							FileUtil.copyTargetFile(file, backUpFile);
						} catch (IOException e) {
							ErrMsg = e.toString();
							e.printStackTrace();
							result = -1;
							break;
						}

						// 文字列置換・ファイル置換
						try {
							String strReadText = "";
							StringBuffer sbWriteText = new StringBuffer();

							BufferedReader read = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

							System.out.println(replaceTableList.size());

							boolean notReplaceFlag = false;	//無視フラグ
							boolean MarkingFlag = false;		//注釈フラグ
							double nowRow = 1;				//現在の行数

							// 行数取得
							double fileRow = FileUtil.getFileRow(file);

							while ((strReadText = read.readLine()) != null) {

								// 無視リスト処理
								for (int j = 0; j < notReplaceList.size(); j++) {
									if (FileUtil.isMatch(strReadText, notReplaceList.get(j))) {
										notReplaceFlag = true;
									}
								}
								if (notReplaceFlag) {

									strReadText = new String(strReadText.toString().getBytes("UTF-8"), "UTF-8");

									sbWriteText.append(strReadText);
									sbWriteText.append("\r\n");

									//行カウント
									nowRow = nowRow + 1;
									notReplaceCount ++;

									notReplaceFlag = false;

									continue;
								}

								// 注釈リスト処理
								for (int k = 0; k < markingList.size(); k++) {
									if (FileUtil.isMatch(strReadText, markingList.get(k))) {
										MarkingFlag = true;
									}
								}

								// 置換処理
								for (int i = 0; i < replaceTableList.size(); i++) {

									ReplaceTable replaceTable = replaceTableList.get(i);
									String strBefore = replaceTable.getSearchStr();
									String strAfter = replaceTable.getReplaceStr();
									String notReplaceStr = replaceTable.getNotReplaceStr();

									boolean notReplaceStrFlag = false;
									String[] sp = notReplaceStr.split(",", 0);

									if (FileUtil.isMatch(strReadText, strBefore)) {
										// 置換禁止条件処理
										for (int nr = 0; nr < sp.length; nr++) {
											System.out.println(sp[nr]);
											if (FileUtil.isMatch(strReadText, sp[nr])) {
												notReplaceStrFlag = true;
											}
										}

										if(!notReplaceStrFlag) {
											// 置換回数加算
											result = result + FileUtil.matchCounter(strReadText, strBefore);

											// 全ての検索文字列を置換
											strReadText = strReadText.replaceAll(strBefore, strAfter);

											notReplaceStrFlag = false;
										}
									}
								}

								//注釈の場合
								if(MarkingFlag) {
									strReadText = markStr + strReadText;
									markCount ++;
									MarkingFlag = false;
								}

								strReadText = new String(strReadText.toString().getBytes("UTF-8"), "UTF-8");

								sbWriteText.append(strReadText);
								sbWriteText.append("\r\n");

								//置換中のファイルNo, 処理中の行数, 全体の行数, 置換数, 注釈数, 無視行数
								publish(new int[]{
										fileCount, (int)nowRow, (int)fileRow, result, markCount ,notReplaceCount
								});
								setProgress((int) (nowRow / fileRow * 100));

								//行カウント
								nowRow = nowRow + 1;
							}

							PrintWriter write = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));

							write.write(sbWriteText.toString());

							write.close();
							read.close();

						} catch (FileNotFoundException e) {
							e.printStackTrace();
							ErrMsg = e.toString();
							result = -1;
						} catch (IOException e) {
							e.printStackTrace();
							ErrMsg = e.toString();
							result = -1;
						}
					}

					return result;
				}

				/** 途中経過の表示 **/
				/** 0:置換中のファイルNo, 1:処理中の行数, 2:全体の行数, 3:置換数, 4:注釈数, 5:無視行数 **/
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
							textArea.append("	読込成功:" + values[2] + "行\n");

							textArea.append("	バックアップ成功:" + backUpFile.getPath() + "\n");

							tmpFileNum = values[0];
						}

						//置換の終了
						if(values[1] == values[2]) {
							if (values[3] > -1) {
								textArea.append("	[置換成功:" + values[3] + "箇所]\n");
								textArea.append("	[注釈数:" + values[4] + "箇所]\n");
								textArea.append("	[無視された行:" + values[5] + "行]\n");
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
						int result = get();

						if (result > -1) {
							textArea.append("[置換終了]--------------------------------------------------------------------------\n");
						} else {
							textArea.append("	置換失敗:" + ErrMsg + "\n");
							textArea.append("[置換終了]--------------------------------------------------------------------------\n");
						}
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

					//初期化
					tmpFileNum = 0;
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