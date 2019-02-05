/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.log;

import hudson.console.LineTransformationOutputStream;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Console annotator which annotates TT messages using {@link TTConsoleNote}.
 * Annotated message has to start with <i>[TT]</i> prefix.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TTConsoleAnnotator extends LineTransformationOutputStream {

    private final OutputStream out;

    /**
     * Instantiates a new {@link TTConsoleAnnotator}.
     *
     * @param out the output stream to write logs
     */
    public TTConsoleAnnotator(final OutputStream out) {
        super();
        this.out = out;
    }

    @Override
    protected void eol(final byte[] bytes, final int len) throws IOException {
        final String line = Charset.defaultCharset().decode(ByteBuffer.wrap(bytes, 0, len)).toString();
        if (line.startsWith("[TT]")) {
            new TTConsoleNote().encodeTo(out);
        }
        out.write(bytes, 0, len);
    }

    @Override
    public void close() throws IOException {
        super.close();
        out.close();
    }
}
