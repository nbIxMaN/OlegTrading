package springboot.controllers;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.Operations;
import ru.tinkoff.invest.openapi.model.rest.Portfolio;
import springboot.database.connection.dao.InstrumentDescription;
import springboot.openFigi.FigiDataSource;
//import springboot.openFigi.FigiToFullInstrumentDescriptionResolver;
//import springboot.logic.ProfitCalculator;
import springboot.openFigi.OpenApiFigiDataSource;
import springboot.openApiConnection.OpenApiTinkoffConnection;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
public class HelloController {

    OpenApiTinkoffConnection openApiTinkoffConnection;
//    ProfitCalculator profitCalculator;
    OpenApiFigiDataSource openApiFigiDataSource;
    FigiDataSource figiDataSource;

    public HelloController(OpenApiTinkoffConnection openApiTinkoffConnection,
//                           ProfitCalculator profitCalculator,
                           OpenApiFigiDataSource openApiFigiDataSource,
                           FigiDataSource figiDataSource){
        this.openApiTinkoffConnection = openApiTinkoffConnection;
//        this.profitCalculator = profitCalculator;
        this.openApiFigiDataSource = openApiFigiDataSource;
        this.figiDataSource = figiDataSource;
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
        return null;
//        return profitCalculator.getTradingOperations(OffsetDateTime.parse(begin), OffsetDateTime.parse(end));
    }

    @RequestMapping("/portfolio")
    public Portfolio portfolio() {
        return openApiTinkoffConnection.getPortfolio();
    }

    @RequestMapping("/figi")
    public Collection<InstrumentDescription> figi() {
        return figiDataSource.getInstrumentDescriptionsByFigi(Collections.singletonList("BBG000BR2B91"));
    }

}