package cz.cvut.fel.berloga;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;


@SpringBootApplication
@EnableCaching
public class BerlogaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BerlogaApplication.class, args);
	}

}
