package springboot.openApi.figi.classes;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Objects;

/* Job class */

@Builder
@AllArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job {
    private FigiIdType idType;
    private String idValue;
    private String exchCode;
    private String micCode;
    private String currency;
    private String marketSecDes;
}