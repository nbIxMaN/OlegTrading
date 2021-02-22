package springboot.openFigi.classes;

/* JobResult Class */

import lombok.Data;

import java.util.List;

@Data
public class JobResult {
    public String error;
    public List<FullInstrumentDescription> data;
}