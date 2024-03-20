import lombok.Data;
import lombok.experimental.Builder;
import lombok.experimental.Value;
import lombok.experimental.Wither;

@Data
@Builder
@Wither
@Value
public class LombokAnnotation {
    private int age;
    private String name;
}
