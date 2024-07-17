package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.inmemory;

import place.sita.labelle.core.repository.inrepository.tags.Tag;

public non-sealed interface TagFilter extends TagFiltering {

	boolean filter(Tag tag);

}
