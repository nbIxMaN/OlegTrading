package springboot.classes;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CalculationProfit {

    private String figi;
    private String description;
    private String ticker;
    private List<Profit> profit;
}
