package springboot.logic;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import springboot.openApiConnection.OpenApiFigiConnection;
import springboot.openApiConnection.classes.FigiIdType;
import springboot.openApiConnection.classes.FullInstrumentDescription;
import springboot.openApiConnection.classes.Job;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class FigiToFullInstrumentDescriptionResolver {

    private final Map<String, FullInstrumentDescription> instrumentDescriptionMap;
    private final Set<Job> requestedFullInstrumentDescription;

    private final OpenApiFigiConnection openApiFigiConnection;

    @Value("${figi.to.full.instrument.description.resolver.request.period:3}")
    private int requestFullDescriptionTimeOut;
    private Observable<Boolean> observable;

    public FigiToFullInstrumentDescriptionResolver(OpenApiFigiConnection openApiFigiConnection) {
        this.openApiFigiConnection = openApiFigiConnection;
        //todo Сделать это красиво
        this.instrumentDescriptionMap = new ConcurrentHashMap<>(16, 0.75f, 1);
        this.requestedFullInstrumentDescription = new CopyOnWriteArraySet<>();
    }

    @PostConstruct
    public void init() {
        this.observable = Observable.interval(requestFullDescriptionTimeOut, TimeUnit.SECONDS).
                map(aLong -> requestFullDescription());
    }

    public CompletableFuture<List<FullInstrumentDescription>> getFullInstrumentDescriptionsByFigi(List<String> figis){

        final CompletableFuture<List<FullInstrumentDescription>> future = new CompletableFuture<>();

        Set<Job> jobs = getJobsSet(figis);

        if (!jobs.isEmpty()) {
            requestedFullInstrumentDescription.addAll(jobs);
            Disposable subscribe = observable.subscribe(isDone ->
                    future.complete(getDescriptionList(figis)));
            future.thenAccept(fullInstrumentDescriptions -> subscribe.dispose());
        } else {
            future.complete(getDescriptionList(figis));
        }

        return future;

    }

    private List<FullInstrumentDescription> getDescriptionList(List<String> figis) {
        return figis.stream().
                filter(instrumentDescriptionMap::containsKey).
                map(instrumentDescriptionMap::get).
                collect(Collectors.toList());
    }

    private Set<Job> getJobsSet(List<String> figis) {
        return figis.stream().
                filter(figi -> !instrumentDescriptionMap.containsKey(figi)).
                map(figi -> Job.builder().
                        idType(FigiIdType.ID_BB_GLOBAL).
                        idValue(figi).
                        build()).
                collect(Collectors.toSet());
    }

    private boolean requestFullDescription() {
        List<Job> requestedFullInstrumentDescriptionCopy = new ArrayList<>(requestedFullInstrumentDescription);
        openApiFigiConnection.mapJobs(requestedFullInstrumentDescriptionCopy).join().
                forEach(fullInstrumentDescription -> instrumentDescriptionMap.put(fullInstrumentDescription.getFigi(), fullInstrumentDescription));
        requestedFullInstrumentDescription.removeAll(requestedFullInstrumentDescriptionCopy);
        return true;
    }

}
