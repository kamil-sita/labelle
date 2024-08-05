package place.sita.labelle.core.repository.inrepository.image.replication;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "place.sita.imageapi.images")
public class ImageReplicationProperties {

	private int tagBulkSize = 100;

	public int getTagBulkSize() {
		return tagBulkSize;
	}

	public void setTagBulkSize(int tagBulkSize) {
		this.tagBulkSize = tagBulkSize;
	}
}
