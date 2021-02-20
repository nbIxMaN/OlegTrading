package springboot.openApiConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.Call;
import okhttp3.Callback;
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
import ru.tinkoff.invest.openapi.okhttp.BaseContextImpl;
import springboot.openApiConnection.classes.FullInstrumentDescription;
import springboot.openApiConnection.classes.Job;
import springboot.openApiConnection.classes.JobResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Component
public class OpenApiFigiConnection extends BaseContextImpl {

    private static final TypeReference<List<JobResult>> figiTypeReference =
            new TypeReference<List<JobResult>>() {
            };

    Logger logger = LoggerFactory.getLogger(OpenApiFigiConnection.class);

    private final OkHttpClient okHttpClient;
    private final HttpUrl finalHttp;

    @Autowired
    public OpenApiFigiConnection(
            @Value("${open.figi.token}") String openApiFigiToken,
            @Value("${api.openfigi.openapi.host}") String figiOpenApiHost,
            OkHttpClient okHttpClient) {
        super(okHttpClient, figiOpenApiHost, openApiFigiToken);
        this.okHttpClient = okHttpClient;
        this.finalHttp = Objects.requireNonNull(HttpUrl.parse(figiOpenApiHost)).newBuilder().build();
    }

    public CompletableFuture<List<FullInstrumentDescription>> mapJobs(List<Job> jobs) {

        final CompletableFuture<List<FullInstrumentDescription>> future = new CompletableFuture<>();

        final String postJsonData;

        try {

            postJsonData = mapper.writeValueAsString(jobs);

            final RequestBody requestBody = RequestBody.create(postJsonData, MediaType.parse("application/json"));

            final Request request = prepareRequest(finalHttp).
                    post(requestBody).
                    build();

            okHttpClient.newCall(request).enqueue(
                    new Callback() {
                        @Override
                        public void onFailure(@NotNull Call call, @NotNull IOException e) {
                            logger.error("При запросе к REST API произошла ошибка", e);
                            future.completeExceptionally(e);
                        }

                        @Override
                        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                            try {
                                final List<JobResult> result = handleResponse(response, figiTypeReference).
                                        stream().
                                        filter(jobResult -> Objects.isNull(jobResult.getError())).
                                        collect(Collectors.toList());
                                List<FullInstrumentDescription> fullInstrumentDescriptions = new ArrayList<>();
                                result.forEach(jobResult -> fullInstrumentDescriptions.addAll(jobResult.getData()));
                                future.complete(fullInstrumentDescriptions);
                            } catch (Exception ex) {
                                future.completeExceptionally(ex);
                            }
                        }
                    }
            );
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            future.completeExceptionally(e);
        }

        return future;
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
