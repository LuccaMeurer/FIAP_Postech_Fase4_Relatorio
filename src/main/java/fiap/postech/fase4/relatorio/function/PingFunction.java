package fiap.postech.fase4.relatorio.function;

import com.microsoft.azure.functions.*;
import com.microsoft.azure.functions.annotation.*;

public class PingFunction {

    @FunctionName("ping")
    public HttpResponseMessage ping(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.GET},
                    authLevel = AuthorizationLevel.ANONYMOUS
            )
            HttpRequestMessage<String> request,
            ExecutionContext context
    ) {
        context.getLogger().info("PING FUNCTION EXECUTOU");

        return request.createResponseBuilder(HttpStatus.OK)
                .body("PING OK NO AZURE")
                .build();
    }
}
