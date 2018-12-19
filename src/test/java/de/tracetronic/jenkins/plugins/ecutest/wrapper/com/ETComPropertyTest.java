/*
 * Copyright (c) 2015-2018 TraceTronic GmbH
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *   1. Redistributions of source code must retain the above copyright notice, this
 *      list of conditions and the following disclaimer.
 *
 *   2. Redistributions in binary form must reproduce the above copyright notice, this
 *      list of conditions and the following disclaimer in the documentation and/or
 *      other materials provided with the distribution.
 *
 *   3. Neither the name of TraceTronic GmbH nor the names of its
 *      contributors may be used to endorse or promote products derived from
 *      this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
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
