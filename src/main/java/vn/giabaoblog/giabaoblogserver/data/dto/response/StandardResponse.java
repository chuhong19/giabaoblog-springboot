package vn.giabaoblog.giabaoblogserver.data.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
//import io.swagger.v3.oas.annotations.Hidden;
import lombok.Data;

//@Hidden
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse<T> {
    private String code;
    private String message;
    private T data;

    private StandardResponse(String code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> StandardResponse<T> createMessage(String message) {
        return createMessage(null, message);
    }

    public static <T> StandardResponse<T> createMessage(String code, String message) {
        return create(code, message, null);
    }

    public static <T> StandardResponse<T> create(T data) {
        return create(null, null, data);
    }

    public static <T> StandardResponse<T> create(String code, T data) {
        return create(code, null, data);
    }

    public static <T> StandardResponse<T> create(String code, String message, T data) {
        return new StandardResponse<T>(code, message, data);
    }
}
