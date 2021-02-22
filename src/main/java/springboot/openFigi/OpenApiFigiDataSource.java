package springboot.openFigi;

import com.fasterxml.jackson.core.type.TypeReference;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.tinkoff.invest.openapi.exceptions.OpenApiException;
import ru.tinkoff.invest.openapi.okhttp.BaseContextImpl;
import springboot.database.connection.dao.InstrumentDescription;
import springboot.openFigi.classes.FigiIdType;
import springboot.openFigi.classes.FullInstrumentDescription;
import springboot.openFigi.classes.Job;
import springboot.openFigi.classes.JobResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class OpenApiFigiDataSource extends BaseContextImpl implements InstrumentDescriptionDataSource {

    private static final TypeReference<List<JobResult>> figiTypeReference =
            new TypeReference<List<JobResult>>() {};

    private Logger logger = LoggerFactory.getLogger(OpenApiFigiDataSource.class);

    private final OkHttpClient okHttpClient;
    private final HttpUrl finalHttp;

    @Value("${figi.to.full.instrument.description.resolver.request.period:3}")
    private int requestFullDescriptionTimeOut;
    private final Observable<Collection<FullInstrumentDescription>> observable;
    private final Set<String> requestedFullInstrumentDescription;

    @Autowired
    public OpenApiFigiDataSource(
            @Value("${open.figi.token}") String openApiFigiToken,
            @Value("${api.openfigi.openapi.host}") String figiOpenApiHost,
            OkHttpClient okHttpClient) {
        super(okHttpClient, figiOpenApiHost, openApiFigiToken);
        this.okHttpClient = okHttpClient;
        this.finalHttp = Objects.requireNonNull(HttpUrl.parse(figiOpenApiHost)).newBuilder().build();
        this.requestedFullInstrumentDescription = new CopyOnWriteArraySet<>();
        this.observable = Observable.interval(requestFullDescriptionTimeOut, TimeUnit.SECONDS).
                map(aLong -> requestInstrumentDescription());
    }

    @Override
    public Collection<InstrumentDescription> getInstrumentDescriptionsByFigi(Collection<String> figis) {
        return getInstrumentDescriptions(figis).join();
    }

    @Override
    public void addInstrumentDescriptionsToSource(Collection<InstrumentDescription> instrumentDescriptions) {
        //do nothing
    }

    private CompletableFuture<Collection<InstrumentDescription>> getInstrumentDescriptions(Collection<String> figis) {
        CompletableFuture<Collection<InstrumentDescription>> future = new CompletableFuture<>();
        requestedFullInstrumentDescription.addAll(figis);
        Disposable subscribe = observable.subscribe(fullInstrumentDescriptions -> {
                Collection<InstrumentDescription> instrumentDescriptions = fullInstrumentDescriptions.stream().
                        filter(fullInstrumentDescription -> figis.contains(fullInstrumentDescription.getFigi())).
                        map(this::getInstrumentDescriptionSet).
                        collect(Collectors.toSet());
                future.complete(instrumentDescriptions);
                });
        future.thenAccept(fullInstrumentDescriptions -> subscribe.dispose());
        return future;
    }

    private Collection<FullInstrumentDescription> requestInstrumentDescription() {
        Collection<FullInstrumentDescription> instrumentDescriptions = new ArrayList<>();
        if (!requestedFullInstrumentDescription.isEmpty()) {
            try {
                instrumentDescriptions.addAll(requestInstrumentDescription(getJobsSet(requestedFullInstrumentDescription)));
            } catch (Exception e) {
                logger.error("Error when executing a request to OPENAPI " + e.getMessage());
            }
            requestedFullInstrumentDescription.clear();
        }
        return instrumentDescriptions;
    }

    private Collection<FullInstrumentDescription> requestInstrumentDescription(Collection<Job> jobs) throws IOException, OpenApiException {

        final String postJsonData = mapper.writeValueAsString(jobs);

        final RequestBody requestBody = RequestBody.create(postJsonData, MediaType.parse("application/json"));

        final Request request = prepareRequest(finalHttp).
                post(requestBody).
                build();

        Response response = okHttpClient.newCall(request).execute();

        final List<JobResult> result = handleResponse(response, figiTypeReference).
                stream().
                filter(jobResult -> Objects.isNull(jobResult.getError())).
                collect(Collectors.toList());
        List<FullInstrumentDescription> fullInstrumentDescriptions = new ArrayList<>();
        result.forEach(jobResult -> fullInstrumentDescriptions.addAll(jobResult.getData()));

        return fullInstrumentDescriptions;
    }

    private Set<Job> getJobsSet(Collection<String> figis) {
        return figis.stream().
                map(figi -> Job.builder().
                        idType(FigiIdType.ID_BB_GLOBAL).
                        idValue(figi).
                        build()).
                collect(Collectors.toSet());
    }

    private InstrumentDescription getInstrumentDescriptionSet(FullInstrumentDescription description) {
        return InstrumentDescription.builder().
                figi(description.getFigi()).
                marketSelector(description.getMarketSector()).
                name(description.getName()).
                ticker(description.getTicker()).
                build();
    }

    @NotNull
    @Override
    protected Request.Builder prepareRequest(@NotNull final HttpUrl requestUrl) {
        return new Request.Builder()
                .url(requestUrl)
                .addHeader("X-OPENFIGI-APIKEY", this.authToken);
    }

    @NotNull
    @Override
    public String getPath() {
        return "";
    }

}
