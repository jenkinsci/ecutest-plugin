/*
 * Copyright (c) 2015-2020 TraceTronic GmbH
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */
package de.tracetronic.jenkins.plugins.ecutest.report.atx.installation;

import de.tracetronic.jenkins.plugins.ecutest.report.atx.installation.ATXSetting.SettingsGroup;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.util.Secret;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parser for the ATX template configuration to gather all available ATX settings.
 */
public final class ATXSettingParser {

    private static final Logger LOGGER = Logger.getLogger(ATXSetting.class.getName());

    private static final String PRECEDING_COMMENT = "//SETTINGS//*[preceding-sibling::comment()[1]";
    private static final String CONNECTION_EXPRESSION = PRECEDING_COMMENT + "[.=' Verbindungseinstellungen ']]";
    private static final String UPLOAD_EXPRESSION = PRECEDING_COMMENT + "[.=' Uploadeinstellungen ']]";
    private static final String ARCHIVE_EXPRESSION = PRECEDING_COMMENT + "[.=' Archiveinstellungen ']]";
    private static final String ATTRIBUTE_EXPRESSION = PRECEDING_COMMENT + "[.=' Attributeinstellungen ']]";
    private static final String TBC_CONSTANT_EXPRESSION = PRECEDING_COMMENT
        + "[.=' TBC-Globale Konstanteneinstellungen ']]";
    private static final String TCF_CONSTANT_EXPRESSION = PRECEDING_COMMENT
        + "[.=' TCF-Globale Konstanteneinstellungen ']]";
    private static final String REVIEW_EXPRESSION = PRECEDING_COMMENT + "[.=' Review Einstellungen ']]";
    private static final String SPECIAL_EXPRESSION = PRECEDING_COMMENT + "[.=' Spezielle Einstellungen ']]";
    private static final List<String> SECRET_SETTINGS =
        Arrays.asList("uploadAuthenticationKey", "httpProxy", "httpsProxy");

    /**
     * Instantiates a new {@link ATXSettingParser}.
     */
    private ATXSettingParser() {
    }

    /**
     * Parses all settings of the ATX template configuration.
     *
     * @param doc the XML document representation
     * @return the map of settings
     */
    public static List<ATXSetting<?>> parseSettings(final Document doc) {
        final List<ATXSetting<?>> settings = new ArrayList<>();

        final List<ATXSetting<?>> connectionSettings = parseSetting(
                doc, SettingsGroup.CONNECTION, CONNECTION_EXPRESSION);
        final List<ATXSetting<?>> uploadSettings = parseSetting(doc, SettingsGroup.UPLOAD, UPLOAD_EXPRESSION);
        final List<ATXSetting<?>> archiveSettings = parseSetting(doc, SettingsGroup.ARCHIVE, ARCHIVE_EXPRESSION);
        final List<ATXSetting<?>> attributeSettings = parseSetting(doc, SettingsGroup.ATTRIBUTE, ATTRIBUTE_EXPRESSION);
        final List<ATXSetting<?>> tbcConstantSettings = parseSetting(doc,
            SettingsGroup.TBC_CONSTANTS, TBC_CONSTANT_EXPRESSION);
        final List<ATXSetting<?>> tcfConstantSettings = parseSetting(doc,
            SettingsGroup.TCF_CONSTANTS, TCF_CONSTANT_EXPRESSION);
        final List<ATXSetting<?>> reviewSettings = parseSetting(doc, SettingsGroup.REVIEW, REVIEW_EXPRESSION);
        final List<ATXSetting<?>> specialSettings = parseSetting(doc, SettingsGroup.SPECIAL, SPECIAL_EXPRESSION);

        settings.addAll(connectionSettings);
        settings.addAll(uploadSettings);
        settings.addAll(archiveSettings);
        settings.addAll(attributeSettings);
        settings.addAll(tbcConstantSettings);
        settings.addAll(tcfConstantSettings);
        settings.addAll(reviewSettings);
        settings.addAll(specialSettings);

        return settings;
    }

    /**
     * Parses a single setting of the ATX template configuration.
     *
     * @param doc        the XML document representation
     * @param group      the settings group
     * @param expression the XPath expression for a separated setting
     * @return the parsed setting represented by a list of settings
     */
    @SuppressFBWarnings(value = "DCN_NULLPOINTER_EXCEPTION", justification = "Has been working as expected.")
    public static List<ATXSetting<?>> parseSetting(final Document doc,
                                                final SettingsGroup group,
                                                final String expression) {
        final List<ATXSetting<?>> settings = new ArrayList<>();
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

                // Add sub setting
                if (isCheckbox(settingName, defaultValue)) {
                    final ATXBooleanSetting setting = new ATXBooleanSetting(settingName, group,
                        descGerman, descEnglish, toBoolean(defaultValue));
                    settings.add(setting);
                } else if (isSecret(settingName)) {
                    final ATXSecretSetting setting = new ATXSecretSetting(settingName, group,
                        descGerman, descEnglish, Secret.fromString(defaultValue));
                    settings.add(setting);
                } else {
                    final ATXTextSetting setting = new ATXTextSetting(settingName, group,
                        descGerman, descEnglish, defaultValue);
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
     * @param node        the node containing the description
     * @param languageKey the language key (only de_DE or en_US supported for now)
     * @return the parsed description value
     */
    private static String parseDescription(final Node node, final String languageKey) {
        String description = "";
        final String expression = String.format("./DESCRIPTION/MULTILANGDATA/ELEMENT[@dkey='%s']/DVALUE", languageKey);
        try {
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final XPathExpression xPathExpression = xpath.compile(expression);
            final Node descNode = (Node) xPathExpression.evaluate(node, XPathConstants.NODE);
            if (descNode != null) {
                final String content = descNode.getTextContent();
                if (content != null) {
                    description = descNode.getTextContent().replaceAll("\\s+", " ").trim();
                }
            }
        } catch (final XPathExpressionException | IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE, "Error parsing description: " + e.getMessage(), e);
        }
        return description;
    }

    /**
     * Parses a node attribute for given name.
     *
     * @param node     the node containing the attribute
     * @param attrName the attribute name
     * @return the parsed attribute value
     */
    private static String parseAttribute(final Node node, final String attrName) {
        return node.getAttributes().getNamedItem(attrName).getTextContent().replaceAll("\\s+", " ").trim();
    }

    /**
     * Determines whether a setting is a checkbox option.
     *
     * @param settingName the name of the setting to check the type for
     * @param defaultValue the default value of the setting
     * @return {@code true} if is checkbox, {@code false} otherwise
     */
    private static boolean isCheckbox(final String settingName, final String defaultValue) {
        if (Arrays.asList("useSettingsFromServer", "archiveRecordings").contains(settingName)) {
            return false;
        }
        return "true".equals(defaultValue.toLowerCase(Locale.getDefault()))
            || "false".equals(defaultValue.toLowerCase(Locale.getDefault()));
    }

    /**
     * Determines whether a setting is a secret option.
     *
     * @param settingName the name of the setting
     * @return {@code true} if is secret, {@code false} otherwise
     */
    private static boolean isSecret(final String settingName) {
        return SECRET_SETTINGS.contains(settingName);
    }

    /**
     * Converts a string value to boolean equivalent.
     *
     * @param value the boolean value
     * @return {@code true} if value represents true, {@code false} otherwise
     */
    private static boolean toBoolean(final String value) {
        return "true".equals(value.toLowerCase(Locale.getDefault()));
    }

    /**
     * Removes whitespace nodes for easier data extraction.
     *
     * @param elem the node element
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
