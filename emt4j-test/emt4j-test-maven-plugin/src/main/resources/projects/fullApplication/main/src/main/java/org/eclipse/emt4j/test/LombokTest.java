import lombok.Data;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

@Data
@Builder
@Wither
public class LombokTest {
    int age;
    String name;

    public static void main(String[] args) {
    }
}
