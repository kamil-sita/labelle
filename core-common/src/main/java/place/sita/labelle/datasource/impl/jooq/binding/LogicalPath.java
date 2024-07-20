package place.sita.labelle.datasource.impl.jooq.binding;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LogicalPath {

	private final List<String> path = new ArrayList<>();

	public static LogicalPath path(String path) {
		LogicalPath lp = new LogicalPath();
		lp.path.add(path);
		return lp;
	}

	public static LogicalPath path(String path, String... paths) {
		LogicalPath lp = new LogicalPath();
		lp.path.add(path);
		Collections.addAll(lp.path, paths);
		return lp;
	}

	public static LogicalPath path(List<String> path, String pathElement) {
		LogicalPath lp = new LogicalPath();
		lp.path.addAll(path);
		lp.path.add(pathElement);
		return lp;
	}

	public List<String> getPath() {
		return Lists.newArrayList(path);
	}

	@Override
	public String toString() {
		return String.join(".", path);
	}
}
