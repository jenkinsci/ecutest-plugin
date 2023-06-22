#  Copyright (c) 2015-2023 TraceTronic GmbH
#
#  SPDX-License-Identifier: BSD-3-Clause

import argparse
import getopt
import json
import jsonschema
import sys
import os
from collections import Counter

from os.path import exists

filename = "check_dependencies.py"

COMPATIBLE_LICENSES = [
    "MIT",
    "GPLv2",
    "The MIT License",
    "Apache-2.0",
    "Apache-1.1",
    "PSF-2.0",
    "BSD",
    "BSD-2-Clause",
    "BSD-3-Clause",
    "GNU LGPL",
    "GNU LGPL-2.1",
    "GNU LGPL-3.0",
    "CDDL-1.1",
    "EDL-1.0",
    "MPL-2.0-no-copyleft-exception",
    "EPL-1.0",
    "The JSON License"
]


class ComponentValidator:
    def __init__(self, allow_component, sbom_component):
        self.allow = allow_component
        self.sbom = sbom_component

        self.errors = []

    def validate(self, product=False):
        self.__licenses()
        if not product:
            self.__version()
        return self.errors


    def __version(self):
        if self.allow["moduleVersion"] != self.sbom["version"]:
            self.append_error("version", self.allow["moduleVersion"], self.sbom["version"])

    def __licenses(self):
        if 'moduleLicense' in self.allow["actualLicense"]:
            allow_license = self.allow["moduleLicense"]
        else:
            allow_license = self.allow["actualLicense"]

        prepared_license_list = [lic.lower() for lic in COMPATIBLE_LICENSES]
        if not (any([allow_license.lower() == lic for lic in prepared_license_list])):
            self.append_error("license", COMPATIBLE_LICENSES, allow_license)

        sbom_licenses = []
        for license in self.sbom.get('licenses', []):
            if license.get('license'):
                sbom_licenses.append(license.get('license').get('name', license['license'].get('id', '')))
            elif license.get('expression'):
                sbom_licenses.append(license.get('expression'))
            else:
                sbom_licenses.append('')

        if all(sbom_lic == "" for sbom_lic in sbom_licenses):
            return

        if self.allow["moduleLicense"] not in sbom_licenses:
            self.append_error("license", sbom_licenses, self.allow["moduleLicense"])

    def append_error(self, key, expect, current):
        self.errors.append("Component '{}':'{}' expects (one of) '{}' but was '{}'."
                           .format(self.allow["moduleName"], key, expect, current))


def read_json(file_path):
    if not exists(file_path):
        print("{} does not exist.".format(file_path))
        sys.exit(2)

    with open(file_path) as f:
        return json.load(f)


def compare_license_files(allowlist_path, sbom_path, allowschema_path):
    allow_json = read_json(allowlist_path)
    sbom_json = read_json(sbom_path)
    schema_json = read_json(allowschema_path)
    found_errors = []
    found_warnings = []

    # check for correct schema first
    try:
        jsonschema.validate(allow_json, schema_json)
    except jsonschema.exceptions.ValidationError as error:
        found_errors.append(f"allowlist does not comply to the schema...\n{error.message}")

    # dependencies
    allowed_packages = [item["moduleName"] for item in allow_json["allowedLicenses"]]
    sbom_packages = ["{}:{}".format(item["group"], item["name"]) for item in sbom_json["components"]]

    count_allowed_packages = Counter(allowed_packages)
    duplicates = [package for package, count in count_allowed_packages.items() if count > 1]
    if duplicates:
        found_errors.append("Packages in allowed_licenses have to be unique: {} ".format(duplicates))

    if len(allowed_packages) != len(sbom_packages):
        found_errors.append("Number of components expects {} but was {}.".format(len(allow_json["allowedLicenses"]),
                                                                                 len(sbom_json["components"])))
    missing_allowed = set(sbom_packages).difference(set(allowed_packages))
    missing_sbom = set(allowed_packages).difference(set(sbom_packages))

    if len(missing_sbom):
        found_errors.append("Dependencies {} not found in sbom_path but in allow list. Check if still "
                            "necessary.".format(missing_sbom))
    elif len(missing_allowed):
        found_warnings.append("Dependencies {} not found in allow list but in sbom_path."
                              .format(missing_allowed))

    for component in sbom_json["components"]:
        # component["license"] = metadata(component["name"])["license"]
        for allowed_component in allow_json["allowedLicenses"]:
            name = allowed_component["moduleName"] == "{}:{}".format(component["group"], component["name"])
            if name:
                validator = ComponentValidator(allowed_component, component)
                dependency_err = validator.validate(False)
                if dependency_err:
                    found_errors.extend(dependency_err)

    return found_errors, found_warnings


def main(argv):
    parser = argparse.ArgumentParser(prog="DependencyChecker",
                                     description="Checks the license entries of a sbom against a allowed list.")

    parser.add_argument("--allowlist", "-a", type=str, required=True, help="Path to allowlist file")
    parser.add_argument("--sbom", "-s", type=str, required=True, help="Path to sbom file")
    parser.add_argument("--schema", type=str, default="config/allowlist_schema.json", help="Path to schema file")

    args = parser.parse_args()

    allow_filepath = args.allowlist
    sbom_filepath = args.sbom
    schema_filepath = args.schema

    for file in [allow_filepath, sbom_filepath]:
        if "" == file:
            parser.error("Allow list and sbom file paths have to be set.")
        if not file.endswith(".json"):
            parser.error("File does not end with '.json'.")

    err, warn = compare_license_files(allow_filepath, sbom_filepath, schema_filepath)
    if warn:
        print(*warn, sep="\n")

    if err:
        print(*err, sep="\n")
        sys.exit(2)

    print("Dependency validation finished successfully.")


if __name__ == "__main__":
    # check against allowlist
    main(sys.argv[1:])
