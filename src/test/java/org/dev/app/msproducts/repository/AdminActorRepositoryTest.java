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
    void returnsTheDemoAdminId() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class))).thenReturn(17L);

        assertEquals(17L, new AdminActorRepository(jdbcTemplate).findDemoAdminId());
    }

    @Test
    void reportsWhenNoActiveDemoAdminExists() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.queryForObject(anyString(), eq(Long.class), any(Object[].class)))
                .thenThrow(new EmptyResultDataAccessException(1));

        assertThrows(StockMovementActorNotFoundException.class,
                () -> new AdminActorRepository(jdbcTemplate).findDemoAdminId());
    }
}
