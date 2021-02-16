package springboot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import springboot.configuration.SpringConfiguration;

@SpringBootApplication
@Import({SpringConfiguration.class})
public class Token {

    Logger logger = LoggerFactory.getLogger(Token.class);

    public static void main(String[] args) {
        SpringApplication.run(Token.class, args);
    }

}
