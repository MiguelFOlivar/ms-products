package org.dev.app.msproducts.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AdminActorRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminActorRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Long findDemoAdminId() {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT id_user
                    FROM users
                    WHERE LOWER(email) = LOWER(?)
                      AND UPPER(role) = 'ADMIN'
                      AND is_active = TRUE
                    ORDER BY id_user
                    LIMIT 1
                    """,
                    Long.class,
                    "admin@demo.com");
        } catch (EmptyResultDataAccessException ex) {
            throw new StockMovementActorNotFoundException(
                    "No existe un usuario ADMIN activo (admin@demo.com) en la tabla users");
        }
    }
}
