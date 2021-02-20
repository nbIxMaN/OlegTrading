package springboot.openApiConnection.classes;

/* Figi class */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@Entity
public class FullInstrumentDescription {

    @Id
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
