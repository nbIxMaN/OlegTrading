package springboot.classes;

import lombok.Builder;
import lombok.Data;
import ru.tinkoff.invest.openapi.model.rest.Currency;

import java.math.BigDecimal;

@Data
@Builder
public class Profit {
    private BigDecimal profit;
    private Currency currency;
}
