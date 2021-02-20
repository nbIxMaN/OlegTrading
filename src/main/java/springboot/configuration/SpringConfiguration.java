package springboot.configuration;

import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.okhttp.OkHttpOpenApi;

import java.time.Duration;

@Configuration
@ComponentScan(basePackages = "springboot")
@PropertySource("classpath:tokens.properties")
@PropertySource("classpath:config.properties")
public class SpringConfiguration {

    @Value("${tinkoff.token}")
    private String tinkoffToken;

    @Bean
    public OpenApi openApi() {
        return new OkHttpOpenApi(
                tinkoffToken, false
        );
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .pingInterval(Duration.ofSeconds(5))
                .build();
    }
}
