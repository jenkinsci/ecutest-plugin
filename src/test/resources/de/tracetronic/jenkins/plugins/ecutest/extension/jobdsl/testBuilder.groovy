freeStyleJob("testBuilder") {
    steps {
        testPackage("test.pkg") {
            testConfig {
                tbcFile("test.tbc")
                tcfFile("test.tcf")
                forceReload(true)
                loadOnly(true)
                keepConfig(true)
                constants {
                    constant("test", "123")
                    constant {
                        name("test2")
                        value("456")
                    }
                }
            }
            packageConfig {
                runTest(false)
                runTraceAnalysis(false)
                parameters {
                    parameter("param", "123")
                    parameter {
                        name("param2")
                        value("456")
                    }
                }
            }
            executionConfig {
                timeout("600")
                stopOnError(false)
                checkTestFile(false)
            }
        }
        testProject("test.prj") {
            testConfig {
                tbcFile("test.tbc")
                tcfFile("test.tcf")
                forceReload(true)
                loadOnly(true)
                keepConfig(true)
                constants {
                    constant("test", "123")
                    constant {
                        name("test2")
                        value("456")
                    }
                }
            }
            projectConfig {
                execInCurrentPkgDir(false)
                filterExpression("Name='TestCase'")
                jobExecutionMode(2)
            }
            executionConfig {
                timeout(600)
                stopOnError(false)
                checkTestFile(false)
            }
        }
        testFolder("test") {
            scanMode("PACKAGES_ONLY")
            recursiveScan(true)
            testConfig {
                tbcFile("test.tbc")
                tcfFile("test.tcf")
                forceReload(true)
                loadOnly(true)
                keepConfig(true)
                constants {
                    constant {
                        name("test")
                        value("123")
                    }
                    constant("test2", "456")
                }
            }
            packageConfig {
                runTest(false)
                runTraceAnalysis(false)
                parameters {
                    parameter {
                        name("param")
                        value("123")
                    }
                    parameter("param2", "456")
                }
            }
            projectConfig {
                execInCurrentPkgDir(false)
                filterExpression("Name='TestCase'")
                jobExecutionMode("PARALLEL_EXECUTION")
            }
            executionConfig {
                timeout("600")
                stopOnError(false)
                checkTestFile(false)
            }
        }
        importPackages {
            importFromTMS("credentialsId", "Subject/Test", "import", "600")
            importFromTMS("credentialsId", "Subject/Test") {
                importPath("import")
                timeout(600)
            }
            importFromTMSDir("credentialsId", "Subject/TestDir", "import", "600")
            importFromTMSDir("credentialsId", "Subject/TestDir") {
                importPath("import")
                timeout(600)
            }
            importAttributesFromTMS("credentialsId", "test.pkg", "600")
            importAttributesFromTMS("credentialsId", "test.pkg") {
                timeout(600)
            }
        }
        importProjects {
            importFromArchive("test.prz", "import", "import", false)
            importFromArchive("test.prz") {
                importPath("import")
                importConfigPath("import")
                replaceFiles(false)
            }
            importFromTMS("credentialsId", "Root/Test", "import", true, "600")
            importFromTMS("credentialsId", "Root/Test") {
                importPath("import")
                importMissingPackages(true)
                timeout(600)
            }
            importFromTMSDir("credentialsId", "Root/TestDir", "import", "600")
            importFromTMSDir("credentialsId", "Root/TestDir") {
                importPath("import")
                timeout(600)
            }
            importAttributesFromTMS("credentialsId", "test.prj", "600")
            importAttributesFromTMS("credentialsId", "test.prj") {
                timeout(600)
            }
        }
        exportPackages {
            exportToTMS("credentialsId", "test.pkg", "Subject/Test", true, "600")
            exportToTMS("credentialsId", "test.pkg") {
                exportPath("Subject/Test")
                createNewPath(true)
                timeout(600)
            }
            exportAttributesToTMS("credentialsId", "test.pkg", "600")
            exportAttributesToTMS("credentialsId", "test.pkg") {
                timeout(600)
            }
        }
        exportProjects {
            exportToTMS("credentialsId", "test.prj", "Root/Test", true, "600")
            exportToTMS("credentialsId", "test.prj") {
                exportPath("Root/Test")
                createNewPath(true)
                timeout(600)
            }
            exportAttributesToTMS("credentialsId", "test.prj", "600")
            exportAttributesToTMS("credentialsId", "test.prj") {
                timeout(600)
            }
        }
    }
}
