/**
 * Copyright (c) 2015 TraceTronic GmbH
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
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * Parser for the ATX template configuration to gather all available ATX settings.
 *
 * @author Christian Pönisch <christian.poenisch@tracetronic.de>
 */
@SuppressWarnings("rawtypes")
public final class ATXSettingParser {

    private static final Logger LOGGER = Logger.getLogger(ATXSetting.class.getName());

    private static final String PRECEDING_COMMENT = "//SETTINGS//*[preceding-sibling::comment()[1]";
    private static final String UPLOAD_EXPRESSION = PRECEDING_COMMENT + "[.=' Uploadeinstellungen ']]";
    private static final String ARCHIVE_EXPRESSION = PRECEDING_COMMENT + "[.=' Archiveinstellungen ']]";
    private static final String ATTRIBUTE_EXPRESSION = PRECEDING_COMMENT + "[.=' Attributeinstellungen ']]";
    private static final String TCF_CONSTANT_EXPRESSION = PRECEDING_COMMENT
            + "[.=' TCF-Globale Konstanteneinstellungen ']]";
    private static final String SPECIAL_EXPRESSION = PRECEDING_COMMENT + "[.=' Spezielle Einstellungen ']]";

    /**
     * Instantiates a new {@link ATXSettingParser}.
     */
    private ATXSettingParser() {
    }

    /**
     * Parses all settings of the ATX template configuration.
     *
     * @param doc
     *            the XML document representation
     * @return the map of settings
     */
    public static Map<String, List<ATXSetting>> parseSettings(final Document doc) {
        final Map<String, List<ATXSetting>> configMap = new LinkedHashMap<String, List<ATXSetting>>();

        final List<ATXSetting> uploadSettings = parseSetting(doc, UPLOAD_EXPRESSION);
        final List<ATXSetting> archiveSettings = parseSetting(doc, ARCHIVE_EXPRESSION);
        final List<ATXSetting> attributeSettings = parseSetting(doc, ATTRIBUTE_EXPRESSION);
        final List<ATXSetting> tcfConstantSettings = parseSetting(doc, TCF_CONSTANT_EXPRESSION);
        final List<ATXSetting> specialSettings = parseSetting(doc, SPECIAL_EXPRESSION);

        configMap.put("uploadConfig", uploadSettings);
        configMap.put("archiveConfig", archiveSettings);
        configMap.put("attributeConfig", attributeSettings);
        configMap.put("tcfConstantConfig", tcfConstantSettings);
        configMap.put("specialConfig", specialSettings);

        return configMap;
    }

    /**
     * Parses a single setting of the ATX template configuration.
     *
     * @param doc
     *            the XML document representation
     * @param expression
     *            the XPath expression for a separated setting
     * @return the parsed setting represented by a list of settings
     */
    public static List<ATXSetting> parseSetting(final Document doc, final String expression) {
        final List<ATXSetting> settings = new ArrayList<ATXSetting>();
        try {
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression xPathExpression = xpath.compile(expression);
            final NodeList settingNodes = (NodeList) xPathExpression.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < settingNodes.getLength(); i++) {
                final Node settingNode = settingNodes.item(i);
                removeWhitespaceNodes((Element) settingNode);

                // Parse setting values
                final String settingName = parseAttribute(settingNode, "name");
                final String defaultValue = parseAttribute(settingNode, "default");
                final String descGerman = parseDescription(settingNode, "de_DE");
                final String descEnglish = parseDescription(settingNode, "en_US");
                final boolean isCheckbox = isCheckbox(defaultValue);

                // Add sub setting
                if (isCheckbox) {
                    final ATXBooleanSetting setting = new ATXBooleanSetting(settingName, descGerman,
                            descEnglish, toBoolean(defaultValue));
                    settings.add(setting);
                } else {
                    final ATXTextSetting setting = new ATXTextSetting(settingName, descGerman, descEnglish,
                            defaultValue);
                    settings.add(setting);
                }
            }
        } catch (final XPathExpressionException | IllegalArgumentException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Error parsing setting: " + e.getMessage(), e);
        }
        return settings;
    }

    /**
     * Parses the description for the given language key.
     *
     * @param node
     *            the node containing the description
     * @param languageKey
     *            the language key (only de_DE or en_US supported for now)
     * @return the parsed description value
     */
    private static String parseDescription(final Node node, final String languageKey) {
        String description = "";
        final String expression = "./DESCRIPTION/MULTILANGDATA/ELEMENT[@dkey='" + languageKey + "']/DVALUE";
        try {
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression xPathExpression = xpath.compile(expression);
            final Node descNode = (Node) xPathExpression.evaluate(node, XPathConstants.NODE);
            if (descNode != null) {
                description = descNode.getTextContent().replaceAll("\\s+", " ").trim();
            }
        } catch (final XPathExpressionException | IllegalArgumentException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Error parsing description: " + e.getMessage(), e);
        }
        return description;
    }

    /**
     * Parses a node attribute for given name.
     *
     * @param node
     *            the node containing the attribute
     * @param attrName
     *            the attribute name
     * @return the parsed attribute value
     */
    private static String parseAttribute(final Node node, final String attrName) {
        return node.getAttributes().getNamedItem(attrName).getTextContent().replaceAll("\\s+", " ").trim();
    }

    /**
     * Determines whether a setting is a checkbox option.
     *
     * @param defaultValue
     *            the default value of the setting
     * @return {@code true} if is checkbox, {@code false} otherwise
     */
    private static boolean isCheckbox(final String defaultValue) {
        final boolean isCheckbox;
        if ("true".equals(defaultValue.toLowerCase()) || "false".equals(defaultValue.toLowerCase())) {
            isCheckbox = true;
        } else {
            isCheckbox = false;
        }
        return isCheckbox;
    }

    /**
     * Converts a string value to boolean equivalent.
     *
     * @param value
     *            the boolean value
     * @return {@code true} if value represents true, {@code false} otherwise
     */
    private static boolean toBoolean(final String value) {
        return "true".equals(value.toLowerCase()) ? true : false;
    }

    /**
     * Removes whitespace nodes for easier data extraction.
     *
     * @param elem
     *            the node element
     */
    private static void removeWhitespaceNodes(final Element elem) {
        final NodeList children = elem.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            final Node child = children.item(i);
            if (child instanceof Text && ((Text) child).getData().trim().length() == 0) {
                elem.removeChild(child);
            } else if (!(child instanceof Text)) {
                removeWhitespaceNodes((Element) child);
            }
        }
    }
}
