package com.example.catalogue.domain

import com.example.catalogue.domain.model.EnquiryValidator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class EnquiryValidatorTest {
    @Test fun `email accepts normal addresses and rejects malformed ones`() {
        assertNull(EnquiryValidator.validateEmail("a@b.co"))
        assertNull(EnquiryValidator.validateEmail("  a@b.co  "))
        listOf("", "a", "a@b", "a@@b.co", "a b@c.co").forEach {
            assertNotNull("'$it' should be invalid", EnquiryValidator.validateEmail(it))
        }
    }

    @Test fun `phone is optional but must look like a phone number when present`() {
        assertNull(EnquiryValidator.validatePhone(""))
        assertNull(EnquiryValidator.validatePhone("+61 412 345 678"))
        assertNotNull(EnquiryValidator.validatePhone("12345"))
        assertNotNull(EnquiryValidator.validatePhone("call me 0412345678"))
        assertNotNull(EnquiryValidator.validatePhone("1".repeat(16)))
    }

    @Test fun `message is required and length limited`() {
        assertNotNull(EnquiryValidator.validateMessage(" "))
        assertNull(EnquiryValidator.validateMessage("hello"))
        assertNotNull(EnquiryValidator.validateMessage("x".repeat(EnquiryValidator.MESSAGE_MAX + 1)))
    }

    @Test fun `name is required`() {
        assertNotNull(EnquiryValidator.validateName(""))
        assertNull(EnquiryValidator.validateName("Asha"))
    }
}
