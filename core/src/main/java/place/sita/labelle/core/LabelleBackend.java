package place.sita.labelle.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@Import(CoreAppConfig.class)
public class LabelleBackend {

    public static void main(String[] args) {
        // start spring

        SpringApplication.run(LabelleBackend.class, args);
    }

}
