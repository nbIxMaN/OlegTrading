package springboot.controllers;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.Operations;
import ru.tinkoff.invest.openapi.model.rest.Portfolio;
import springboot.logic.ProfitCalculator;
import springboot.openApiConnection.OpenApiConnection;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
public class HelloController {

    OpenApiConnection openApiConnection;
    ProfitCalculator profitCalculator;

    public HelloController(OpenApiConnection openApiConnection,
                           ProfitCalculator profitCalculator){
        this.openApiConnection = openApiConnection;
        this.profitCalculator = profitCalculator;
    }

    @RequestMapping("/operations")
    public Operations operations(
            @RequestParam(name="begin", required=true) String begin,
            @RequestParam(name="end", required=true) String  end
    ) {
        return openApiConnection.getOperations(OffsetDateTime.parse(begin), OffsetDateTime.parse(end));
    }

    @RequestMapping("/tradingOperations")
    public List<Operation> tradingOperations(
            @RequestParam(name="begin", required=true) String begin,
            @RequestParam(name="end", required=true) String  end
    ) {
        return profitCalculator.calculateProfit(OffsetDateTime.parse(begin), OffsetDateTime.parse(end));
    }

    @RequestMapping("/portfolio")
    public Portfolio portfolio() {
        return openApiConnection.getPortfolio();
    }

}