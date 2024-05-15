package place.sita.magicscheduler.scheduler;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import place.sita.magicscheduler.TaskTypeRepository;

@Component
public class TypeSpecificQueueFactory {
    private static final Logger log = LoggerFactory.getLogger(TypeSpecificQueueFactory.class);

    private final ApplicationContext applicationContext;
    private final TaskTypeRepository taskTypeRepository;

    public TypeSpecificQueueFactory(ApplicationContext applicationContext, TaskTypeRepository taskTypeRepository) {
        this.applicationContext = applicationContext;
        this.taskTypeRepository = taskTypeRepository;
    }

    @PostConstruct
    public void createBeans() {
        for (var type : taskTypeRepository.all()) {
            TypeSpecificQueue typeSpecificQueue = applicationContext.getBean(TypeSpecificQueue.class);
            typeSpecificQueue.setType(type);
            log.info("Registering a TypeSpecificQueue for {}", type.code());
            typeSpecificQueue.scheduleFirstExecution();
        }
    }

}
