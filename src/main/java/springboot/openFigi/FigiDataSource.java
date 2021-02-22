package springboot.openFigi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springboot.database.connection.dao.InstrumentDescription;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FigiDataSource implements InstrumentDescriptionDataSource{

    Logger logger = LoggerFactory.getLogger(FigiDataSource.class);

    private final CacheFigiDataSource cacheFigiDataSource;
    private final DataBaseFigiDataSource dataBaseFigiDataSource;
    private final OpenApiFigiDataSource openApiFigiDataSource;

    @Autowired
    public FigiDataSource(CacheFigiDataSource cacheFigiDataSource,
                          DataBaseFigiDataSource dataBaseFigiDataSource,
                          OpenApiFigiDataSource openApiFigiDataSource) {
        this.cacheFigiDataSource = cacheFigiDataSource;
        this.dataBaseFigiDataSource = dataBaseFigiDataSource;
        this.openApiFigiDataSource = openApiFigiDataSource;
    }

    @Override
    public Collection<InstrumentDescription> getInstrumentDescriptionsByFigi(Collection<String> figis) {

        Set<String> figisCopy = new HashSet<>(figis);

        Collection<InstrumentDescription> cacheInstrumentDescriptions = cacheFigiDataSource.getInstrumentDescriptionsByFigi(figisCopy);
        figisCopy.remove(cacheInstrumentDescriptions.stream().
                map(InstrumentDescription::getFigi).
                collect(Collectors.joining()));

        Collection<InstrumentDescription> dataBaseInstrumentDescriptions = cacheFigiDataSource.getInstrumentDescriptionsByFigi(figisCopy);
        figisCopy.remove(dataBaseInstrumentDescriptions.stream().
                map(InstrumentDescription::getFigi).
                collect(Collectors.joining()));

        Collection<InstrumentDescription> openApiInstrumentDescriptions = openApiFigiDataSource.getInstrumentDescriptionsByFigi(figisCopy);
        figisCopy.remove(openApiInstrumentDescriptions.stream().
                map(InstrumentDescription::getFigi).
                collect(Collectors.joining()));

        if (!figisCopy.isEmpty()) {
            logger.warn("Some instrument descriptions is not found " + figisCopy);
        }

        dataBaseFigiDataSource.addInstrumentDescriptionsToSource(openApiInstrumentDescriptions);
        cacheFigiDataSource.addInstrumentDescriptionsToSource(openApiInstrumentDescriptions);
        cacheFigiDataSource.addInstrumentDescriptionsToSource(dataBaseInstrumentDescriptions);

        Set<InstrumentDescription> instrumentDescriptions = new HashSet<>();
        instrumentDescriptions.addAll(cacheInstrumentDescriptions);
        instrumentDescriptions.addAll(dataBaseInstrumentDescriptions);
        instrumentDescriptions.addAll(openApiInstrumentDescriptions);

        return instrumentDescriptions;
    }

    @Override
    public void addInstrumentDescriptionsToSource(Collection<InstrumentDescription> instrumentDescriptions) {
        cacheFigiDataSource.addInstrumentDescriptionsToSource(instrumentDescriptions);
        dataBaseFigiDataSource.addInstrumentDescriptionsToSource(instrumentDescriptions);
        openApiFigiDataSource.addInstrumentDescriptionsToSource(instrumentDescriptions);
    }
}
