package SecureMiddleware;



import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;

@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class})
public class SecuremiddlewareApplication {

	public static void main(String[] args) {
		SpringApplication.run(SecuremiddlewareApplication.class, args);
	}

}
