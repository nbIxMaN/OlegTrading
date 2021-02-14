package springboot.openApiConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.Operations;
import ru.tinkoff.invest.openapi.model.rest.Portfolio;
import ru.tinkoff.invest.openapi.model.rest.UserAccount;

import java.time.OffsetDateTime;
import java.util.concurrent.CompletableFuture;


@Component
@Scope("prototype")
public class OpenApiConnection implements AutoCloseable{

    Logger logger = LoggerFactory.getLogger(OpenApiConnection.class);

    private final OpenApi openApi;

    @Autowired
    public OpenApiConnection(OpenApi openApi){
        this.openApi = openApi;
    }

    public Operations getOperations(OffsetDateTime begin, OffsetDateTime end) {
        UserAccount accounts = openApi.getUserContext().getAccounts().join().getAccounts().get(0);
        Operations operations = openApi.getOperationsContext().getOperations(
                begin,
                end,
                null,
                accounts.getBrokerAccountId()
        ).join();
        return operations;
    }

    public Portfolio getPortfolio() {
        UserAccount accounts = openApi.getUserContext().getAccounts().join().getAccounts().get(0);
        Portfolio portfolio = openApi.getPortfolioContext().getPortfolio(accounts.getBrokerAccountId()).join();
        return portfolio;
    }

    @Override
    public void close() throws Exception {
        openApi.close();
    }
}
