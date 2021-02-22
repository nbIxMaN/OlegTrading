package springboot.openFigi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springboot.database.connection.dao.InstrumentDescription;
import springboot.database.connection.repositories.InstrumentDescriptionRepository;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class DataBaseFigiDataSource implements InstrumentDescriptionDataSource {

    private final InstrumentDescriptionRepository instrumentDescriptionRepository;

    @Autowired
    public DataBaseFigiDataSource(InstrumentDescriptionRepository instrumentDescriptionRepository) {
        this.instrumentDescriptionRepository = instrumentDescriptionRepository;
    }

    @Override
    @Transactional
    public Collection<InstrumentDescription> getInstrumentDescriptionsByFigi(Collection<String> figis) {
        return figis.stream().
                map(instrumentDescriptionRepository::findById).
                filter(Optional::isPresent).
                map(Optional::get).
                collect(Collectors.toSet());
    }

    @Override
    @Transactional
    public void addInstrumentDescriptionsToSource(Collection<InstrumentDescription> instrumentDescriptions) {
        instrumentDescriptionRepository.saveAll(instrumentDescriptions);
    }
}
