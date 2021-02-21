package springboot.logic;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import springboot.database.connection.dao.InstrumentDescription;
import springboot.database.connection.repositories.InstrumentDescriptionRepository;
import springboot.openApiConnection.OpenApiFigiConnection;
import springboot.openApiConnection.classes.FigiIdType;
import springboot.openApiConnection.classes.Job;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class FigiToFullInstrumentDescriptionResolver {

    private final Map<String, InstrumentDescription> instrumentDescriptionMap;
    private final Set<Job> requestedFullInstrumentDescription;

    private final OpenApiFigiConnection openApiFigiConnection;
    private final InstrumentDescriptionRepository instrumentDescriptionRepository;

    @Value("${figi.to.full.instrument.description.resolver.request.period:3}")
    private int requestFullDescriptionTimeOut;
    private Observable<Boolean> observable;

    @Autowired
    public FigiToFullInstrumentDescriptionResolver(OpenApiFigiConnection openApiFigiConnection,
                                                   InstrumentDescriptionRepository instrumentDescriptionRepository) {
        this.openApiFigiConnection = openApiFigiConnection;
        this.instrumentDescriptionRepository = instrumentDescriptionRepository;
        //todo Сделать это красиво
        this.instrumentDescriptionMap = new ConcurrentHashMap<>(16, 0.75f, 1);
        this.requestedFullInstrumentDescription = new CopyOnWriteArraySet<>();
    }

    @PostConstruct
    public void init() {
        this.observable = Observable.interval(requestFullDescriptionTimeOut, TimeUnit.SECONDS).
                map(aLong -> requestFullDescription());
    }

    public CompletableFuture<Map<String, InstrumentDescription>> getFullInstrumentDescriptionsByFigi(Collection<String> figis){

        final CompletableFuture<Map<String, InstrumentDescription>> future = new CompletableFuture<>();

        addAvailableFigisFromDataBase(figis);

        Set<Job> jobs = getJobsSet(figis);

        if (!jobs.isEmpty()) {
            requestedFullInstrumentDescription.addAll(jobs);
            Disposable subscribe = observable.subscribe(isDone ->
                    future.complete(getDescriptionMap(figis)));
            future.thenAccept(fullInstrumentDescriptions -> subscribe.dispose());
        } else {
            future.complete(getDescriptionMap(figis));
        }

        return future;

    }

    private void addAvailableFigisFromDataBase(Collection<String> figis) {
        figis.stream().
                filter(figi -> !instrumentDescriptionMap.containsKey(figi)).
                map(instrumentDescriptionRepository::findById).
                forEach(optional -> optional.ifPresent(fullInstrumentDescription ->
                        instrumentDescriptionMap.put(fullInstrumentDescription.getFigi(), fullInstrumentDescription)
                ));
    }

    private Map<String, InstrumentDescription> getDescriptionMap(Collection<String> figis) {
        return figis.stream().
                filter(instrumentDescriptionMap::containsKey).
                map(instrumentDescriptionMap::get).
                collect(Collectors.toMap(InstrumentDescription::getFigi, Function.identity()));
    }

    private Set<Job> getJobsSet(Collection<String> figis) {
        return figis.stream().
                filter(figi -> !instrumentDescriptionMap.containsKey(figi)).
                map(figi -> Job.builder().
                        idType(FigiIdType.ID_BB_GLOBAL).
                        idValue(figi).
                        build()).
                collect(Collectors.toSet());
    }

    //todo Что за join? убрать?
    private boolean requestFullDescription() {
        List<Job> requestedFullInstrumentDescriptionCopy = new ArrayList<>(requestedFullInstrumentDescription);
        openApiFigiConnection.mapJobs(requestedFullInstrumentDescriptionCopy).join().stream().
                map(fullInstrumentDescription -> InstrumentDescription.builder().
                        figi(fullInstrumentDescription.getFigi()).
                        marketSelector(fullInstrumentDescription.getMarketSector()).
                        name(fullInstrumentDescription.getName()).
                        ticker(fullInstrumentDescription.getTicker()).
                        build()).
                forEach(instrumentDescription -> {
                    instrumentDescriptionMap.put(instrumentDescription.getFigi(), instrumentDescription);
                    instrumentDescriptionRepository.save(instrumentDescription);
                });
        requestedFullInstrumentDescription.removeAll(requestedFullInstrumentDescriptionCopy);
        return true;
    }

}
