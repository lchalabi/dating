package dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

@Repository
public class DBUtils {

    private JdbcTemplate template;

    public DBUtils(JdbcTemplate template) {
        this.template = template;
    }

    @PostConstruct
    public void setup() {
        template.execute("CREATE TABLE IF NOT EXISTS users (id serial primary key, first_name text, last_name text, email " +
            "text)");
        template.execute("INSERT INTO users (id, first_name, last_name, email) values " +
            "(1, 'lila', 'chalabi', 'lchalabi@gmail.com') ON CONFLICT DO NOTHING");
    }
}
