package springboot.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.Operations;
import ru.tinkoff.invest.openapi.model.rest.Portfolio;
import springboot.classes.CalculationProfit;
import springboot.database.connection.dao.InstrumentDescription;
import springboot.logic.FigiToFullInstrumentDescriptionResolver;
import springboot.logic.ProfitCalculator;
import springboot.openApiConnection.OpenApiFigiConnection;
import springboot.openApiConnection.OpenApiTinkoffConnection;
import springboot.openApiConnection.classes.FullInstrumentDescription;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    OpenApiTinkoffConnection openApiTinkoffConnection;
    ProfitCalculator profitCalculator;
    OpenApiFigiConnection openApiFigiConnection;
    FigiToFullInstrumentDescriptionResolver figiToFullInstrumentDescriptionResolver;

    public HelloController(OpenApiTinkoffConnection openApiTinkoffConnection,
                           ProfitCalculator profitCalculator,
                           OpenApiFigiConnection openApiFigiConnection,
                           FigiToFullInstrumentDescriptionResolver figiToFullInstrumentDescriptionResolver){
        this.openApiTinkoffConnection = openApiTinkoffConnection;
        this.profitCalculator = profitCalculator;
        this.openApiFigiConnection = openApiFigiConnection;
        this.figiToFullInstrumentDescriptionResolver = figiToFullInstrumentDescriptionResolver;
    }

    @RequestMapping("/operations")
    public Operations operations(
            @RequestParam(name="begin", required=true) String begin,
            @RequestParam(name="end", required=true) String  end
    ) {
        return openApiTinkoffConnection.getOperations(OffsetDateTime.parse(begin), OffsetDateTime.parse(end));
    }

    @RequestMapping("/tradingOperations")
    public List<Operation> tradingOperations(
            @RequestParam(name="begin", required=true) String begin,
            @RequestParam(name="end", required=true) String  end
    ) {
        return profitCalculator.getTradingOperations(OffsetDateTime.parse(begin), OffsetDateTime.parse(end));
    }

    @RequestMapping("/profit")
    public List<CalculationProfit> profit(
            @RequestParam(name="begin", required=true) String begin,
            @RequestParam(name="end", required=true) String  end
    ) {
        return profitCalculator.calculateProfit(OffsetDateTime.parse(begin), OffsetDateTime.parse(end)).join();
    }

    @RequestMapping("/portfolio")
    public Portfolio portfolio() {
        return openApiTinkoffConnection.getPortfolio();
    }

    @RequestMapping("/figi")
    public Map<String, InstrumentDescription> figi() {
        return figiToFullInstrumentDescriptionResolver.getFullInstrumentDescriptionsByFigi(Collections.singletonList("BBG000BR2B91")).join();
    }

}