package vn.giabaoblog.giabaoblogserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaAuditing
@EnableJpaRepositories
@SpringBootApplication
//@EntityScan(basePackages = {"vn.giabaoblog.giabaoblogserver"})
public class GiaBaoBlogServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GiaBaoBlogServerApplication.class, args);
	}

}

