package springboot.openFigi;

import springboot.database.connection.dao.InstrumentDescription;

import java.util.Collection;

public interface InstrumentDescriptionDataSource {

    Collection<InstrumentDescription> getInstrumentDescriptionsByFigi(Collection<String> figis);

    void addInstrumentDescriptionsToSource(Collection<InstrumentDescription> instrumentDescriptions);
}
