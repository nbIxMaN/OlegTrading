package springboot.openFigi.classes;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

/* Job class */

@Builder
@AllArgsConstructor
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Job {
    @EqualsAndHashCode.Include
    private FigiIdType idType;
    @EqualsAndHashCode.Include
    private String idValue;
    private String exchCode;
    private String micCode;
    private String currency;
    private String marketSecDes;
}