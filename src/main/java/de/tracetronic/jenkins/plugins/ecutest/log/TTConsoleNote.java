/*
 * Copyright (c) 2015-2023 tracetronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
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
                final String color;
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
            return "tracetronic Console Note";
        }
    }
}
