package springboot.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;
import springboot.openApiConnection.OpenApiConnection;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProfitCalculator {

    OpenApiConnection openApiConnection;

    @Autowired
    public ProfitCalculator(OpenApiConnection openApiConnection) {
        this.openApiConnection = openApiConnection;
    }

    public List<Operation> calculateProfit(OffsetDateTime begin, OffsetDateTime end) {
        List<Operation> tradingOperations = openApiConnection.getOperations(begin, end).
                getOperations().
                stream().
                filter(this::isTradingOperation).
                collect(Collectors.toList());
        return tradingOperations;
    }

    private boolean isTradingOperation(Operation operation) {
        return (operation.getOperationType().equals(OperationTypeWithCommission.BUY) ||
                operation.getOperationType().equals(OperationTypeWithCommission.SELL)) &&
                operation.getStatus().equals(OperationStatus.DONE);
    }
}
