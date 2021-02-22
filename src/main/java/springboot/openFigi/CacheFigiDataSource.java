package springboot.openFigi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import springboot.database.connection.dao.InstrumentDescription;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class CacheFigiDataSource implements InstrumentDescriptionDataSource {
    private final Map<String, InstrumentDescription> instrumentDescriptionMap;

    @Autowired
    public CacheFigiDataSource() {
        this.instrumentDescriptionMap = new ConcurrentHashMap<>();
    }

    @Override
    public Collection<InstrumentDescription> getInstrumentDescriptionsByFigi(Collection<String> figis) {
        return figis.stream().
                filter(instrumentDescriptionMap::containsKey).
                map(instrumentDescriptionMap::get).
                collect(Collectors.toSet());
    }

    @Override
    public void addInstrumentDescriptionsToSource(Collection<InstrumentDescription> instrumentDescriptions) {
        instrumentDescriptions.forEach(instrumentDescription ->
                instrumentDescriptionMap.putIfAbsent(instrumentDescription.getFigi(), instrumentDescription));
    }
}
