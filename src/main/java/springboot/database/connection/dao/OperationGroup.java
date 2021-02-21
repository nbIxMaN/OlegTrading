package springboot.database.connection.dao;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import java.util.Set;

@Entity
@Data
public class OperationGroup {

    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;
    private String name;
    private String commentary;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Color color;
    @ManyToOne
    @JoinColumn(nullable = false)
    private Client client;

}
