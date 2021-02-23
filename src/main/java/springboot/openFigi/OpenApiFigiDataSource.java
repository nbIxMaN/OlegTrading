package springboot.openFigi;

import com.fasterxml.jackson.core.type.TypeReference;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.Disposable;
import lombok.SneakyThrows;
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
import org.springframework.context.annotation.Scope;
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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Scope("singleton")
public class OpenApiFigiDataSource extends BaseContextImpl implements InstrumentDescriptionDataSource {

    private static final TypeReference<List<JobResult>> figiTypeReference =
            new TypeReference<List<JobResult>>() {};

    private final Logger logger = LoggerFactory.getLogger(OpenApiFigiDataSource.class);

    private final OkHttpClient okHttpClient;
    private final HttpUrl finalHttp;

    @Value("${figi.to.full.instrument.description.resolver.request.period:3}")
    private int requestFullDescriptionTimeOut;
    private final List<CompletableFuture<Collection<InstrumentDescription>>> futures;
    private final Set<String> requestedFullInstrumentDescription;
    private final ExecutorService executorService;

    @Autowired
    public OpenApiFigiDataSource(
            @Value("${open.figi.token}") String openApiFigiToken,
            @Value("${api.openfigi.openapi.host}") String figiOpenApiHost,
            OkHttpClient okHttpClient) {
        super(okHttpClient, figiOpenApiHost, openApiFigiToken);
        this.okHttpClient = okHttpClient;
        this.finalHttp = Objects.requireNonNull(HttpUrl.parse(figiOpenApiHost)).newBuilder().build();
        this.requestedFullInstrumentDescription = new CopyOnWriteArraySet<>();
        this.futures = new CopyOnWriteArrayList<>();
        this.executorService = Executors.newSingleThreadExecutor();
        this.executorService.execute(() -> {
            while (!Thread.interrupted()) {
                try {
                    TimeUnit.SECONDS.sleep(requestFullDescriptionTimeOut);
                    requestInstrumentDescription();
                } catch (InterruptedException e) {
                    logger.info("RequestFigiThread was interrupted");
                }
            }
        });
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
        futures.add(future);
        requestedFullInstrumentDescription.addAll(figis);
        return future.thenCompose(instrumentDescriptions ->
                CompletableFuture.supplyAsync(() ->
                        instrumentDescriptions.stream().filter(
                                instrumentDescription -> figis.contains(instrumentDescription.getFigi())).
                                collect(Collectors.toSet()))

        );
    }

    private void requestInstrumentDescription() {
        logger.info("requestInstrumentDescription in parallel stream");
        Collection<InstrumentDescription> instrumentDescriptions = new ArrayList<>();
        Set<Job> jobs = getJobsSet(requestedFullInstrumentDescription);
        if (!requestedFullInstrumentDescription.isEmpty()) {
            logger.info("requestedFullInstrumentDescription is not empty" + requestedFullInstrumentDescription);
            try {
                Collection<FullInstrumentDescription> fullInstrumentDescriptions = requestInstrumentDescription(jobs);
                instrumentDescriptions.addAll(getInstrumentDescriptionSet(fullInstrumentDescriptions));
            } catch (Exception e) {
                logger.error("Error when executing a request to OPENAPI " + e.getMessage());
            }
            requestedFullInstrumentDescription.clear();
        }
        futures.forEach(futures -> futures.complete(instrumentDescriptions));
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

    private Set<InstrumentDescription> getInstrumentDescriptionSet(Collection<FullInstrumentDescription> description) {
        return description.stream().
                map(fullInstrumentDescription -> InstrumentDescription.builder().
                        figi(fullInstrumentDescription.getFigi()).
                        marketSelector(fullInstrumentDescription.getMarketSector()).
                        name(fullInstrumentDescription.getName()).
                        ticker(fullInstrumentDescription.getTicker()).
                        build()).
                collect(Collectors.toSet());
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
