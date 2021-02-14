package springboot.controllers;

import okhttp3.Response;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.Operations;
import ru.tinkoff.invest.openapi.model.rest.Portfolio;
import springboot.logic.ProfitCalculator;
import springboot.openApi.figi.classes.Figi;
import springboot.openApi.figi.classes.FigiIdType;
import springboot.openApi.figi.classes.Job;
import springboot.openApiConnection.OpenApiFigiConnection;
import springboot.openApiConnection.OpenApiTinkoffConnection;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

@RestController
public class HelloController {

    OpenApiTinkoffConnection openApiTinkoffConnection;
    ProfitCalculator profitCalculator;
    OpenApiFigiConnection openApiFigiConnection;

    public HelloController(OpenApiTinkoffConnection openApiTinkoffConnection,
                           ProfitCalculator profitCalculator,
                           OpenApiFigiConnection openApiFigiConnection){
        this.openApiTinkoffConnection = openApiTinkoffConnection;
        this.profitCalculator = profitCalculator;
        this.openApiFigiConnection = openApiFigiConnection;
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
        return profitCalculator.calculateProfit(OffsetDateTime.parse(begin), OffsetDateTime.parse(end));
    }

    @RequestMapping("/portfolio")
    public Portfolio portfolio() {
        return openApiTinkoffConnection.getPortfolio();
    }

    @RequestMapping("/figi")
    public List<Figi> figi() throws IOException {
        return openApiFigiConnection.mapJobs(Collections.singletonList(Job.builder().
                idType(FigiIdType.ID_BB_GLOBAL).
                idValue("BBG000BR2B91").
                build())).join();
    }

}