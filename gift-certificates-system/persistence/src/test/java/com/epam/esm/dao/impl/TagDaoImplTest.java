package com.epam.esm.dao.impl;

import com.epam.esm.dao.TagDao;
import com.epam.esm.entity.Tag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TagDaoImplTest {
    private TagDao tagDao;

    @BeforeEach
    void setUp() {
        DataSource dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setScriptEncoding("UTF-8")
                .addScript("classpath:script/tag_create.sql")
                .addScript("classpath:script/tag_fill_up.sql")
                .build();
        tagDao = new TagDaoImpl();
        tagDao.setJdbcTemplate(new JdbcTemplate(dataSource));
    }

    @AfterEach
    void tearDown() {
        new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .setScriptEncoding("UTF-8")
                .addScript("classpath:script/tag_drop.sql")
                .build();
        tagDao = null;
    }

    @Test
    void addCorrectDataShouldReturnTrue() {
        Tag tag = Tag.builder()
                .name("Shop")
                .build();
        boolean actual = tagDao.add(tag);
        assertTrue(actual);
    }

    @Test
    void addCorrectDataShouldSetId() {
        Tag tag = Tag.builder()
                .name("Shop")
                .build();
        tagDao.add(tag);
        long actual = tag.getId();
        long expected = 5;
        assertEquals(actual, expected);
    }

    @Test
    void addIncorrectDataShouldThrowException() {
        StringBuilder name = new StringBuilder("");
        for (int i = 0; i < 21; i++) {
            name.append("aaaaa");
        }
        Tag tag = Tag.builder()
                .name(name.toString())
                .build();
        assertThrows(DataIntegrityViolationException.class, () -> {
            tagDao.add(tag);
        });
    }

    @Test
    void findAllCorrectDataShouldReturnListOfTags() {
        List<Tag> tags = tagDao.findAll();
        long actual = tags.size();
        long expected = 4;
        assertEquals(actual, expected);
    }

    @Test
    void updateRandomDataShouldThrowException() {
        Tag tag = new Tag();
        assertThrows(UnsupportedOperationException.class, () -> {
            tagDao.update(tag);
        });
    }

    @Test
    void removeCorrectDataShouldReturnTrue() {
        long id = 1;
        boolean actual = tagDao.remove(id);
        assertTrue(actual);
    }

    @Test
    void removeCorrectDataShouldReturnFalse() {
        long id = -1;
        boolean actual = tagDao.remove(id);
        assertFalse(actual);
    }
}