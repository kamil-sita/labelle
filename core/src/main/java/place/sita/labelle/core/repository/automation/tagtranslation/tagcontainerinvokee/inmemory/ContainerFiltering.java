package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory;

import java.util.Collection;

import place.sita.labelle.core.repository.inrepository.tags.Tag;

public non-sealed interface ContainerFiltering extends TagFiltering {

	boolean filter(Collection<Tag> tags);

}
