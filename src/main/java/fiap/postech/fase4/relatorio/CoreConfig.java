package fiap.postech.fase4.relatorio;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "fiap.postech.fase4.relatorio")
public class CoreConfig {

    @Bean
    public DataSource dataSource() {
        try {
            String url = System.getenv("DB_URL");
            String user = System.getenv("DB_USER");
            String password = System.getenv("DB_PASSWORD");

            System.out.println("DB_URL = " + url);
            System.out.println("DB_USER = " + user);

            DriverManagerDataSource ds = new DriverManagerDataSource();
            ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
            ds.setUrl(url);
            ds.setUsername(user);
            ds.setPassword(password);
            ds.getConnection().close();

            return ds;
        } catch (Exception e) {
            e.printStackTrace();   // <<< ISSO É O QUE FALTAVA
            throw new RuntimeException(e);
        }
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
