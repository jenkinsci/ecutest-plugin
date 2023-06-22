#  Copyright (c) 2015-2023 TraceTronic GmbH
#
#  SPDX-License-Identifier: BSD-3-Clause
#
#  SPDX-License-Identifier: BSD-3-Clause

import getopt
import json
import jsonschema
import sys
import os

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
        self.__name()
        self.__licenses()
        if not product:
            self.__version()
        return self.errors

    def __name(self):
        sbom_name = "{}:{}".format(self.sbom["group"], self.sbom["name"])
        if self.allow["moduleName"] != sbom_name:
            self.append_error("name", self.allow["moduleName"], sbom_name)

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

    if len(allowed_packages) != len(sbom_packages):
        found_errors.append("Number of components expects {} but was {}.".format(len(allow_json["allowedLicenses"]),
                                                                                 len(sbom_json["components"])))
    missing_allowed = set(sbom_packages).difference(set(allowed_packages))
    missing_sbom = set(allowed_packages).difference(set(sbom_packages))

    if len(missing_sbom):
        found_errors.append("Dependency {} not found in sbom_path but in allow list.".format(missing_sbom))
    elif len(missing_allowed):
        found_warnings.append("Dependency {} not found in allow list but in sbom_path. Check if still necessary."
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
    allow_filepath = ''
    sbom_filepath = ''
    schema_filepath = "config/allowlist_schema.json"
    try:
        opts, args = getopt.getopt(argv, "h", ["allowlist=", "sbom=", "schema="])
    except getopt.GetoptError:
        print("{} -a <allowfile> -s <sbomfile>".format(filename))
        sys.exit(2)
    for opt, arg in opts:
        if opt == "-h":
            print("{} -a <allowfile> -s <sbomfile>".format(filename))
            sys.exit()
        elif opt == "--allowlist":
            allow_filepath = arg
        elif opt == "--sbom":
            sbom_filepath = arg
        elif opt == "--schema":
            schema_filepath = arg

    for file in [allow_filepath, sbom_filepath]:
        if "" == file:
            print(f"Allow list and sbom_path file path have to be set. For more information run '{filename} -h'.")
            sys.exit(2)
        if not file.endswith(".json"):
            print(f"File '{filename}' does not end with '.json'.")
            sys.exit(2)

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
