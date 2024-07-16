package place.sita.magicscheduler.tasktype;

public sealed interface TaskTypeRef permits HistoricTask, TaskType {

	String code();

	String name();

	/**
	 * A task is historic if there are references to it, but we do not know how to run it, which suggests
	 * that someone did run such a task in the past, but there no longer is a code reference to it.
	 */
	boolean isHistoric();

}
