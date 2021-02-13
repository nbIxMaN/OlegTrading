package springboot.openApiConnection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import ru.tinkoff.invest.openapi.OpenApi;
import ru.tinkoff.invest.openapi.model.rest.Operations;
import ru.tinkoff.invest.openapi.model.rest.UserAccount;

import java.time.OffsetDateTime;


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
        return openApi.getOperationsContext().getOperations(
                begin,
                end,
                null,
                accounts.getBrokerAccountId()
        ).join();
    }

    @Override
    public void close() throws Exception {
        openApi.close();
    }
}
