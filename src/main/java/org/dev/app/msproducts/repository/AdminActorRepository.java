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

    public ActorDetails findActiveAdminByEmail(String email) {
        try {
            return jdbcTemplate.queryForObject(
                    """
                    SELECT id_user, CONCAT_WS(' ', name, paternal_lastname, maternal_lastname) AS full_name
                    FROM users
                    WHERE LOWER(email) = LOWER(?)
                      AND UPPER(role) = 'ADMIN'
                      AND is_active = TRUE
                    """,
                    (resultSet, rowNum) -> new ActorDetails(
                            resultSet.getLong("id_user"),
                            resultSet.getString("full_name")),
                    email);
        } catch (EmptyResultDataAccessException ex) {
            throw new StockMovementActorNotFoundException(
                    "No existe un usuario ADMIN activo para el correo autenticado: " + email);
        }
    }

    public record ActorDetails(Long id, String name) {
    }
}
