package place.sita.labelle;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import place.sita.labelle.gui.local.GuiConfig;

@SpringBootTest(classes = GuiConfig.class)
public class TestContainersTest {
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>( DockerImageName.parse("postgres").withTag("13.14-bullseye"));


    static {
        postgres.start();
    }

    @DynamicPropertySource
    private static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }
}
