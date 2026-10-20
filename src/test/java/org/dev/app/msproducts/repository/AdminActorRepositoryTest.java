package org.dev.app.msproducts.repository;

import org.junit.jupiter.api.Test;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AdminActorRepositoryTest {

    @Test
    void returnsTheAuthenticatedAdminDetails() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class),
                any(Object[].class))).thenAnswer(invocation -> {
            org.springframework.jdbc.core.RowMapper<?> rowMapper = invocation.getArgument(1);
            java.sql.ResultSet resultSet = mock(java.sql.ResultSet.class);
            when(resultSet.getLong("id_user")).thenReturn(17L);
            when(resultSet.getString("full_name")).thenReturn("Admin Demo");
            return rowMapper.mapRow(resultSet, 0);
        });

        assertEquals(new AdminActorRepository.ActorDetails(17L, "Admin Demo"),
                new AdminActorRepository(jdbcTemplate).findActiveAdminByEmail("admin@demo.com"));
    }

    @Test
    void reportsWhenNoActiveDemoAdminExists() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), any(org.springframework.jdbc.core.RowMapper.class),
                any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(StockMovementActorNotFoundException.class,
                () -> new AdminActorRepository(jdbcTemplate).findActiveAdminByEmail("missing@demo.com"));
    }
}
