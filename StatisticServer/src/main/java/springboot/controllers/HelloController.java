package springboot.controllers;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tinkoff.invest.openapi.model.rest.Operations;
import springboot.openApiConnection.OpenApiConnection;

import java.time.OffsetDateTime;

@RestController
public class HelloController {

    OpenApiConnection openApiConnection;

    public HelloController(OpenApiConnection openApiConnection){
        this.openApiConnection = openApiConnection;
    }

    @RequestMapping("/operations")
    public Operations operations(
            @RequestParam(name="begin", required=true) String begin,
            @RequestParam(name="end", required=true) String  end
    ) {
        return openApiConnection.getOperations(OffsetDateTime.parse(begin), OffsetDateTime.parse(end));
    }

}