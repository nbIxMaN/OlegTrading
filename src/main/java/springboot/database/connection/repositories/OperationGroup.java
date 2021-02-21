package springboot.database.connection.repositories;

import org.springframework.data.repository.CrudRepository;
import springboot.database.connection.dao.InstrumentDescription;

public interface OperationGroup extends CrudRepository<InstrumentDescription, Long> {

}
