package springboot.openFigi.classes;

/* Figi class */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class FullInstrumentDescription {

    private String figi;
    private String securityType;
    private String marketSector;
    private String ticker;
    private String name;
    private String uniqueID;
    private String exchCode;
    private String shareClassFIGI;
    private String compositeFIGI;
    private String securityType2;
    private String securityDescription;
    private String uniqueIDFutOpt;
}
