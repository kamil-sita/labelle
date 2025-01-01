package place.sita.labelle.core.tasks;

import java.io.File;
import java.util.List;

public class Utils {
	static void findAllFilesInDir(File dir, List<File> fileList) {
		File[] files = dir.listFiles();
		if (files != null) {
			for (File file : files) {
				if (file.isFile()) {
					if (file.getPath().toLowerCase().endsWith("thumbs.db")) {
						continue;
					}
					fileList.add(file);
				} else if (file.isDirectory()) {
					findAllFilesInDir(file, fileList);
				}
			}
		}
	}
}
