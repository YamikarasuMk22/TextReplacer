package replacer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
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

}
