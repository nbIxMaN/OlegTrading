package springboot.openApiConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import ru.tinkoff.invest.openapi.model.rest.PortfolioResponse;
import ru.tinkoff.invest.openapi.okhttp.BaseContextImpl;
import springboot.openApi.figi.classes.Figi;
import springboot.openApi.figi.classes.Job;
import springboot.openApi.figi.classes.JobResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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

    public CompletableFuture<List<Figi>> mapJobs(List<Job> jobs) throws IOException {

        final CompletableFuture<List<Figi>> future = new CompletableFuture<>();

        final String postJsonData = mapper.writeValueAsString(jobs);

        final RequestBody requestBody = RequestBody.create(postJsonData, MediaType.parse("application/json"));

        final Request request = prepareRequest(finalHttp).
                post(requestBody).
                build();

//        Response response = okHttpClient.newCall(request).execute();
//        String responseJson = Objects.requireNonNull(response.body()).string();
//
//        List<JobResult> jobResult = mapper.readValue(responseJson, figiTypeReference);

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
                            final List<JobResult> result = handleResponse(response, figiTypeReference);
                            List<Figi> figis = new ArrayList<>();
                            result.forEach(jobResult -> figis.addAll(jobResult.getData()));
                            future.complete(figis);
                        } catch (Exception ex) {
                            future.completeExceptionally(ex);
                        }
                    }
                }
        );

        return future;
//        String responseJson = Objects.requireNonNull(response.body()).string();
//
//        return response;
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
