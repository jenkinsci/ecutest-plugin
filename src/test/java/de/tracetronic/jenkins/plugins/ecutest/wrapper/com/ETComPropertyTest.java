/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link ETComProperty}.
 */
public class ETComPropertyTest {

    @Test
    public void testDefaultProgId() {
        final ETComProperty prop = ETComProperty.getInstance();
        assertEquals(ETComProperty.DEFAULT_PROG_ID, prop.getProgId());
    }

    @Test
    public void testEmptyProgId() {
        final ETComProperty prop = ETComProperty.getInstance();
        prop.setProgId("");
        assertEquals(ETComProperty.DEFAULT_PROG_ID, prop.getProgId());
    }

    @Test
    public void testNewProgId() {
        final ETComProperty prop = ETComProperty.getInstance();
        prop.setProgId("ECU-TEST6.Application");
        assertEquals("ECU-TEST6.Application", prop.getProgId());
    }

    @Test
    public void testDefaultTimeout() {
        final ETComProperty prop = ETComProperty.getInstance();
        assertEquals(ETComProperty.DEFAULT_TIMEOUT, prop.getTimeout());
    }

    @Test
    public void testNewTimeout() {
        final ETComProperty prop = ETComProperty.getInstance();
        prop.setTimeout(120);
        assertEquals(120, prop.getTimeout());
        prop.setTimeout(ETComProperty.DEFAULT_TIMEOUT);
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
        final File tempFile = File.createTempFile("test", ".ser");

        // Serialize object to file
        final ETComProperty prop = ETComProperty.getInstance();
        final ObjectOutput out = new ObjectOutputStream(new FileOutputStream(tempFile));
        out.writeObject(prop);
        out.close();

        // Deserialize from file to object
        final ObjectInput in = new ObjectInputStream(new FileInputStream(tempFile));
        final ETComProperty prop2 = (ETComProperty) in.readObject();
        in.close();

        tempFile.delete();

        assertEquals("HashCode of both instance must match", prop.hashCode(), prop2.hashCode());
    }
}
