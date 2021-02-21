package springboot.database.connection.dao;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InstrumentDescription {

    @Id
    private String figi;
    private String name;
    private String ticker;
    private String marketSelector;
}
