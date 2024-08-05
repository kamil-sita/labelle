package place.sita.labelle.core.repository.inrepository.tags;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "place.sita.imageapi.tags")
public class TagRepositoryProperties {

	private int tagBulkSize = 100;
	private int imageBulkSize = 20;

	public int getTagBulkSize() {
		return tagBulkSize;
	}

	public void setTagBulkSize(int tagBulkSize) {
		this.tagBulkSize = tagBulkSize;
	}

	public int getImageBulkSize() {
		return imageBulkSize;
	}

	public void setImageBulkSize(int imageBulkSize) {
		this.imageBulkSize = imageBulkSize;
	}
}
