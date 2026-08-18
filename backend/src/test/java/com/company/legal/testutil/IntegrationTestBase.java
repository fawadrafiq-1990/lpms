package com.company.legal.testutil;

import com.company.legal.LegalApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Base class for integration tests.
 * Provides shared setup including MockMvc, database seeding, and test utilities.
 */
@SpringBootTest(classes = LegalApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    /**
     * Encodes a plain-text password for use in tests.
     */
    protected String encodePassword(String rawPassword) {
        return passwordEncoder.encode(rawPassword);
    }
}
