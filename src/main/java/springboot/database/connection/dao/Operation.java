package springboot.database.connection.dao;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.Date;
import java.util.Set;

@Entity
@Data
public class Operation {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    @ManyToOne
    private Client client;
    @ManyToOne
    private InstrumentDescription instrumentDescription;
    private Double volume;
    private Integer quantity;
    @ManyToOne
    private Currency currency;
    private Date openDate;
    private Date closeDate;
    private Double comission;
    private Boolean isMarginCall;
    private String commentary;
    @ManyToMany
    private Set<OperationGroup> operationGroup;
}
