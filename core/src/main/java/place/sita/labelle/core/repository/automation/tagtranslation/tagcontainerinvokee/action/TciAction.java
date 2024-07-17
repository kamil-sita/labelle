package place.sita.labelle.core.repository.automation.tagtranslation.tagcontainerinvokee.action;

public sealed interface TciAction permits TciAddTag, TciAddUsingFunction, TciIsInExpressionWrapper, TciJustRemoveTag, TciModifyTag, TciModifyUsingFunction, TciRemoveTag, TciRemoveUsingFunction {
}
