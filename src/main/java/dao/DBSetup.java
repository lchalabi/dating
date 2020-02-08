package dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Repository
public class DBSetup {

    private JdbcTemplate template;

    public DBSetup(JdbcTemplate template) {
        this.template = template;
    }

    @PostConstruct
    public void setup() {
        users(Arrays.asList(
            "(1, 'lila', 'chalabi', 'lchalabi@gmail.com')",
            "(2, 'sasha', 'whittle', 'sasha@gmail.com')",
            "(3, 'brad', 'tyson', 'btyson@gmail.com')"
            ));
        userRelationships(Arrays.asList(
            "(1, 1, 2, 'LIKED', " + Instant.now().toEpochMilli() + ")",
            "(2, 2, 1, 'LIKED', " + Instant.now().toEpochMilli() + ")"
        ));
    }

    private void users(List<String> values) {
        template.execute("CREATE TABLE IF NOT EXISTS users (id serial PRIMARY KEY, first_name text, last_name text, " +
            "email text UNIQUE)");

        values.forEach(valueSet -> {
            template.execute("INSERT INTO users (id, first_name, last_name, email) values " +
                valueSet + "ON CONFLICT DO NOTHING");
        });
    }

    private void userRelationships(List<String> values) {
        template.execute("CREATE TABLE IF NOT EXISTS user_relationships (id serial UNIQUE, user1_id int not null, " +
            "user2_id int not null, status text not null, updated_at bigint, " +
            "PRIMARY KEY (user1_id, user2_id), " +
            "FOREIGN KEY (user1_id) REFERENCES users (id)," +
            "FOREIGN KEY (user2_id) REFERENCES users (id))");

        values.forEach(valueSet -> {
            template.execute("INSERT INTO user_relationships (id, user1_id, user2_id, status, updated_at) values " +
                valueSet + "ON CONFLICT DO NOTHING");
        });
    }

}