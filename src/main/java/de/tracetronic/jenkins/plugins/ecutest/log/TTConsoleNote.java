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
package de.tracetronic.jenkins.plugins.ecutest.log;

import hudson.Extension;
import hudson.MarkupText;
import hudson.console.ConsoleAnnotationDescriptor;
import hudson.console.ConsoleAnnotator;
import hudson.console.ConsoleNote;

import java.util.Arrays;

/**
 * Annotator which adds color highlighting. There are currently three message categories:
 * <ul>
 * <li>error staring with <i>ERROR:</i> prefix</li>
 * <li>warning starting with <i>WARN:</i> prefix</li>
 * <li>info starting with <i>INFO:</i> prefix</li>
 * </ul>
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
public class TTConsoleNote extends ConsoleNote<Object> {

    private static final long serialVersionUID = 1L;

    @Override
    public ConsoleAnnotator<Object> annotate(final Object context, final MarkupText text, final int charPos) {
        final String plainText = text.getText();
        if (plainText.contains("ERROR:")) {
            text.addMarkup(0, text.length(), "<span style=\"font-weight: bold; color:#FF0000\">", "</span>");
        }
        if (plainText.contains("WARN:")) {
            text.addMarkup(0, text.length(), "<span style=\"color:#ED8B00\">", "</span>");
        }
        if (plainText.contains("INFO:")) {
            text.addMarkup(0, text.length(), "<span style=\"color:#208CA3\">", "</span>");
            addResultMarkup(text);
        }
        if (plainText.contains("DEBUG:")) {
            text.addMarkup(0, text.length(), "<span style=\"color:#63666A\">", "</span>");
        }
        return null;
    }

    /**
     * Adds markup for the test result part of the console log.
     *
     * @param text the text
     */
    private void addResultMarkup(final MarkupText text) {
        final String plainText = text.getText();
        for (final String result : Arrays.asList("NONE", "INCONCLUSIVE", "SUCCESS", "FAILED", "ERROR")) {
            if (plainText.contains("result: " + result)) {
                final int startPos = plainText.indexOf(result);
                final int endPos = startPos + result.length();
                String color;
                switch (result) {
                    case "NONE":
                        color = "#63666A";
                        break;
                    case "INCONCLUSIVE":
                        color = "#ED8B00";
                        break;
                    case "SUCCESS":
                        color = "#A1C057";
                        break;
                    case "FAILED":
                        color = "#F25757";
                        break;
                    case "ERROR":
                        color = "#B40000";
                        break;
                    default:
                        color = "#208CA3";
                        break;
                }
                text.addMarkup(startPos, endPos, "<span style=\"color:" + color + "\">", "</span>");
            }
        }
    }

    /**
     * DescriptorImpl for {@link TTConsoleNote}.
     */
    @Extension
    public static final class DescriptorImpl extends ConsoleAnnotationDescriptor {

        @Override
        public String getDisplayName() {
            return "TraceTronic Console Note";
        }
    }
}
