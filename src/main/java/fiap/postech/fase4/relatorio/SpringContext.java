package fiap.postech.fase4.relatorio;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class SpringContext {

    private static final ApplicationContext CONTEXT =
            new AnnotationConfigApplicationContext(CoreConfig.class);

    public static <T> T getBean(Class<T> clazz) {
        return CONTEXT.getBean(clazz);
    }
}
