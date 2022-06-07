#  Copyright (c) 2015-2022 TraceTronic GmbH
#
#  SPDX-License-Identifier: BSD-3-Clause

import getopt
import json
import sys
from os.path import exists

filename = "check_dependies.py"


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
        if self.allow["name"] != self.sbom["name"]:
            self.append_error("name", self.allow["name"], self.sbom["name"])

    def __version(self):
        if self.allow["version"] != self.sbom["version"]:
            self.append_error("version", self.allow["version"], self.sbom["version"])

    def __licenses(self):
        allow_licenses = self.allow["licenses"]
        sbom_licenses = self.sbom.get("licenses") if self.sbom.get("licenses") else []

        for sbom_lic in sbom_licenses:
            sbom_id = sbom_lic["license"].get("id")
            if not sbom_id:
                sbom_id = sbom_lic["license"].get("name")

            id_list = sbom_id in [lic["license"].get("id") for lic in allow_licenses]
            if not id_list:
                self.append_error("license", "to be found in {}".format(allow_licenses), sbom_id)

    def append_error(self, key, expect, current):
        self.errors.append("Component '{}':'{}' expects '{}' but was '{}'."
                           .format(self.allow["group"], key, expect, current))


def read_json(file_path):
    if not exists(file_path):
        print("{} does not exist.".format(file_path))
        sys.exit(2)

    with open(file_path) as f:
        return json.load(f)


def validate(allow_list, sbom):
    allow_json = read_json(allow_list)
    sbom_json = read_json(sbom)
    error_collection = []

    # product
    validator = ComponentValidator(allow_json["component"], sbom_json["metadata"]["component"])
    product_err = validator.validate(True)
    if product_err:
        error_collection.append(product_err)

    # dependencies
    if len(allow_json["components"]) != len(sbom_json["components"]):
        error_collection.append("Number expects {} but was {}.".format(len(allow_json["components"]),
                                                                       len(sbom_json["components"])))
    for component in sbom_json["components"]:
        if component["name"] not in [component["name"] for component in allow_json["components"]]:
            error_collection.append("Could not find component '{}' in allow list".format(component["name"]))

        for allowed_component in allow_json["components"]:
            if not allowed_component.get("licenses"):
                continue

            name = allowed_component["name"] == component["name"]
            group = allowed_component["group"] == component["group"]
            if name & group:
                validator = ComponentValidator(allowed_component, component)
                dependency_err = validator.validate(False)
                if dependency_err:
                    error_collection.extend(dependency_err)

    return error_collection


def main(argv):
    allow_file = ''
    sbom_file = ''
    try:
        opts, args = getopt.getopt(argv, "h", ["allowlist=", "sbom="])
    except getopt.GetoptError:
        print("{} -a <allowfile> -s <sbomfile>".format(filename))
        sys.exit(2)
    for opt, arg in opts:
        if opt == "-h":
            print("{} -a <allowfile> -s <sbomfile>".format(filename))
            sys.exit()
        elif opt == "--allowlist":
            allow_file = arg
        elif opt == "--sbom":
            sbom_file = arg

    for file in [allow_file, sbom_file]:
        if "" == file:
            print("Allow list and sbom file path has to be set. For more information run '{} -h'.".format(filename))
            sys.exit(2)
        if not file.endswith(".json"):
            print("File '{}' does not end with '.json'.".format(filename))
            sys.exit(2)

    err = validate(allow_file, sbom_file)
    if err:
        print(*err, sep="\n")
        sys.exit(2)
    print("License validation finished successfully.")

if __name__ == "__main__":
    main(sys.argv[1:])
