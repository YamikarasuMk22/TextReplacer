package replacer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {
	@SuppressWarnings("resource")
	public static void copyTargetFile(File in, File out) throws IOException {
		FileChannel inChannel = new FileInputStream(in).getChannel();
		FileChannel outChannel = new FileOutputStream(out).getChannel();
		try {
			inChannel.transferTo(0, inChannel.size(), outChannel);
		} catch (IOException e) {
			throw e;
		} finally {
			if (inChannel != null)
				inChannel.close();
			if (outChannel != null)
				outChannel.close();
		}
	}

	public static int matchCounter(String str, String searchstr) {

		final String REGEX = searchstr;
		final String INPUT = str;

		Pattern p = Pattern.compile(REGEX, Pattern.LITERAL);
		Matcher m = p.matcher(INPUT);
		int count = 0;

		while (m.find()) {
			count++;
		}

		return count;
	}

	public static boolean isMatch(String str, String searchstr) {

		final String REGEX = searchstr;
		final String INPUT = str;

		Pattern p = Pattern.compile(REGEX, Pattern.LITERAL);
		Matcher m = p.matcher(INPUT);
		boolean count = false;

		while (m.find()) {
			count = true;
		}

		return count;
	}

	public static int getFileRow(File file) {

		int count = 0;

		try {
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);

			while((br.readLine()) != null) {
				count ++;
			}

			br.close();
			fr.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return count;
	}
}
