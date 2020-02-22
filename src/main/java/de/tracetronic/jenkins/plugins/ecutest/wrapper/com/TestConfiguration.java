/*
 * Copyright (c) 2015-2019 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.wrapper.com;

import com.jacob.com.Dispatch;
import com.jacob.com.Variant;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComConstants;
import de.tracetronic.jenkins.plugins.ecutest.wrapper.com.api.ComTestConfiguration;
import org.apache.commons.lang.StringUtils;

/**
 * COM object representing the currently loaded test configuration file and
 * provides methods for accessing the contained settings.
 */
public class TestConfiguration extends ETComDispatch implements ComTestConfiguration {

    /**
     * Instantiates a new {@link TestConfiguration}.
     *
     * <p>
     * This constructor is used instead of a case operation to turn a Dispatch object into a wider object - it must
     * exist in every wrapper class whose instances may be returned from method calls wrapped in VT_DISPATCH Variants.
     *
     * @param dispatch   the dispatch
     * @param useTimeout specifies whether to apply timeout
     */
    public TestConfiguration(final Dispatch dispatch, final boolean useTimeout) {
        super(dispatch, useTimeout);
    }

    @SuppressWarnings("checkstyle:BooleanExpressionComplexity")
    @Override
    public void setGlobalConstant(final String name, final String value) throws ETComException {
        final Object objValue;
        if (StringUtils.isNotEmpty(value)
            && (StringUtils.isNumeric(value) ||
            startsAndEndsWith(value, "[", "]") ||
            startsAndEndsWith(value, "{", "}") ||
            startsAndEndsWith(value, "(", ")") ||
            startsAndEndsWith(value, "'", "'") ||
            startsAndEndsWith(value, "\"", "\"") ||
            "True".equals(value) || "False".equals(value))) {
            // Assume Python integer, list, dictionary, tuple, string or boolean literal
            objValue = value;
        } else {
            // Convert to Python string literal
            objValue = String.format("'%s'", value);
        }
        performRequest("SetGlobalConstant", new Variant(name), new Variant(objValue));
    }

    /**
     * Checks whether the given value starts with prefix and ends with suffix.
     *
     * @param value  the value
     * @param prefix the prefix
     * @param suffix the suffix
     * @return {@code true} if check passed, {@code false} otherwise
     */
    private boolean startsAndEndsWith(final String value, final String prefix, final String suffix) {
        return value.startsWith(prefix) && value.endsWith(suffix);
    }

    @Override
    public ComConstants getGlobalConstants() throws ETComException {
        return new Constants(performRequest("GetGlobalConstants").toDispatch(), useTimeout());
    }

    @Override
    public String getFileName() throws ETComException {
        return performRequest("GetFileName").getString();
    }
}
