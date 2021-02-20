package springboot.logic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.tinkoff.invest.openapi.model.rest.Currency;
import ru.tinkoff.invest.openapi.model.rest.Operation;
import ru.tinkoff.invest.openapi.model.rest.OperationStatus;
import ru.tinkoff.invest.openapi.model.rest.OperationTypeWithCommission;
import springboot.classes.CalculationProfit;
import springboot.classes.Profit;
import springboot.openApiConnection.OpenApiTinkoffConnection;
import springboot.openApiConnection.classes.FullInstrumentDescription;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.AbstractMap;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class ProfitCalculator {

    OpenApiTinkoffConnection openApiTinkoffConnection;
    FigiToFullInstrumentDescriptionResolver figiToFullInstrumentDescriptionResolver;

    @Autowired
    public ProfitCalculator(OpenApiTinkoffConnection openApiTinkoffConnection,
                            FigiToFullInstrumentDescriptionResolver figiToFullInstrumentDescriptionResolver) {
        this.openApiTinkoffConnection = openApiTinkoffConnection;
        this.figiToFullInstrumentDescriptionResolver = figiToFullInstrumentDescriptionResolver;
    }

    public List<Operation> getTradingOperations(OffsetDateTime begin, OffsetDateTime end) {
        return openApiTinkoffConnection.getOperations(begin, end).
                getOperations().
                stream().
                filter(this::isTradingOperation).
                collect(Collectors.toList());
    }

    public CompletableFuture<List<CalculationProfit>> calculateProfit(OffsetDateTime begin, OffsetDateTime end) {

        CompletableFuture<List<CalculationProfit>> future = new CompletableFuture<>();

        List<Operation> tradingOperations = openApiTinkoffConnection.getOperations(begin, end).
                getOperations().
                stream().
                filter(this::isTradingOperation).
                collect(Collectors.toList());

        Map<String, List<Operation>> groupingOperations = tradingOperations.stream().
                collect(Collectors.groupingBy(Operation::getFigi, Collectors.toList()));

        CompletableFuture<Map<String, FullInstrumentDescription>> fullInstrumentDescriptionMapFuture =
                figiToFullInstrumentDescriptionResolver.getFullInstrumentDescriptionsByFigi(groupingOperations.keySet());

        fullInstrumentDescriptionMapFuture.thenAccept(stringFullInstrumentDescriptionMap -> {
            List<CalculationProfit> calculationProfits = groupingOperations.entrySet().stream().
                    map(entry -> new AbstractMap.SimpleEntry<String, List<Profit>>(
                            entry.getKey(),
                            this.collectProfitByOperationCollection(entry.getValue()))
                    ).
                    map(entry -> {
                        FullInstrumentDescription fullInstrumentDescription =
                                stringFullInstrumentDescriptionMap.getOrDefault(
                                        entry.getKey(),
                                        FullInstrumentDescription.builder().
                                                figi(entry.getKey()).
                                                name("ERROR").
                                                ticker("ERROR").
                                                build()
                                );
                        return CalculationProfit.builder().
                                description(fullInstrumentDescription.getName()).
                                figi(entry.getKey()).
                                ticker(fullInstrumentDescription.getTicker()).
                                profit(entry.getValue()).
                                build();
                    }).
                    collect(Collectors.toList());
            future.complete(calculationProfits);
        });

        return future;
    }

    private List<Profit> collectProfitByOperationCollection(Collection<Operation> operations) {

        Map<Currency, List<Operation>> groupingOperationsByCurrency = operations.stream().
                collect(Collectors.groupingBy(Operation::getCurrency, Collectors.toList()));

        return groupingOperationsByCurrency.entrySet().stream().
                map(entry -> Profit.builder().
                        currency(entry.getKey()).
                        profit(collectProfit(entry.getValue())).
                        build()).
                collect(Collectors.toList());
    }

    //todo Сделать нормальный подсчёт. Обрадатывать ошибки свёртки. Падает NPE и хер найдёшь почему программа не работает
    private BigDecimal collectProfit(Collection<Operation> operations) {
        return operations.stream().reduce(BigDecimal.ZERO,
                (x, y) -> {
                    BigDecimal decimal = x.add(y.getPayment());
                    if (!Objects.isNull(y.getCommission())){
                        decimal = decimal.add(y.getCommission().getValue());
                    }
                    return decimal;
                },
                BigDecimal::add);
    }

    private boolean isTradingOperation(Operation operation) {
        return (operation.getOperationType().equals(OperationTypeWithCommission.BUY) ||
                operation.getOperationType().equals(OperationTypeWithCommission.SELL) ||
                operation.getOperationType().equals(OperationTypeWithCommission.BUYCARD) ) &&
                operation.getStatus().equals(OperationStatus.DONE);
    }
}
