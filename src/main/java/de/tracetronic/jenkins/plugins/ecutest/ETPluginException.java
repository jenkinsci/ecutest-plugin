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
package de.tracetronic.jenkins.plugins.ecutest;

/**
 * Exception thrown if an error occurs while performing plugin-specific operations.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class ETPluginException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new {@link ETPluginException}.
     *
     * @param cause
     *            the cause of the {@link Exception}
     */
    public ETPluginException(final Throwable cause) {
        super(cause);
    }

    /**
     * Instantiates a new {@link ETPluginException}.
     *
     * @param message
     *            the message to attach to the {@link Exception}
     * @param cause
     *            the cause of the {@link Exception}
     */
    public ETPluginException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Instantiates a new {@link ETPluginException}.
     *
     * @param message
     *            the message to attach to the {@link Exception}
     */
    public ETPluginException(final String message) {
        super(message);
    }

    /**
     * Instantiates a new {@link ETPluginException}.
     */
    public ETPluginException() {
        super();
    }
}
