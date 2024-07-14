package place.sita.labelle.core.repository.inrepository.tags.tagcontainerinvokee;

import java.util.List;

/**
 * Represents an abstract tag container on which operations can be performed.
 *
 * This abstraction is written this way because of an assumption, that the operation of changes to tag
 * might be performed on many layers of the application - be it in memory, or in the database - and this
 * abstraction allows to perform these operations in a uniform way.
 */
public interface TagContainerInvokee {

	void applyInstructions(List<TciInstruction> instructions);

	default void applyInstructions(String expression) {
		applyInstructions(TfLangChangeExpressionToTciInstructionConverter.convert(expression));
	}

}
