package springboot.openApiConnection.classes;

/* Figi class */

import lombok.Data;
import lombok.ToString;

@Data
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
