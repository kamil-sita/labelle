package place.sita.magicscheduler.tasktype;

public sealed interface TaskTypeRef permits HistoricTask, TaskType {

	String code();

	String name();

	boolean isHistoric();

}
