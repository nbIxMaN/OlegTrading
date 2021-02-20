package springboot.database.connection;

import org.springframework.data.repository.CrudRepository;
import springboot.openApiConnection.classes.FullInstrumentDescription;

public interface FullInstrumentDescriptionRepository extends CrudRepository<FullInstrumentDescription, String> {

}
