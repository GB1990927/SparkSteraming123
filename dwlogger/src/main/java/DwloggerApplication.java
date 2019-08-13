import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("controller")
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
public class DwloggerApplication {

	public static void main(String[] args) {
		SpringApplication.run(DwloggerApplication.class, args);
	}

}
